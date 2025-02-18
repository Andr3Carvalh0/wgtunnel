package com.zaneschepke.wireguardautotunnel.domain.enums

internal enum class TunnelStatus {
	UP,
	DOWN,
	;

	fun isDown(): Boolean = this == DOWN

	fun isUp(): Boolean = this == UP
}
