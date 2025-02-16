package com.zaneschepke.wireguardautotunnel.ui.screens.main.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.zaneschepke.wireguardautotunnel.R
import com.zaneschepke.wireguardautotunnel.ui.AppUiState
import com.zaneschepke.wireguardautotunnel.ui.common.ExpandingRowListItem
import com.zaneschepke.wireguardautotunnel.ui.common.button.ScaledSwitch
import com.zaneschepke.wireguardautotunnel.ui.theme.SilverTree

@Composable
fun AutoTunnelRowItem(appUiState: AppUiState, onToggle: () -> Unit) {
	ExpandingRowListItem(
		leading = {
			val icon = Icons.Rounded.Bolt
			Icon(
				icon,
				icon.name,
				modifier =
				Modifier
					.size(16.dp).scale(1.5f),
				tint =
				if (!appUiState.autoTunnelActive) {
					Color.Gray
				} else {
					SilverTree
				},
			)
		},
		text = stringResource(R.string.auto_tunneling),
		trailing = {
			ScaledSwitch(
				appUiState.settings.isAutoTunnelEnabled,
				onClick = {
					onToggle()
				},
			)
		},
		onClick = { },
		isExpanded = false,
	)
}
