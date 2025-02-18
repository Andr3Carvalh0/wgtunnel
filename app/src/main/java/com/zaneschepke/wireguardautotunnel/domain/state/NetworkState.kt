package com.zaneschepke.wireguardautotunnel.domain.state

internal data class NetworkState(
	val isWifiConnected: Boolean = false,
	val isMobileDataConnected: Boolean = false,
	val isEthernetConnected: Boolean = false,
	val wifiName: String? = null,
)
