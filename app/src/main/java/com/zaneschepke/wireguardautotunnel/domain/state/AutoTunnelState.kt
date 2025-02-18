package com.zaneschepke.wireguardautotunnel.domain.state

import com.zaneschepke.wireguardautotunnel.domain.events.KillSwitchEvent
import com.zaneschepke.wireguardautotunnel.domain.entity.AppSettings
import com.zaneschepke.wireguardautotunnel.domain.entity.TunnelConf
import com.zaneschepke.wireguardautotunnel.domain.events.AutoTunnelEvent
import com.zaneschepke.wireguardautotunnel.util.extensions.isMatchingToWildcardList

internal data class AutoTunnelState(
	val activeTunnels: List<TunnelConf> = emptyList(),
	val networkState: NetworkState = NetworkState(),
	val settings: AppSettings = AppSettings(),
	val tunnels: List<TunnelConf> = emptyList(),
) {

	private fun isMobileDataActive(): Boolean = !networkState.isEthernetConnected && !networkState.isWifiConnected && networkState.isMobileDataConnected

	private fun isMobileTunnelDataChangeNeeded(): Boolean {
		val preferredTunnel = preferredMobileDataTunnel()
		return preferredTunnel != null &&
			activeTunnels.isNotEmpty() &&
			!activeTunnels.any { it.id == preferredTunnel.id }
	}

	private fun isEthernetTunnelChangeNeeded(): Boolean {
		val preferredTunnel = preferredEthernetTunnel()
		return preferredTunnel != null && activeTunnels.isNotEmpty() && !activeTunnels.any { it.id == preferredTunnel.id }
	}

	private fun preferredMobileDataTunnel(): TunnelConf? = tunnels.firstOrNull { it.isMobileDataTunnel } ?: tunnels.firstOrNull { it.isPrimaryTunnel }

	private fun preferredEthernetTunnel(): TunnelConf? = tunnels.firstOrNull { it.isEthernetTunnel } ?: tunnels.firstOrNull { it.isPrimaryTunnel }

	private fun preferredWifiTunnel(): TunnelConf? = getTunnelWithMatchingTunnelNetwork() ?: tunnels.firstOrNull { it.isPrimaryTunnel }

	private fun isWifiActive(): Boolean = !networkState.isEthernetConnected && networkState.isWifiConnected

	private fun startOnEthernet(): Boolean = networkState.isEthernetConnected && settings.isTunnelOnEthernetEnabled && activeTunnels.isEmpty()

	private fun stopOnEthernet(): Boolean = networkState.isEthernetConnected && !settings.isTunnelOnEthernetEnabled && activeTunnels.isNotEmpty()

	// TODO test removed kill switch state check
	private fun stopKillSwitchOnTrusted(): Boolean =
		networkState.isWifiConnected && settings.isVpnKillSwitchEnabled && settings.isDisableKillSwitchOnTrustedEnabled && isCurrentSSIDTrusted()

	// TODO test, removed kill switch state check
	private fun startKillSwitch(): Boolean =
		settings.isVpnKillSwitchEnabled && (!settings.isDisableKillSwitchOnTrustedEnabled || !isCurrentSSIDTrusted())

	fun isNoConnectivity(): Boolean = !networkState.isEthernetConnected && !networkState.isWifiConnected && !networkState.isMobileDataConnected

	private fun stopOnMobileData(): Boolean = isMobileDataActive() && !settings.isTunnelOnMobileDataEnabled && activeTunnels.isNotEmpty()

	private fun startOnMobileData(): Boolean = isMobileDataActive() && settings.isTunnelOnMobileDataEnabled && activeTunnels.isEmpty()

	private fun changeOnMobileData(): Boolean = isMobileDataActive() && settings.isTunnelOnMobileDataEnabled && isMobileTunnelDataChangeNeeded()

	private fun changeOnEthernet(): Boolean = networkState.isEthernetConnected && settings.isTunnelOnEthernetEnabled && isEthernetTunnelChangeNeeded()

	private fun stopOnWifi(): Boolean = isWifiActive() && !settings.isTunnelOnWifiEnabled && activeTunnels.isNotEmpty()

	private fun stopOnTrustedWifi(): Boolean = isWifiActive() && settings.isTunnelOnWifiEnabled && activeTunnels.isNotEmpty() && isCurrentSSIDTrusted()

	private fun startOnUntrustedWifi(): Boolean = isWifiActive() && settings.isTunnelOnWifiEnabled && activeTunnels.isEmpty() && !isCurrentSSIDTrusted()

	private fun changeOnUntrustedWifi(): Boolean =
		isWifiActive() && settings.isTunnelOnWifiEnabled && activeTunnels.isNotEmpty() && !isCurrentSSIDTrusted() && !isWifiTunnelPreferred()

	private fun isWifiTunnelPreferred(): Boolean {
		val preferred = preferredWifiTunnel()
		return activeTunnels.any { it.id == preferred?.id }
	}

	fun asAutoTunnelEvent(): AutoTunnelEvent = when {
		// ethernet scenarios
		stopOnEthernet() -> AutoTunnelEvent.Stop
		startOnEthernet() || changeOnEthernet() -> AutoTunnelEvent.Start(preferredEthernetTunnel())
		// mobile data scenarios
		stopOnMobileData() -> AutoTunnelEvent.Stop
		startOnMobileData() || changeOnMobileData() -> AutoTunnelEvent.Start(preferredMobileDataTunnel())
		// wifi scenarios
		stopOnWifi() -> AutoTunnelEvent.Stop
		stopOnTrustedWifi() -> AutoTunnelEvent.Stop
		startOnUntrustedWifi() || changeOnUntrustedWifi() -> AutoTunnelEvent.Start(preferredWifiTunnel())
		// no connectivity
		isNoConnectivity() && settings.isStopOnNoInternetEnabled -> AutoTunnelEvent.Stop
		else -> AutoTunnelEvent.DoNothing
	}

	fun asKillSwitchEvent(): KillSwitchEvent = when {
		stopKillSwitchOnTrusted() -> KillSwitchEvent.Stop
		startKillSwitch() -> {
			val allowedIps = if (settings.isLanOnKillSwitchEnabled) TunnelConf.LAN_BYPASS_ALLOWED_IPS else emptyList()
			KillSwitchEvent.Start(allowedIps)
		}
		else -> KillSwitchEvent.DoNothing
	}

	private fun isCurrentSSIDTrusted(): Boolean = networkState.wifiName?.let {
		hasTrustedWifiName(it)
	} == true

	private fun hasTrustedWifiName(wifiName: String, wifiNames: List<String> = settings.trustedNetworkSSIDs): Boolean =
		if (settings.isWildcardsEnabled) {
			wifiNames.isMatchingToWildcardList(wifiName)
		} else {
			wifiNames.contains(wifiName)
		}

	private fun getTunnelWithMatchingTunnelNetwork(): TunnelConf? = networkState.wifiName?.let { wifiName ->
		tunnels.firstOrNull {
			hasTrustedWifiName(wifiName, it.tunnelNetworks)
		}
	}
}
