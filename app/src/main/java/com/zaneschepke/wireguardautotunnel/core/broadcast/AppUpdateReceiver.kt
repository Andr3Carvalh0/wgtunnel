package com.zaneschepke.wireguardautotunnel.core.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.zaneschepke.wireguardautotunnel.domain.repository.AppDataRepository
import com.zaneschepke.wireguardautotunnel.di.ApplicationScope
import com.zaneschepke.wireguardautotunnel.core.service.ServiceManager
import com.zaneschepke.wireguardautotunnel.core.tunnel.TunnelManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
internal class AppUpdateReceiver : BroadcastReceiver() {

	@Inject
	@ApplicationScope
	lateinit var applicationScope: CoroutineScope

	@Inject
	lateinit var appDataRepository: AppDataRepository

	@Inject
	lateinit var serviceManager: ServiceManager

	@Inject
	lateinit var tunnelManager: TunnelManager

	override fun onReceive(context: Context, intent: Intent) {
		if (intent.action != Intent.ACTION_MY_PACKAGE_REPLACED) return
		serviceManager.updateTunnelTile()
		serviceManager.updateAutoTunnelTile()
		applicationScope.launch {
			with(appDataRepository.settings.get()) {
				if (isRestoreOnBootEnabled) {
					// If auto tunnel is enabled, just start it and let auto tunnel start appropriate tun
					if (isAutoTunnelEnabled && !serviceManager.autoTunnelActive.value) return@launch serviceManager.startAutoTunnel(true)
					tunnelManager.restorePreviousState()
				}
			}
		}
	}
}
