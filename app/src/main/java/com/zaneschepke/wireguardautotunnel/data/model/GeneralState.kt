package com.zaneschepke.wireguardautotunnel.data.model

import com.zaneschepke.wireguardautotunnel.domain.entity.AppState
import com.zaneschepke.wireguardautotunnel.ui.theme.Theme

internal data class GeneralState(
	val isLocationDisclosureShown: Boolean = LOCATION_DISCLOSURE_SHOWN_DEFAULT,
	val isBatteryOptimizationDisableShown: Boolean = BATTERY_OPTIMIZATION_DISABLE_SHOWN_DEFAULT,
	val isTunnelStatsExpanded: Boolean = IS_TUNNEL_STATS_EXPANDED,
	val theme: Theme = Theme.AUTOMATIC,
) {
	companion object {
		fun from(appState: AppState): GeneralState = with(appState) {
			GeneralState(
				isLocationDisclosureShown,
				isBatteryOptimizationDisableShown,
				isTunnelStatsExpanded,
				theme,
			)
		}

		const val LOCATION_DISCLOSURE_SHOWN_DEFAULT = false
		const val BATTERY_OPTIMIZATION_DISABLE_SHOWN_DEFAULT = false
		const val IS_TUNNEL_STATS_EXPANDED = false
	}
}
