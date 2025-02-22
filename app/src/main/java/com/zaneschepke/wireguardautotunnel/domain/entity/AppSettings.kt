package com.zaneschepke.wireguardautotunnel.domain.entity

internal data class AppSettings(
	val id: Int = 0,
	val isAutoTunnelEnabled: Boolean = false,
	val isTunnelOnMobileDataEnabled: Boolean = false,
	val trustedNetworkSSIDs: List<String> = emptyList(),
	val isAlwaysOnVpnEnabled: Boolean = false,
	val isTunnelOnEthernetEnabled: Boolean = false,
	val isShortcutsEnabled: Boolean = false,
	val isTunnelOnWifiEnabled: Boolean = false,
	val isRestoreOnBootEnabled: Boolean = false,
	val isMultiTunnelEnabled: Boolean = false,
	val isPingEnabled: Boolean = false,
	val isAmneziaEnabled: Boolean = false,
	val isWildcardsEnabled: Boolean = false,
	val isStopOnNoInternetEnabled: Boolean = false,
	val isVpnKillSwitchEnabled: Boolean = false,
	val isLanOnKillSwitchEnabled: Boolean = false,
	val debounceDelaySeconds: Int = 3,
	val isDisableKillSwitchOnTrustedEnabled: Boolean = false,
) {
	fun debounceDelayMillis(): Long = debounceDelaySeconds * 1000L
}
