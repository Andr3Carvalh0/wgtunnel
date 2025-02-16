package com.zaneschepke.wireguardautotunnel.ui.screens.main.components

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Circle
import androidx.compose.material.icons.rounded.CopyAll
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SettingsEthernet
import androidx.compose.material.icons.rounded.Smartphone
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.zaneschepke.wireguardautotunnel.domain.entity.TunnelConf
import com.zaneschepke.wireguardautotunnel.domain.state.TunnelState
import com.zaneschepke.wireguardautotunnel.ui.Route
import com.zaneschepke.wireguardautotunnel.ui.common.ExpandingRowListItem
import com.zaneschepke.wireguardautotunnel.ui.common.button.ScaledSwitch
import com.zaneschepke.wireguardautotunnel.ui.common.navigation.LocalNavController
import com.zaneschepke.wireguardautotunnel.util.extensions.asColor

@Composable
fun TunnelRowItem(
	isActive: Boolean,
	expanded: Boolean,
	isSelected: Boolean,
	tunnel: TunnelConf,
	tunnelState: TunnelState,
	onHold: () -> Unit,
	onClick: () -> Unit,
	onCopy: () -> Unit,
	onDelete: () -> Unit,
	onSwitchClick: (checked: Boolean) -> Unit,
) {
	val leadingIconColor = if (!isActive) Color.Gray else tunnelState.statistics.asColor()

	val navController = LocalNavController.current
	val haptic = LocalHapticFeedback.current
	val itemFocusRequester = remember { FocusRequester() }
	ExpandingRowListItem(
		leading = {
			val icon = when {
				tunnel.isPrimaryTunnel -> Icons.Rounded.Star
				tunnel.isMobileDataTunnel -> Icons.Rounded.Smartphone
				tunnel.isEthernetTunnel -> Icons.Rounded.SettingsEthernet
				else -> Icons.Rounded.Circle
			}
			Icon(
				icon,
				icon.name,
				tint = leadingIconColor,
				modifier = Modifier.size(16.dp),
			)
		},
		text = tunnel.tunName,
		onHold = {
			haptic.performHapticFeedback(HapticFeedbackType.LongPress)
			onHold()
		},
		onClick = {
			if (isActive) {
				onClick()
			}
		},
		isExpanded = expanded && isActive,
		expanded = { if (isActive && expanded) TunnelStatisticsRow(tunnelState.statistics, tunnel) },
		trailing = {
			if (isSelected) {
				Row {
					IconButton(
						onClick = {
							navController.navigate(
								Route.TunnelOptions(tunnel.id),
							)
						},
					) {
						val icon = Icons.Rounded.Settings
						Icon(
							icon,
							icon.name,
						)
					}
					IconButton(
						modifier = Modifier.focusable(),
						onClick = { onCopy() },
					) {
						val icon = Icons.Rounded.CopyAll
						Icon(icon, icon.name)
					}
					IconButton(
						enabled = !isActive,
						modifier = Modifier.focusable(),
						onClick = { onDelete() },
					) {
						val icon = Icons.Rounded.Delete
						Icon(icon, icon.name)
					}
				}
			} else {
				ScaledSwitch(
					modifier = Modifier.focusRequester(itemFocusRequester),
					checked = isActive,
					onClick = onSwitchClick,
				)
			}
		},
	)
}
