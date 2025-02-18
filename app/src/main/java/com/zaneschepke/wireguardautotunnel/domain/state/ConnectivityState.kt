package com.zaneschepke.wireguardautotunnel.domain.state

internal data class ConnectivityState(val wifiAvailable: Boolean, val ethernetAvailable: Boolean, val cellularAvailable: Boolean) {
	val allOffline = !wifiAvailable && !ethernetAvailable && !cellularAvailable
}
