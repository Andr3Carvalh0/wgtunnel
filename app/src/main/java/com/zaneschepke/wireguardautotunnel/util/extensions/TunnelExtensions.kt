package com.zaneschepke.wireguardautotunnel.util.extensions

import androidx.compose.ui.graphics.Color
import com.wireguard.android.backend.BackendException
import com.wireguard.config.Peer
import com.zaneschepke.wireguardautotunnel.domain.enums.BackendError
import com.zaneschepke.wireguardautotunnel.domain.enums.BackendState
import com.zaneschepke.wireguardautotunnel.domain.enums.HandshakeStatus
import com.zaneschepke.wireguardautotunnel.domain.enums.TunnelStatus
import com.zaneschepke.wireguardautotunnel.domain.enums.TunnelStatus.DOWN
import com.zaneschepke.wireguardautotunnel.domain.enums.TunnelStatus.UP
import com.zaneschepke.wireguardautotunnel.domain.state.TunnelStatistics
import com.zaneschepke.wireguardautotunnel.ui.theme.SilverTree
import com.zaneschepke.wireguardautotunnel.ui.theme.Straw
import com.zaneschepke.wireguardautotunnel.util.Constants
import com.zaneschepke.wireguardautotunnel.util.NumberUtils
import org.amnezia.awg.backend.Backend
import org.amnezia.awg.backend.Tunnel
import org.amnezia.awg.config.Config
import timber.log.Timber
import java.net.InetAddress

internal fun TunnelStatistics.mapPeerStats(): Map<org.amnezia.awg.crypto.Key, TunnelStatistics.PeerStats?> = this.getPeers().associateWith { key ->
	(this.peerStats(key))
}

internal fun TunnelStatistics.PeerStats.latestHandshakeSeconds(): Long? =
	NumberUtils.getSecondsBetweenTimestampAndNow(this.latestHandshakeEpochMillis)

internal fun TunnelStatistics.PeerStats.handshakeStatus(): HandshakeStatus {
	// TODO add never connected status after duration
	return this.latestHandshakeSeconds().let {
		when {
			it == null -> HandshakeStatus.NOT_STARTED
			it <= HandshakeStatus.STALE_TIME_LIMIT_SEC -> HandshakeStatus.HEALTHY
			it > HandshakeStatus.STALE_TIME_LIMIT_SEC -> HandshakeStatus.STALE
			else -> {
				HandshakeStatus.UNKNOWN
			}
		}
	}
}

internal fun Peer.isReachable(preferIpv4: Boolean): Boolean {
	val host =
		if (this.endpoint.isPresent &&
			this.endpoint.get().getResolved(preferIpv4).isPresent
		) {
			this.endpoint.get().getResolved(preferIpv4).get().host
		} else {
			Constants.DEFAULT_PING_IP
		}
	Timber.d("Checking reachability of peer: $host")
	val reachable =
		InetAddress.getByName(host)
			.isReachable(Constants.PING_TIMEOUT.toInt())
	return reachable
}

internal fun TunnelStatistics?.asColor(): Color = this?.mapPeerStats()
	?.map { it.value?.handshakeStatus() }
	?.let { statuses ->
		when {
			statuses.all { it == HandshakeStatus.HEALTHY } -> SilverTree
			statuses.any { it == HandshakeStatus.STALE } -> Straw
			statuses.all { it == HandshakeStatus.NOT_STARTED } -> Color.Gray
			else -> Color.Gray
		}
	} ?: Color.Gray

internal fun Config.toWgQuickString(): String {
	val amQuick = toAwgQuickString(true)
	val lines = amQuick.lines().toMutableList()
	val linesIterator = lines.iterator()
	while (linesIterator.hasNext()) {
		val next = linesIterator.next()
		Constants.amProperties.forEach {
			if (next.startsWith(it, ignoreCase = true)) {
				linesIterator.remove()
			}
		}
	}
	return lines.joinToString(System.lineSeparator())
}

internal fun BackendState.asAmBackendState(): Backend.BackendState = Backend.BackendState.valueOf(this.name)

internal fun Tunnel.State.asTunnelState(): TunnelStatus = when (this) {
	Tunnel.State.DOWN -> DOWN
	Tunnel.State.UP -> UP
}

internal fun BackendException.toBackendError(): BackendError = when (this.reason) {
	BackendException.Reason.VPN_NOT_AUTHORIZED -> BackendError.Unauthorized
	BackendException.Reason.DNS_RESOLUTION_FAILURE -> BackendError.DNS
	else -> BackendError.Unauthorized
}

internal fun com.wireguard.android.backend.Tunnel.State.asTunnelState(): TunnelStatus = when (this) {
	com.wireguard.android.backend.Tunnel.State.DOWN -> DOWN
	com.wireguard.android.backend.Tunnel.State.UP -> UP
}
