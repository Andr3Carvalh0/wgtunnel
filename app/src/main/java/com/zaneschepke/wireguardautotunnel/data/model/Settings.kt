package com.zaneschepke.wireguardautotunnel.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.zaneschepke.wireguardautotunnel.domain.entity.AppSettings

@Entity
internal data class Settings(
	@PrimaryKey(autoGenerate = true) val id: Int = 0,
	@ColumnInfo(name = "is_tunnel_enabled") val isAutoTunnelEnabled: Boolean = false,
	@ColumnInfo(name = "is_tunnel_on_mobile_data_enabled")
	val isTunnelOnMobileDataEnabled: Boolean = false,
	@ColumnInfo(name = "trusted_network_ssids")
	val trustedNetworkSSIDs: MutableList<String> = mutableListOf(),
	@ColumnInfo(name = "is_always_on_vpn_enabled") val isAlwaysOnVpnEnabled: Boolean = false,
	@ColumnInfo(name = "is_tunnel_on_ethernet_enabled")
	val isTunnelOnEthernetEnabled: Boolean = false,
	@ColumnInfo(
		name = "is_shortcuts_enabled",
		defaultValue = "false",
	)
	val isShortcutsEnabled: Boolean = false,
	@ColumnInfo(
		name = "is_tunnel_on_wifi_enabled",
		defaultValue = "false",
	)
	val isTunnelOnWifiEnabled: Boolean = false,
	@ColumnInfo(
		name = "is_restore_on_boot_enabled",
		defaultValue = "false",
	)
	val isRestoreOnBootEnabled: Boolean = false,
	@ColumnInfo(
		name = "is_multi_tunnel_enabled",
		defaultValue = "false",
	)
	val isMultiTunnelEnabled: Boolean = false,
	@ColumnInfo(
		name = "is_ping_enabled",
		defaultValue = "false",
	)
	val isPingEnabled: Boolean = false,
	@ColumnInfo(
		name = "is_amnezia_enabled",
		defaultValue = "false",
	)
	val isAmneziaEnabled: Boolean = false,
	@ColumnInfo(
		name = "is_wildcards_enabled",
		defaultValue = "false",
	)
	val isWildcardsEnabled: Boolean = false,
	@ColumnInfo(
		name = "is_stop_on_no_internet_enabled",
		defaultValue = "false",
	)
	val isStopOnNoInternetEnabled: Boolean = false,
	@ColumnInfo(
		name = "is_vpn_kill_switch_enabled",
		defaultValue = "false",
	)
	val isVpnKillSwitchEnabled: Boolean = false,
	@ColumnInfo(
		name = "is_lan_on_kill_switch_enabled",
		defaultValue = "false",
	)
	val isLanOnKillSwitchEnabled: Boolean = false,
	@ColumnInfo(
		name = "debounce_delay_seconds",
		defaultValue = "3",
	)
	val debounceDelaySeconds: Int = 3,
	@ColumnInfo(
		name = "is_disable_kill_switch_on_trusted_enabled",
		defaultValue = "false",
	)
	val isDisableKillSwitchOnTrustedEnabled: Boolean = false,
) {

	fun toAppSettings(): AppSettings = AppSettings(
		id = id,
		isAutoTunnelEnabled = isAutoTunnelEnabled,
		isTunnelOnMobileDataEnabled = isTunnelOnMobileDataEnabled,
		trustedNetworkSSIDs = trustedNetworkSSIDs,
		isAlwaysOnVpnEnabled = isAlwaysOnVpnEnabled,
		isTunnelOnEthernetEnabled = isTunnelOnEthernetEnabled,
		isShortcutsEnabled = isShortcutsEnabled,
		isTunnelOnWifiEnabled = isTunnelOnWifiEnabled,
		isRestoreOnBootEnabled = isRestoreOnBootEnabled,
		isMultiTunnelEnabled = isMultiTunnelEnabled,
		isPingEnabled = isPingEnabled,
		isAmneziaEnabled = isAmneziaEnabled,
		isWildcardsEnabled = isWildcardsEnabled,
		isStopOnNoInternetEnabled = isStopOnNoInternetEnabled,
		isVpnKillSwitchEnabled = isVpnKillSwitchEnabled,
		isLanOnKillSwitchEnabled = isLanOnKillSwitchEnabled,
		debounceDelaySeconds = debounceDelaySeconds,
		isDisableKillSwitchOnTrustedEnabled = isDisableKillSwitchOnTrustedEnabled,
	)

	companion object {
		fun from(appSettings: AppSettings): Settings = with(appSettings) {
			Settings(
				id = id,
				isAutoTunnelEnabled = isAutoTunnelEnabled,
				isTunnelOnMobileDataEnabled = isTunnelOnMobileDataEnabled,
				trustedNetworkSSIDs = trustedNetworkSSIDs.toMutableList(),
				isAlwaysOnVpnEnabled = isAlwaysOnVpnEnabled,
				isTunnelOnEthernetEnabled = isTunnelOnEthernetEnabled,
				isShortcutsEnabled = isShortcutsEnabled,
				isTunnelOnWifiEnabled = isTunnelOnWifiEnabled,
				isRestoreOnBootEnabled = isRestoreOnBootEnabled,
				isMultiTunnelEnabled = isMultiTunnelEnabled,
				isPingEnabled = isPingEnabled,
				isAmneziaEnabled = isAmneziaEnabled,
				isWildcardsEnabled = isWildcardsEnabled,
				isStopOnNoInternetEnabled = isStopOnNoInternetEnabled,
				isVpnKillSwitchEnabled = isVpnKillSwitchEnabled,
				isLanOnKillSwitchEnabled = isLanOnKillSwitchEnabled,
				debounceDelaySeconds = debounceDelaySeconds,
				isDisableKillSwitchOnTrustedEnabled = isDisableKillSwitchOnTrustedEnabled,
			)
		}
	}
}
