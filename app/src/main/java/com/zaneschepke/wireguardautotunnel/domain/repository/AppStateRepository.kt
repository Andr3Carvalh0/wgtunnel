package com.zaneschepke.wireguardautotunnel.domain.repository

import com.zaneschepke.wireguardautotunnel.data.model.GeneralState
import com.zaneschepke.wireguardautotunnel.ui.theme.Theme
import kotlinx.coroutines.flow.Flow

internal interface AppStateRepository {

	suspend fun isLocationDisclosureShown(): Boolean

	suspend fun setLocationDisclosureShown(shown: Boolean)

	suspend fun isBatteryOptimizationDisableShown(): Boolean

	suspend fun setBatteryOptimizationDisableShown(shown: Boolean)

	suspend fun isTunnelStatsExpanded(): Boolean

	suspend fun setTunnelStatsExpanded(expanded: Boolean)

	suspend fun setTheme(theme: Theme)

	suspend fun getTheme(): Theme

	val flow: Flow<GeneralState>
}
