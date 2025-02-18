package com.zaneschepke.wireguardautotunnel.domain.events

internal sealed class KillSwitchEvent {
	data class Start(val allowedIps: List<String>) : KillSwitchEvent()
	data object Stop : KillSwitchEvent()
	data object DoNothing : KillSwitchEvent()
}
