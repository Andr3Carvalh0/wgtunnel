package com.zaneschepke.wireguardautotunnel.util.extensions

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.zaneschepke.wireguardautotunnel.WireGuardAutoTunnel
import com.zaneschepke.wireguardautotunnel.ui.Route
import com.zaneschepke.wireguardautotunnel.ui.common.navigation.isCurrentRoute

internal fun NavController.goFromRoot(route: Route) {
	if (currentBackStackEntry?.isCurrentRoute(route::class) == true) return
	this.navigate(route) {
		// Pop up to the start destination of the graph to
		// avoid building up a large stack of destinations
		// on the back stack as users select items
		popUpTo(graph.findStartDestination().id) {
			saveState = true
		}
		// Avoid multiple copies of the same destination when
		// reselecting the same item
		launchSingleTop = true
		restoreState = true
	}
}

internal fun Dp.scaledHeight(): Dp = WireGuardAutoTunnel.instance.resizeHeight(this)

internal fun Dp.scaledWidth(): Dp = WireGuardAutoTunnel.instance.resizeWidth(this)

internal fun TextUnit.scaled(): TextUnit = WireGuardAutoTunnel.instance.resizeHeight(this)
