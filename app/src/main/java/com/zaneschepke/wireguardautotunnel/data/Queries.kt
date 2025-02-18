package com.zaneschepke.wireguardautotunnel.data

object Queries {
	fun createDefaultSettings(): String = """
		INSERT INTO Settings (is_tunnel_enabled,
		        is_tunnel_on_mobile_data_enabled,
		        trusted_network_ssids,
		        is_always_on_vpn_enabled,
		        is_tunnel_on_ethernet_enabled,
		        is_shortcuts_enabled,
		        is_tunnel_on_wifi_enabled,
		        is_restore_on_boot_enabled,
		        is_multi_tunnel_enabled)
		        VALUES
		        ('false',
		        'false',
		        '',
		        'false',
		        'false',
		        'false',
		        'false',
		        'false',
		        'false')
	""".trimIndent()
}
