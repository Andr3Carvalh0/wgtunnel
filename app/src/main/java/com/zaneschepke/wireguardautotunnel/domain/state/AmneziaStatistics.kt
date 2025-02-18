package com.zaneschepke.wireguardautotunnel.domain.state

import org.amnezia.awg.backend.Statistics
import org.amnezia.awg.crypto.Key

internal class AmneziaStatistics(private val statistics: Statistics) : TunnelStatistics() {
	override fun peerStats(peer: Key): PeerStats? {
		val key = Key.fromBase64(peer.toBase64())
		val stats = statistics.peer(key)
		return stats?.let {
			PeerStats(
				rxBytes = stats.rxBytes,
				txBytes = stats.txBytes,
				latestHandshakeEpochMillis = stats.latestHandshakeEpochMillis,
			)
		}
	}

	override fun isTunnelStale(): Boolean = statistics.isStale

	override fun getPeers(): Array<Key> = statistics.peers()

	override fun rx(): Long = statistics.totalRx()

	override fun tx(): Long = statistics.totalTx()
}
