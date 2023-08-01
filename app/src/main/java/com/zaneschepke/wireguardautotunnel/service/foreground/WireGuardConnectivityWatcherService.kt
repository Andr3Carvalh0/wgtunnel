package com.zaneschepke.wireguardautotunnel.service.foreground

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PowerManager
import android.os.SystemClock
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wireguard.android.backend.Tunnel
import com.zaneschepke.wireguardautotunnel.Constants
import com.zaneschepke.wireguardautotunnel.R
import com.zaneschepke.wireguardautotunnel.repository.Repository
import com.zaneschepke.wireguardautotunnel.service.network.MobileDataService
import com.zaneschepke.wireguardautotunnel.service.network.NetworkService
import com.zaneschepke.wireguardautotunnel.service.network.NetworkStatus
import com.zaneschepke.wireguardautotunnel.service.network.WifiService
import com.zaneschepke.wireguardautotunnel.service.notification.NotificationService
import com.zaneschepke.wireguardautotunnel.service.tunnel.VpnService
import com.zaneschepke.wireguardautotunnel.service.tunnel.model.Settings
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class WireGuardConnectivityWatcherService : ForegroundService() {

    private val foregroundId = 122;

    @Inject
    lateinit var wifiService : NetworkService<WifiService>

    @Inject
    lateinit var mobileDataService : NetworkService<MobileDataService>

    @Inject
    lateinit var settingsRepo: Repository<Settings>

    @Inject
    lateinit var notificationService : NotificationService

    @Inject
    lateinit var vpnService : VpnService

    private var isWifiConnected = false;
    private var isMobileDataConnected = false;
    private var currentNetworkSSID = "";

    private lateinit var watcherJob : Job;
    private lateinit var setting : Settings
    private lateinit var tunnelId: String

    private var wakeLock: PowerManager.WakeLock? = null
    private val tag = this.javaClass.name;


    override fun startService(extras: Bundle?) {
        super.startService(extras)
        val tunnelId = extras?.getString(getString(R.string.tunnel_extras_key))
        if (tunnelId != null) {
            this.tunnelId = tunnelId
        }
        // we need this lock so our service gets not affected by Doze Mode
        initWakeLock()
        cancelWatcherJob()
        launchWatcherNotification()
        if(this::tunnelId.isInitialized) {
            startWatcherJob()
        } else {
            stopService(extras)
        }
    }

    override fun stopService(extras: Bundle?) {
        super.stopService(extras)
        wakeLock?.let {
            if (it.isHeld) {
                it.release()
            }
        }
        cancelWatcherJob()
        stopVPN()
        stopSelf()
    }

    private fun launchWatcherNotification() {
        val notification = notificationService.createNotification(
            channelId = getString(R.string.watcher_channel_id),
            channelName = getString(R.string.watcher_channel_name),
            description = getString(R.string.watcher_notification_text))
        super.startForeground(foregroundId, notification)
    }

    //try to start task again if killed
    override fun onTaskRemoved(rootIntent: Intent) {
        Timber.d("Task Removed called")
        val restartServiceIntent = Intent(rootIntent)
        val restartServicePendingIntent: PendingIntent = PendingIntent.getService(this, 1, restartServiceIntent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE);
        applicationContext.getSystemService(Context.ALARM_SERVICE);
        val alarmService: AlarmManager = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager;
        alarmService.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 1000, restartServicePendingIntent);
    }

    private fun initWakeLock() {
        wakeLock =
            (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "$tag::lock").apply {
                    acquire()
                }
            }
    }

    private fun cancelWatcherJob() {
        if(this::watcherJob.isInitialized) {
            watcherJob.cancel()
        }
    }

    private fun startWatcherJob() {
        watcherJob = CoroutineScope(SupervisorJob()).launch {
            val settings = settingsRepo.getAll();
            if(!settings.isNullOrEmpty()) {
                setting = settings[0]
            }
            CoroutineScope(watcherJob).launch {
                watchForWifiConnectivityChanges()
            }
            if(setting.isTunnelOnMobileDataEnabled) {
                CoroutineScope(watcherJob).launch {
                    watchForMobileDataConnectivityChanges()
                }
            }
            CoroutineScope(watcherJob).launch {
                manageVpn()
            }
        }
    }

    private suspend fun watchForMobileDataConnectivityChanges() {
        mobileDataService.networkStatus.collect {
            when(it) {
                is NetworkStatus.Available -> {
                    Timber.d("Gained Mobile data connection")
                    isMobileDataConnected = true
                }
                is NetworkStatus.CapabilitiesChanged -> {
                    isMobileDataConnected = true
                    Timber.d("Mobile data capabilities changed")
                }
                is NetworkStatus.Unavailable -> {
                    isMobileDataConnected = false
                    Timber.d("Lost mobile data connection")
                }
            }
        }
    }

    private suspend fun watchForWifiConnectivityChanges() {
        wifiService.networkStatus.collect {
                when (it) {
                    is NetworkStatus.Available -> {
                        Timber.d("Gained Wi-Fi connection")
                        isWifiConnected = true
                    }
                    is NetworkStatus.CapabilitiesChanged -> {
                        Timber.d("Wifi capabilities changed")
                        isWifiConnected = true
                        currentNetworkSSID = wifiService.getNetworkName(it.networkCapabilities) ?: "";
                    }
                    is NetworkStatus.Unavailable -> {
                        isWifiConnected = false
                        Timber.d("Lost Wi-Fi connection")
                    }
                }
            }
        }

    private suspend fun manageVpn() {
        while(watcherJob.isActive) {
            if(setting.isTunnelOnMobileDataEnabled &&
                !isWifiConnected &&
                isMobileDataConnected
                && vpnService.getState() == Tunnel.State.DOWN) {
                startVPN()
            } else if(!setting.isTunnelOnMobileDataEnabled &&
                !isWifiConnected &&
                vpnService.getState() == Tunnel.State.UP) {
                stopVPN()
            } else if(isWifiConnected &&
                !setting.trustedNetworkSSIDs.contains(currentNetworkSSID) &&
                (vpnService.getState() != Tunnel.State.UP)) {
                startVPN()
            } else if((isWifiConnected &&
                        setting.trustedNetworkSSIDs.contains(currentNetworkSSID)) &&
                (vpnService.getState() == Tunnel.State.UP)) {
                stopVPN()
            }
            delay(Constants.VPN_CONNECTIVITY_CHECK_INTERVAL)
        }
    }

    private fun startVPN() {
        ServiceTracker.actionOnService(
            Action.START,
            this.applicationContext as Application,
            WireGuardTunnelService::class.java,
            mapOf(getString(R.string.tunnel_extras_key) to tunnelId))
    }
    private fun stopVPN() {
        ServiceTracker.actionOnService(
            Action.STOP,
            this.applicationContext as Application,
            WireGuardTunnelService::class.java
        )
    }
}