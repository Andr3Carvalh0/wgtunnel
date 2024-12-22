package com.zaneschepke.wireguardautotunnel.ui.screens.tunneloptions.config

import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zaneschepke.wireguardautotunnel.R
import com.zaneschepke.wireguardautotunnel.ui.AppUiState
import com.zaneschepke.wireguardautotunnel.ui.AppViewModel
import com.zaneschepke.wireguardautotunnel.ui.common.config.ConfigurationTextBox
import com.zaneschepke.wireguardautotunnel.ui.common.config.ConfigurationToggle
import com.zaneschepke.wireguardautotunnel.ui.common.label.GroupLabel
import com.zaneschepke.wireguardautotunnel.ui.common.navigation.LocalNavController
import com.zaneschepke.wireguardautotunnel.ui.common.navigation.TopNavBar
import com.zaneschepke.wireguardautotunnel.ui.common.prompt.AuthorizationPrompt
import com.zaneschepke.wireguardautotunnel.ui.common.snackbar.SnackbarController
import com.zaneschepke.wireguardautotunnel.ui.screens.tunneloptions.config.model.InterfaceProxy
import com.zaneschepke.wireguardautotunnel.ui.screens.tunneloptions.config.model.PeerProxy
import com.zaneschepke.wireguardautotunnel.util.Constants
import com.zaneschepke.wireguardautotunnel.util.extensions.scaledHeight
import com.zaneschepke.wireguardautotunnel.util.extensions.scaledWidth
import kotlinx.coroutines.launch
import org.amnezia.awg.crypto.KeyPair

@Composable
fun ConfigScreen(appUiState: AppUiState, appViewModel: AppViewModel, tunnelId: Int) {
	val context = LocalContext.current
	val snackbar = SnackbarController.current
	val clipboardManager: ClipboardManager = LocalClipboardManager.current
	val keyboardController = LocalSoftwareKeyboardController.current
	val navController = LocalNavController.current
	val scope = rememberCoroutineScope()

	val popBackStack by appViewModel.popBackStack.collectAsStateWithLifecycle(false)

	val tunnelConfig by remember {
		derivedStateOf {
			appUiState.tunnels.first { it.id == tunnelId }
		}
	}

	val configPair by remember {
		derivedStateOf {
			Pair(tunnelConfig.name, tunnelConfig.toAmConfig())
		}
	}

	var tunnelName by remember {
		mutableStateOf(configPair.first)
	}

	var interfaceState by remember {
		mutableStateOf(InterfaceProxy.from(configPair.second.`interface`))
	}

	var showAmneziaValues by remember {
		mutableStateOf(configPair.second.`interface`.junkPacketCount.isPresent)
	}

	var showScripts by remember {
		mutableStateOf(false)
	}

	val peersState = remember {
		configPair.second.peers.map { PeerProxy.from(it) }.toMutableStateList()
	}

	var showAuthPrompt by remember { mutableStateOf(false) }
	var isAuthenticated by remember { mutableStateOf(false) }

	val keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() })
	val keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)

	if (showAuthPrompt) {
		AuthorizationPrompt(
			onSuccess = {
				showAuthPrompt = false
				isAuthenticated = true
			},
			onError = {
				showAuthPrompt = false
				snackbar.showMessage(
					context.getString(R.string.error_authentication_failed),
				)
			},
			onFailure = {
				showAuthPrompt = false
				snackbar.showMessage(
					context.getString(R.string.error_authorization_failed),
				)
			},
		)
	}

	LaunchedEffect(popBackStack) {
		if (popBackStack) navController.popBackStack()
	}

	Scaffold(
		topBar = {
			TopNavBar(stringResource(R.string.edit_tunnel), trailing = {
				IconButton(onClick = {
					appViewModel.saveConfigChanges(
						tunnelConfig.copy(
							name = tunnelName,
						),
						peers = peersState,
						`interface` = interfaceState,
					)
				}) {
					val icon = Icons.Outlined.Save
					Icon(
						imageVector = icon,
						contentDescription = icon.name,
					)
				}
			})
		},
	) { padding ->
		Column(
			horizontalAlignment = Alignment.Start,
			verticalArrangement = Arrangement.spacedBy(24.dp.scaledHeight(), Alignment.Top),
			modifier =
			Modifier
				.fillMaxSize()
				.padding(padding)
				.verticalScroll(rememberScrollState())
				.padding(top = 24.dp.scaledHeight())
				.padding(horizontal = 24.dp.scaledWidth()),
		) {
			Surface(
				shape = RoundedCornerShape(12.dp),
				color = MaterialTheme.colorScheme.surface,
			) {
				Column(
					horizontalAlignment = Alignment.Start,
					verticalArrangement = Arrangement.spacedBy(5.dp, Alignment.Top),
					modifier = Modifier
						.padding(16.dp.scaledWidth())
						.focusGroup(),
				) {
					GroupLabel(
						stringResource(R.string.interface_),
					)
					ConfigurationToggle(
						stringResource(id = R.string.show_amnezia_properties),
						checked = showAmneziaValues,
						onCheckChanged = {
							if (appUiState.settings.isKernelEnabled) {
								snackbar.showMessage(context.getString(R.string.amnezia_kernel_message))
							} else {
								showAmneziaValues = it
							}
						},
					)
					ConfigurationToggle(
						stringResource(id = R.string.show_scripts),
						checked = showScripts,
						onCheckChanged = { checked ->
							if (appUiState.settings.isKernelEnabled) {
								showScripts = checked
							} else {
								scope.launch {
									appViewModel.requestRoot().onSuccess {
										showScripts = checked
									}
								}
							}
						},
					)
					ConfigurationTextBox(
						value = tunnelName,
						onValueChange = { tunnelName = it },
						keyboardActions = keyboardActions,
						label = stringResource(R.string.name),
						hint = stringResource(R.string.tunnel_name).lowercase(),
						modifier =
						Modifier
							.fillMaxWidth(),
					)
					OutlinedTextField(
						textStyle = MaterialTheme.typography.labelLarge,
						modifier =
						Modifier
							.fillMaxWidth()
							.clickable { showAuthPrompt = true },
						value = interfaceState.privateKey,
						visualTransformation =
						if ((tunnelId == Constants.MANUAL_TUNNEL_CONFIG_ID) || isAuthenticated) {
							VisualTransformation.None
						} else {
							PasswordVisualTransformation()
						},
						enabled = (tunnelId == Constants.MANUAL_TUNNEL_CONFIG_ID) || isAuthenticated,
						onValueChange = { interfaceState = interfaceState.copy(privateKey = it) },
						trailingIcon = {
							IconButton(
								enabled = isAuthenticated,
								modifier = Modifier.focusRequester(FocusRequester.Default),
								onClick = {
									val keypair = KeyPair()
									interfaceState = interfaceState.copy(
										privateKey = keypair.privateKey.toBase64(),
										publicKey = keypair.publicKey.toBase64(),
									)
								},
							) {
								Icon(
									Icons.Rounded.Refresh,
									stringResource(R.string.rotate_keys),
									tint = if (isAuthenticated) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
								)
							}
						},
						label = { Text(stringResource(R.string.private_key)) },
						singleLine = true,
						placeholder = {
							Text(
								stringResource(R.string.base64_key),
								style = MaterialTheme.typography.labelLarge,
								color = MaterialTheme.colorScheme.outline,
							)
						},
						keyboardOptions = keyboardOptions,
						keyboardActions = keyboardActions,
					)
					OutlinedTextField(
						textStyle = MaterialTheme.typography.labelLarge,
						modifier =
						Modifier
							.fillMaxWidth()
							.focusRequester(FocusRequester.Default),
						value = interfaceState.publicKey,
						enabled = false,
						onValueChange = {
							interfaceState = interfaceState.copy(publicKey = it)
						},
						trailingIcon = {
							IconButton(
								modifier = Modifier.focusRequester(FocusRequester.Default),
								onClick = {
									clipboardManager.setText(
										AnnotatedString(interfaceState.publicKey),
									)
								},
							) {
								Icon(
									Icons.Rounded.ContentCopy,
									stringResource(R.string.copy_public_key),
									tint = MaterialTheme.colorScheme.onSurface,
								)
							}
						},
						label = { Text(stringResource(R.string.public_key)) },
						singleLine = true,
						placeholder = {
							Text(
								stringResource(R.string.base64_key),
								style = MaterialTheme.typography.labelLarge,
								color = MaterialTheme.colorScheme.outline,
							)
						},
						keyboardOptions = keyboardOptions,
						keyboardActions = keyboardActions,
					)
					ConfigurationTextBox(
						value = interfaceState.addresses,
						onValueChange = {
							interfaceState = interfaceState.copy(addresses = it)
						},
						keyboardActions = keyboardActions,
						label = stringResource(R.string.addresses),
						hint = stringResource(R.string.comma_separated_list),
						modifier =
						Modifier
							.fillMaxWidth()
							.padding(end = 5.dp),
					)
					ConfigurationTextBox(
						value = interfaceState.listenPort,
						onValueChange = {
							interfaceState = interfaceState.copy(listenPort = it)
						},
						keyboardActions = keyboardActions,
						label = stringResource(R.string.listen_port),
						hint = stringResource(R.string.random),
						modifier = Modifier.fillMaxWidth(),
					)
					Row(
						modifier = Modifier.fillMaxWidth(),
						horizontalArrangement = Arrangement.spacedBy(5.dp),
					) {
						ConfigurationTextBox(
							value = interfaceState.dnsServers,
							onValueChange = {
								interfaceState = interfaceState.copy(dnsServers = it)
							},
							keyboardActions = keyboardActions,
							label = stringResource(R.string.dns_servers),
							hint = stringResource(R.string.comma_separated_list),
							modifier =
							Modifier
								.fillMaxWidth(3 / 5f)
								.padding(end = 5.dp),
						)
						ConfigurationTextBox(
							value = interfaceState.mtu,
							onValueChange = {
								interfaceState = interfaceState.copy(mtu = it)
							},
							keyboardActions = keyboardActions,
							label = stringResource(R.string.mtu),
							hint = stringResource(R.string.auto),
							modifier = Modifier.width(IntrinsicSize.Min),
						)
					}
					if (showScripts) {
						ConfigurationTextBox(
							value = interfaceState.preUp,
							onValueChange = {
								interfaceState = interfaceState.copy(preUp = it)
							},
							keyboardActions = keyboardActions,
							label = stringResource(R.string.pre_up),
							hint = stringResource(R.string.comma_separated_list).lowercase(),
							modifier = Modifier.fillMaxWidth(),
						)
						ConfigurationTextBox(
							value = interfaceState.postUp,
							onValueChange = {
								interfaceState = interfaceState.copy(postUp = it)
							},
							keyboardActions = keyboardActions,
							label = stringResource(R.string.post_up),
							hint = stringResource(R.string.comma_separated_list).lowercase(),
							modifier = Modifier.fillMaxWidth(),
						)
						ConfigurationTextBox(
							value = interfaceState.preDown,
							onValueChange = {
								interfaceState = interfaceState.copy(preDown = it)
							},
							keyboardActions = keyboardActions,
							label = stringResource(R.string.pre_down),
							hint = stringResource(R.string.comma_separated_list).lowercase(),
							modifier = Modifier.fillMaxWidth(),
						)
						ConfigurationTextBox(
							value = interfaceState.postDown,
							onValueChange = {
								interfaceState = interfaceState.copy(postDown = it)
							},
							keyboardActions = keyboardActions,
							label = stringResource(R.string.post_down),
							hint = stringResource(R.string.comma_separated_list).lowercase(),
							modifier = Modifier.fillMaxWidth(),
						)
					}
					if (showAmneziaValues) {
						ConfigurationTextBox(
							value = interfaceState.junkPacketCount,
							onValueChange = {
								interfaceState = interfaceState.copy(junkPacketCount = it)
							},
							keyboardActions = keyboardActions,
							label = stringResource(R.string.junk_packet_count),
							hint = stringResource(R.string.junk_packet_count).lowercase(),
							modifier =
							Modifier
								.fillMaxWidth(),
						)
						ConfigurationTextBox(
							value = interfaceState.junkPacketMinSize,
							onValueChange = {
								interfaceState = interfaceState.copy(junkPacketMinSize = it)
							},
							keyboardActions = keyboardActions,
							label = stringResource(R.string.junk_packet_minimum_size),
							hint =
							stringResource(
								R.string.junk_packet_minimum_size,
							).lowercase(),
							modifier =
							Modifier
								.fillMaxWidth(),
						)
						ConfigurationTextBox(
							value = interfaceState.junkPacketMaxSize,
							onValueChange = {
								interfaceState = interfaceState.copy(junkPacketMaxSize = it)
							},
							keyboardActions = keyboardActions,
							label = stringResource(R.string.junk_packet_maximum_size),
							hint =
							stringResource(
								R.string.junk_packet_maximum_size,
							).lowercase(),
							modifier =
							Modifier
								.fillMaxWidth(),
						)
						ConfigurationTextBox(
							value = interfaceState.initPacketJunkSize,
							onValueChange = {
								interfaceState = interfaceState.copy(initPacketJunkSize = it)
							},
							keyboardActions = keyboardActions,
							label = stringResource(R.string.init_packet_junk_size),
							hint = stringResource(R.string.init_packet_junk_size).lowercase(),
							modifier =
							Modifier
								.fillMaxWidth(),
						)
						ConfigurationTextBox(
							value = interfaceState.responsePacketJunkSize,
							onValueChange = {
								interfaceState = interfaceState.copy(responsePacketJunkSize = it)
							},
							keyboardActions = keyboardActions,
							label = stringResource(R.string.response_packet_junk_size),
							hint =
							stringResource(
								R.string.response_packet_junk_size,
							).lowercase(),
							modifier =
							Modifier
								.fillMaxWidth(),
						)
						ConfigurationTextBox(
							value = interfaceState.initPacketMagicHeader,
							onValueChange = {
								interfaceState = interfaceState.copy(initPacketMagicHeader = it)
							},
							keyboardActions = keyboardActions,
							label = stringResource(R.string.init_packet_magic_header),
							hint =
							stringResource(
								R.string.init_packet_magic_header,
							).lowercase(),
							modifier =
							Modifier
								.fillMaxWidth(),
						)
						ConfigurationTextBox(
							value = interfaceState.responsePacketMagicHeader,
							onValueChange = {
								interfaceState = interfaceState.copy(responsePacketMagicHeader = it)
							},
							keyboardActions = keyboardActions,
							label = stringResource(R.string.response_packet_magic_header),
							hint =
							stringResource(
								R.string.response_packet_magic_header,
							).lowercase(),
							modifier =
							Modifier
								.fillMaxWidth(),
						)
						ConfigurationTextBox(
							value = interfaceState.underloadPacketMagicHeader,
							onValueChange = {
								interfaceState = interfaceState.copy(underloadPacketMagicHeader = it)
							},
							keyboardActions = keyboardActions,
							label = stringResource(R.string.underload_packet_magic_header),
							hint =
							stringResource(
								R.string.underload_packet_magic_header,
							).lowercase(),
							modifier =
							Modifier
								.fillMaxWidth(),
						)
						ConfigurationTextBox(
							value = interfaceState.transportPacketMagicHeader,
							onValueChange = {
								interfaceState = interfaceState.copy(transportPacketMagicHeader = it)
							},
							keyboardActions = keyboardActions,
							label = stringResource(R.string.transport_packet_magic_header),
							hint =
							stringResource(
								R.string.transport_packet_magic_header,
							).lowercase(),
							modifier =
							Modifier
								.fillMaxWidth(),
						)
					}
				}
			}
			peersState.forEachIndexed { index, peer ->
				Surface(
					shape = RoundedCornerShape(12.dp),
					color = MaterialTheme.colorScheme.surface,
				) {
					Column(
						horizontalAlignment = Alignment.Start,
						verticalArrangement = Arrangement.spacedBy(5.dp, Alignment.Top),
						modifier = Modifier
							.padding(16.dp.scaledWidth())
							.focusGroup(),
					) {
						Row(
							horizontalArrangement = Arrangement.SpaceBetween,
							verticalAlignment = Alignment.CenterVertically,
							modifier =
							Modifier.fillMaxWidth(),
						) {
							GroupLabel(
								stringResource(R.string.peer),
							)
							IconButton(onClick = {
								peersState.removeAt(index)
							}) {
								val icon = Icons.Rounded.Delete
								Icon(icon, icon.name)
							}
						}

						ConfigurationTextBox(
							value = peer.publicKey,
							onValueChange = { value ->
								peersState[index] = peersState[index].copy(publicKey = value)
							},
							keyboardActions = keyboardActions,
							label = stringResource(R.string.public_key),
							hint = stringResource(R.string.base64_key),
							modifier = Modifier.fillMaxWidth(),
						)
						ConfigurationTextBox(
							value = peer.preSharedKey,
							onValueChange = { value ->
								peersState[index] = peersState[index].copy(preSharedKey = value)
							},
							keyboardActions = keyboardActions,
							label = stringResource(R.string.preshared_key),
							hint = stringResource(R.string.optional),
							modifier = Modifier.fillMaxWidth(),
						)
						OutlinedTextField(
							textStyle = MaterialTheme.typography.labelLarge,
							modifier = Modifier.fillMaxWidth(),
							value = peer.persistentKeepalive,
							enabled = true,
							onValueChange = { value ->
								peersState[index] = peersState[index].copy(persistentKeepalive = value)
							},
							trailingIcon = {
								Text(
									stringResource(R.string.seconds),
									modifier = Modifier.padding(end = 10.dp),
									style = MaterialTheme.typography.labelMedium,
								)
							},
							label = { Text(stringResource(R.string.persistent_keepalive), style = MaterialTheme.typography.labelMedium) },
							singleLine = true,
							placeholder = {
								Text(stringResource(R.string.optional_no_recommend), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.outline)
							},
							keyboardOptions = keyboardOptions,
							keyboardActions = keyboardActions,
						)
						ConfigurationTextBox(
							value = peer.endpoint,
							onValueChange = { value ->
								peersState[index] = peersState[index].copy(endpoint = value)
							},
							keyboardActions = keyboardActions,
							label = stringResource(R.string.endpoint),
							hint = stringResource(R.string.endpoint).lowercase(),
							modifier = Modifier.fillMaxWidth(),
						)
						OutlinedTextField(
							textStyle = MaterialTheme.typography.labelLarge,
							modifier = Modifier.fillMaxWidth(),
							value = peer.allowedIps,
							enabled = true,
							onValueChange = { value ->
								peersState[index] = peersState[index].copy(allowedIps = value)
							},
							label = { Text(stringResource(R.string.allowed_ips)) },
							singleLine = true,
							placeholder = {
								Text(stringResource(R.string.comma_separated_list), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.outline)
							},
							keyboardOptions = keyboardOptions,
							keyboardActions = keyboardActions,
						)
					}
				}
			}
			Row(
				horizontalArrangement = Arrangement.SpaceEvenly,
				verticalAlignment = Alignment.CenterVertically,
				modifier =
				Modifier
					.fillMaxSize()
					.padding(bottom = 140.dp),
			) {
				Row(
					verticalAlignment = Alignment.CenterVertically,
					horizontalArrangement = Arrangement.Center,
				) {
					TextButton(onClick = {
						peersState.add(PeerProxy())
					}) {
						Text(stringResource(R.string.add_peer))
					}
				}
			}
		}
	}
}