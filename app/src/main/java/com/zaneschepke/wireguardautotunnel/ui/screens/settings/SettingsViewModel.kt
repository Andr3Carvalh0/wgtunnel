package com.zaneschepke.wireguardautotunnel.ui.screens.settings

import android.content.Context
import android.location.LocationManager
import androidx.core.location.LocationManagerCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wireguard.android.backend.WgQuickBackend
import com.wireguard.android.util.RootShell
import com.zaneschepke.wireguardautotunnel.R
import com.zaneschepke.wireguardautotunnel.data.domain.Settings
import com.zaneschepke.wireguardautotunnel.data.repository.AppDataRepository
import com.zaneschepke.wireguardautotunnel.module.IoDispatcher
import com.zaneschepke.wireguardautotunnel.service.foreground.ServiceManager
import com.zaneschepke.wireguardautotunnel.ui.common.snackbar.SnackbarController
import com.zaneschepke.wireguardautotunnel.util.Constants
import com.zaneschepke.wireguardautotunnel.util.FileUtils
import com.zaneschepke.wireguardautotunnel.util.StringValue
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Provider

@HiltViewModel
class SettingsViewModel
@Inject
constructor(
	private val appDataRepository: AppDataRepository,
	private val rootShell: Provider<RootShell>,
	private val fileUtils: FileUtils,
	@IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {

	private val _uiState = MutableStateFlow(SettingsUiState())
	val uiState = _uiState.onStart {
		_uiState.update {
			it.copy(isKernelAvailable = isKernelSupported(), isRooted = isRooted())
		}
	}.stateIn(
		viewModelScope,
		SharingStarted.WhileSubscribed(Constants.SUBSCRIPTION_TIMEOUT),
		SettingsUiState(),
	)
	private val settings = appDataRepository.settings.getSettingsFlow()
		.stateIn(viewModelScope, SharingStarted.Eagerly, Settings())

	fun onSaveTrustedSSID(ssid: String) = viewModelScope.launch {
		val trimmed = ssid.trim()
		with(settings.value) {
			if (!trustedNetworkSSIDs.contains(trimmed)) {
				this.trustedNetworkSSIDs.add(ssid)
				appDataRepository.settings.save(this)
			} else {
				SnackbarController.showMessage(
					StringValue.StringResource(
						R.string.error_ssid_exists,
					),
				)
			}
		}
	}

	fun setLocationDisclosureShown() = viewModelScope.launch {
		appDataRepository.appState.setLocationDisclosureShown(true)
	}

	fun setBatteryOptimizeDisableShown() = viewModelScope.launch {
		appDataRepository.appState.setBatteryOptimizationDisableShown(true)
	}

	fun onToggleTunnelOnMobileData() = viewModelScope.launch {
		with(settings.value) {
			appDataRepository.settings.save(
				copy(
					isTunnelOnMobileDataEnabled = !this.isTunnelOnMobileDataEnabled,
				),
			)
		}
	}

	fun onDeleteTrustedSSID(ssid: String) = viewModelScope.launch {
		with(settings.value) {
			appDataRepository.settings.save(
				copy(
					trustedNetworkSSIDs = (this.trustedNetworkSSIDs - ssid).toMutableList(),
				),
			)
		}
	}

	private fun exportTunnels(files: List<File>) = viewModelScope.launch {
		fileUtils.saveFilesToZip(files).onSuccess {
			SnackbarController.showMessage(StringValue.StringResource(R.string.exported_configs_message))
		}.onFailure {
			SnackbarController.showMessage(StringValue.StringResource(R.string.export_configs_failed))
		}
	}

	fun onToggleAutoTunnel(context: Context) = viewModelScope.launch {
		with(settings.value) {
			var isAutoTunnelPaused = this.isAutoTunnelPaused
			if (isAutoTunnelEnabled) {
				ServiceManager.stopWatcherService(context)
			} else {
				ServiceManager.startWatcherService(context)
				isAutoTunnelPaused = false
			}
			appDataRepository.settings.save(
				copy(
					isAutoTunnelEnabled = !isAutoTunnelEnabled,
					isAutoTunnelPaused = isAutoTunnelPaused,
				),
			)
		}
	}

	fun onToggleAlwaysOnVPN() = viewModelScope.launch {
		with(settings.value) {
			appDataRepository.settings.save(
				copy(
					isAlwaysOnVpnEnabled = !isAlwaysOnVpnEnabled,
				),
			)
		}
	}

	fun onToggleTunnelOnEthernet() = viewModelScope.launch {
		with(settings.value) {
			appDataRepository.settings.save(
				copy(
					isTunnelOnEthernetEnabled = !isTunnelOnEthernetEnabled,
				),
			)
		}
	}

	fun isLocationEnabled(context: Context): Boolean {
		val locationManager =
			context.getSystemService(
				Context.LOCATION_SERVICE,
			) as LocationManager
		return LocationManagerCompat.isLocationEnabled(locationManager)
	}

	fun onToggleShortcutsEnabled() = viewModelScope.launch {
		with(settings.value) {
			appDataRepository.settings.save(
				this.copy(
					isShortcutsEnabled = !isShortcutsEnabled,
				),
			)
		}
	}

	private fun saveKernelMode(enabled: Boolean) = viewModelScope.launch {
		with(settings.value) {
			appDataRepository.settings.save(
				this.copy(
					isKernelEnabled = enabled,
				),
			)
		}
	}

	fun onToggleTunnelOnWifi() = viewModelScope.launch {
		with(settings.value) {
			appDataRepository.settings.save(
				copy(
					isTunnelOnWifiEnabled = !isTunnelOnWifiEnabled,
				),
			)
		}
	}

	fun onToggleAmnezia() = viewModelScope.launch {
		with(settings.value) {
			if (isKernelEnabled) {
				saveKernelMode(false)
			}
			appDataRepository.settings.save(
				copy(
					isAmneziaEnabled = !isAmneziaEnabled,
				),
			)
		}
	}

	fun onToggleKernelMode() = viewModelScope.launch {
		with(settings.value) {
			if (!isKernelEnabled) {
				requestRoot().onSuccess {
					appDataRepository.settings.save(
						copy(
							isKernelEnabled = true,
							isAmneziaEnabled = false,
						),
					)
				}
			} else {
				saveKernelMode(enabled = false)
			}
		}
	}

	fun onToggleRestartOnPing() = viewModelScope.launch {
		with(settings.value) {
			appDataRepository.settings.save(
				copy(
					isPingEnabled = !isPingEnabled,
				),
			)
		}
	}

	private suspend fun isKernelSupported(): Boolean {
		return withContext(ioDispatcher) {
			WgQuickBackend.hasKernelSupport()
		}
	}

	fun onToggleRestartAtBoot() = viewModelScope.launch {
		with(settings.value) {
			appDataRepository.settings.save(
				copy(
					isRestoreOnBootEnabled = !isRestoreOnBootEnabled,
				),
			)
		}
	}

	private suspend fun isRooted(): Boolean {
		return try {
			withContext(ioDispatcher) {
				rootShell.get().start()
			}
			true
		} catch (_: Exception) {
			false
		}
	}

	private suspend fun requestRoot(): Result<Unit> {
		return withContext(ioDispatcher) {
			kotlin.runCatching {
				rootShell.get().start()
				SnackbarController.showMessage(StringValue.StringResource(R.string.root_accepted))
			}.onFailure {
				SnackbarController.showMessage(StringValue.StringResource(R.string.error_root_denied))
			}
		}
	}

	fun onRequestRoot() = viewModelScope.launch {
		requestRoot()
	}

	fun exportAllConfigs() = viewModelScope.launch {
		kotlin.runCatching {
			val tunnels = appDataRepository.tunnels.getAll()
			val wgFiles = fileUtils.createWgFiles(tunnels)
			val amFiles = fileUtils.createAmFiles(tunnels)
			exportTunnels(wgFiles + amFiles)
		}
	}
}
