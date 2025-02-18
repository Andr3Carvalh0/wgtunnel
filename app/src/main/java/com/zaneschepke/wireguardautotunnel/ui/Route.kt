package com.zaneschepke.wireguardautotunnel.ui

import kotlinx.serialization.Serializable

internal sealed class Route {

	@Serializable
	data object Settings : Route()

	@Serializable
	data object AutoTunnel : Route()

	@Serializable
	data object AutoTunnelAdvanced : Route()

	@Serializable
	data object LocationDisclosure : Route()

	@Serializable
	data object Display : Route()

	@Serializable
	data object KillSwitch : Route()

	@Serializable
	data object Main : Route()

	@Serializable
	data class TunnelOptions(val id: Int) : Route()

	@Serializable
	data object Scanner : Route()

	@Serializable
	data class Config(val id: Int) : Route()

	@Serializable
	data class SplitTunnel(val id: Int) : Route()

	@Serializable
	data class TunnelAutoTunnel(val id: Int) : Route()
}
