package com.zaneschepke.wireguardautotunnel.data.repository

import com.zaneschepke.wireguardautotunnel.data.DataStoreManager
import com.zaneschepke.wireguardautotunnel.domain.repository.AppStateRepository
import com.zaneschepke.wireguardautotunnel.data.model.GeneralState
import com.zaneschepke.wireguardautotunnel.ui.theme.Theme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber

internal class DataStoreAppStateRepository(private val dataStoreManager: DataStoreManager) : AppStateRepository {
	override suspend fun isLocationDisclosureShown(): Boolean = dataStoreManager.getFromStore(DataStoreManager.locationDisclosureShown)
		?: GeneralState.LOCATION_DISCLOSURE_SHOWN_DEFAULT

	override suspend fun setLocationDisclosureShown(shown: Boolean) {
		dataStoreManager.saveToDataStore(DataStoreManager.locationDisclosureShown, shown)
	}

	override suspend fun isBatteryOptimizationDisableShown(): Boolean = dataStoreManager.getFromStore(DataStoreManager.batteryDisableShown)
		?: GeneralState.BATTERY_OPTIMIZATION_DISABLE_SHOWN_DEFAULT

	override suspend fun setBatteryOptimizationDisableShown(shown: Boolean) {
		dataStoreManager.saveToDataStore(DataStoreManager.batteryDisableShown, shown)
	}

	override suspend fun isTunnelStatsExpanded(): Boolean = dataStoreManager.getFromStore(DataStoreManager.tunnelStatsExpanded)
		?: GeneralState.IS_TUNNEL_STATS_EXPANDED

	override suspend fun setTunnelStatsExpanded(expanded: Boolean) {
		dataStoreManager.saveToDataStore(DataStoreManager.tunnelStatsExpanded, expanded)
	}

	override suspend fun setTheme(theme: Theme) {
		dataStoreManager.saveToDataStore(DataStoreManager.theme, theme.name)
	}

	override suspend fun getTheme(): Theme = dataStoreManager.getFromStore(DataStoreManager.theme)?.let {
		try {
			Theme.valueOf(it)
		} catch (_: IllegalArgumentException) {
			Theme.AUTOMATIC
		}
	} ?: Theme.AUTOMATIC

	override val flow: Flow<GeneralState> =
		dataStoreManager.preferencesFlow.map { prefs ->
			prefs?.let { pref ->
				try {
					GeneralState(
						isLocationDisclosureShown = pref[DataStoreManager.locationDisclosureShown]
							?: GeneralState.LOCATION_DISCLOSURE_SHOWN_DEFAULT,
						isBatteryOptimizationDisableShown = pref[DataStoreManager.batteryDisableShown]
							?: GeneralState.BATTERY_OPTIMIZATION_DISABLE_SHOWN_DEFAULT,
						isTunnelStatsExpanded = pref[DataStoreManager.tunnelStatsExpanded] ?: GeneralState.IS_TUNNEL_STATS_EXPANDED,
						theme = getTheme(),
					)
				} catch (e: IllegalArgumentException) {
					Timber.e(e)
					GeneralState()
				}
			} ?: GeneralState()
		}
}
