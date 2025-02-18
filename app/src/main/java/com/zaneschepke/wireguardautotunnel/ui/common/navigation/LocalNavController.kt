package com.zaneschepke.wireguardautotunnel.ui.common.navigation

import androidx.compose.runtime.compositionLocalOf
import androidx.navigation.NavHostController

internal val LocalNavController = compositionLocalOf<NavHostController> {
	error("NavController was not provided")
}
