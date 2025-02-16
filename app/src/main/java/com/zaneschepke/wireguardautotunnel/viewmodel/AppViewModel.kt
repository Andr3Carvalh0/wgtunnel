package com.zaneschepke.wireguardautotunnel.viewmodel

import android.content.Context
import android.content.pm.ApplicationInfo
import androidx.lifecycle.viewModelScope
import com.zaneschepke.wireguardautotunnel.R
import com.zaneschepke.wireguardautotunnel.core.service.ServiceManager
import com.zaneschepke.wireguardautotunnel.core.tunnel.TunnelManager
import com.zaneschepke.wireguardautotunnel.di.IoDispatcher
import com.zaneschepke.wireguardautotunnel.domain.entity.TunnelConf
import com.zaneschepke.wireguardautotunnel.domain.enums.BackendState
import com.zaneschepke.wireguardautotunnel.domain.repository.AppDataRepository
import com.zaneschepke.wireguardautotunnel.ui.common.snackbar.SnackbarController
import com.zaneschepke.wireguardautotunnel.ui.state.AppUiState
import com.zaneschepke.wireguardautotunnel.ui.state.InterfaceProxy
import com.zaneschepke.wireguardautotunnel.ui.state.PeerProxy
import com.zaneschepke.wireguardautotunnel.ui.state.SplitTunnelApp
import com.zaneschepke.wireguardautotunnel.util.Constants
import com.zaneschepke.wireguardautotunnel.util.LocaleUtil
import com.zaneschepke.wireguardautotunnel.util.StringValue
import com.zaneschepke.wireguardautotunnel.util.extensions.getAllInternetCapablePackages
import com.zaneschepke.wireguardautotunnel.util.extensions.withData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.withContext
import org.amnezia.awg.config.Config
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AppViewModel
@Inject
constructor(
	appDataRepository: AppDataRepository,
	@IoDispatcher private val ioDispatcher: CoroutineDispatcher,
	private val tunnelManager: TunnelManager,
	private val serviceManager: ServiceManager,
) : BaseViewModel(appDataRepository) {

	private val _popBackStack = MutableSharedFlow<Boolean>()
	val popBackStack = _popBackStack.asSharedFlow()

	private val _isAppReady = MutableStateFlow(false)
	val isAppReady = _isAppReady.asStateFlow()

	private val _configurationChange = MutableStateFlow(false)
	val configurationChange = _configurationChange.asStateFlow()

	private val _splitTunnelApps = MutableStateFlow<List<SplitTunnelApp>>(emptyList())
	val splitTunnelApps = _splitTunnelApps.asStateFlow()

	val uiState =
		combine(
			appDataRepository.settings.flow,
			appDataRepository.tunnels.flow,
			appDataRepository.appState.flow,
			serviceManager.autoTunnelActive,
		) { settings, tunnels, generalState, autoTunnel ->
			AppUiState(
				settings,
				tunnels,
				generalState,
				autoTunnel,
			)
		}.stateIn(
			viewModelScope + ioDispatcher,
			SharingStarted.Companion.WhileSubscribed(Constants.SUBSCRIPTION_TIMEOUT),
			AppUiState(),
		)

	init {
		viewModelScope.launch {
			handleKillSwitchChange()
			initServices()
			launch {
				initTunnels()
			}
			appReadyCheck()
		}
	}

	private suspend fun appReadyCheck() {
		val tunnelCount = appDataRepository.tunnels.count()
		uiState.takeWhile { it.tunnels.size != tunnelCount }.onCompletion {
			_isAppReady.emit(true)
		}.collect()
	}

	private suspend fun initTunnels() {
		tunnels.withData {
			it.filter { it.isActive }.forEach {
				tunnelManager.startTunnel(it)
			}
		}
	}

	private suspend fun initServices() {
		withContext(ioDispatcher) {
			appSettings.withData {
				if (it.isAutoTunnelEnabled) serviceManager.startAutoTunnel(false)
			}
		}
	}

	fun setLocationDisclosureShown() = viewModelScope.launch {
		appDataRepository.appState.setLocationDisclosureShown(true)
	}

	fun onToggleAlwaysOnVPN() = viewModelScope.launch {
		with(uiState.value.appSettings) {
			appDataRepository.settings.save(
				copy(
					isAlwaysOnVpnEnabled = !isAlwaysOnVpnEnabled,
				),
			)
		}
	}

	fun onLocaleChange(localeTag: String) = viewModelScope.launch {
		appDataRepository.appState.setLocale(localeTag)
		LocaleUtil.changeLocale(localeTag)
		_configurationChange.update {
			true
		}
	}

	fun onToggleRestartAtBoot() = viewModelScope.launch {
		with(uiState.value.appSettings) {
			appDataRepository.settings.save(
				copy(
					isRestoreOnBootEnabled = !isRestoreOnBootEnabled,
				),
			)
		}
	}

	fun onToggleVpnKillSwitch(enabled: Boolean) = viewModelScope.launch {
		with(uiState.value.appSettings) {
			appDataRepository.settings.save(
				copy(
					isVpnKillSwitchEnabled = enabled,
					isLanOnKillSwitchEnabled = if (enabled) isLanOnKillSwitchEnabled else false,
				),
			)
		}
		handleKillSwitchChange()
	}

	private suspend fun handleKillSwitchChange() {
		withContext(ioDispatcher) {
			appSettings.withData {
				if (!it.isVpnKillSwitchEnabled) return@withData tunnelManager.setBackendState(BackendState.SERVICE_ACTIVE, emptyList())
				Timber.d("Starting kill switch")
				val allowedIps = if (it.isLanOnKillSwitchEnabled) TunnelConf.LAN_BYPASS_ALLOWED_IPS else emptyList()
				tunnelManager.setBackendState(BackendState.KILL_SWITCH_ACTIVE, allowedIps)
			}
		}
	}

	fun onToggleLanOnKillSwitch(enabled: Boolean) = viewModelScope.launch(ioDispatcher) {
		appDataRepository.settings.save(
			uiState.value.appSettings.copy(
				isLanOnKillSwitchEnabled = enabled,
			),
		)
		handleKillSwitchChange()
	}

	fun onToggleShortcutsEnabled() = viewModelScope.launch {
		with(uiState.value.appSettings) {
			appDataRepository.settings.save(
				copy(
					isShortcutsEnabled = !isShortcutsEnabled,
				),
			)
		}
	}

	suspend fun getEmitSplitTunnelApps(context: Context) {
		withContext(ioDispatcher) {
			val apps = context.getAllInternetCapablePackages().filter { it.applicationInfo != null }.map { pack ->
				SplitTunnelApp(
					icon = context.packageManager.getApplicationIcon(pack.applicationInfo!!),
					name = context.packageManager.getApplicationLabel(pack.applicationInfo!!).toString(),
					`package` = pack.packageName,
					isSystem = pack.applicationInfo?.let { (it.flags and ApplicationInfo.FLAG_SYSTEM) != 0 } ?: false,
				)
			}
			_splitTunnelApps.emit(apps)
		}
	}

	fun updateExistingTunnelConfig(
		tunnelConfig: TunnelConf,
		tunnelName: String? = null,
		peers: List<PeerProxy>? = null,
		`interface`: InterfaceProxy? = null,
	) = viewModelScope.launch {
		runCatching {
			val amConfig = tunnelConfig.toAmConfig()
			val wgConfig = tunnelConfig.toWgConfig()
			updateTunnelConfig(tunnelConfig, tunnelName, amConfig, wgConfig, peers, `interface`)
			_popBackStack.emit(true)
			SnackbarController.Companion.showMessage(StringValue.StringResource(R.string.config_changes_saved))
		}.onFailure {
			onConfigSaveError(it)
		}
	}

	fun saveNewTunnel(tunnelName: String, peers: List<PeerProxy>, `interface`: InterfaceProxy) = viewModelScope.launch {
		runCatching {
			val config = buildConfigs(peers, `interface`)
			appDataRepository.tunnels.save(
				TunnelConf(
					tunName = tunnelName,
					wgQuick = config.first.toWgQuickString(true),
					amQuick = config.second.toAwgQuickString(true),
				),
			)
			_popBackStack.emit(true)
			SnackbarController.Companion.showMessage(StringValue.StringResource(R.string.config_changes_saved))
		}.onFailure {
			onConfigSaveError(it)
		}
	}

	private fun onConfigSaveError(throwable: Throwable) {
		Timber.Forest.e(throwable)
		SnackbarController.Companion.showMessage(
			throwable.message?.let { message ->
				(StringValue.DynamicString(message))
			} ?: StringValue.StringResource(R.string.unknown_error),
		)
	}

	private suspend fun updateTunnelConfig(
		tunnelConf: TunnelConf,
		tunnelName: String? = null,
		amConfig: Config,
		wgConfig: com.wireguard.config.Config,
		peers: List<PeerProxy>? = null,
		`interface`: InterfaceProxy? = null,
	) {
		val configs = rebuildConfigs(amConfig, wgConfig, peers, `interface`)
		appDataRepository.tunnels.save(
			tunnelConf.copy(
				tunName = tunnelName ?: tunnelConf.tunName,
				amQuick = configs.second.toAwgQuickString(true),
				wgQuick = configs.first.toWgQuickString(true),
			),
		)
	}

	fun cleanUpUninstalledApps(tunnelConfig: TunnelConf, packages: List<String>) = viewModelScope.launch(ioDispatcher) {
		runCatching {
			val amConfig = tunnelConfig.toAmConfig()
			val wgConfig = tunnelConfig.toWgConfig()
			val proxy = InterfaceProxy.Companion.from(amConfig.`interface`)
			if (proxy.includedApplications.isEmpty() && proxy.excludedApplications.isEmpty()) return@launch
			if (proxy.includedApplications.retainAll(packages.toSet()) || proxy.excludedApplications.retainAll(packages.toSet())) {
				updateTunnelConfig(tunnelConfig, amConfig = amConfig, wgConfig = wgConfig, `interface` = proxy)
				Timber.Forest.i("Removed split tunnel package for app that no longer exists on the device")
			}
		}.onFailure {
			Timber.Forest.e(it)
		}
	}

	private suspend fun rebuildConfigs(
		amConfig: Config,
		wgConfig: com.wireguard.config.Config,
		peers: List<PeerProxy>? = null,
		`interface`: InterfaceProxy? = null,
	): Pair<com.wireguard.config.Config, Config> = withContext(ioDispatcher) {
		Pair(
			com.wireguard.config.Config.Builder().apply {
				addPeers(peers?.map { it.toWgPeer() } ?: wgConfig.peers)
				setInterface(`interface`?.toWgInterface() ?: wgConfig.`interface`)
			}.build(),
			Config.Builder().apply {
				addPeers(peers?.map { it.toAmPeer() } ?: amConfig.peers)
				setInterface(`interface`?.toAmInterface() ?: amConfig.`interface`)
			}.build(),
		)
	}

	private suspend fun buildConfigs(peers: List<PeerProxy>, `interface`: InterfaceProxy): Pair<com.wireguard.config.Config, Config> =
		withContext(ioDispatcher) {
			Pair(
				com.wireguard.config.Config.Builder().apply {
					addPeers(peers.map { it.toWgPeer() })
					setInterface(`interface`.toWgInterface())
				}.build(),
				Config.Builder().apply {
					addPeers(peers.map { it.toAmPeer() })
					setInterface(`interface`.toAmInterface())
				}.build(),
			)
		}
}
