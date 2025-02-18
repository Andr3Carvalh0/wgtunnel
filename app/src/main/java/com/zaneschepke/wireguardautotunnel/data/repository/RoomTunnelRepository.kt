package com.zaneschepke.wireguardautotunnel.data.repository

import com.zaneschepke.wireguardautotunnel.data.dao.TunnelConfigDao
import com.zaneschepke.wireguardautotunnel.domain.repository.TunnelRepository
import com.zaneschepke.wireguardautotunnel.data.model.TunnelConfig
import com.zaneschepke.wireguardautotunnel.di.IoDispatcher
import com.zaneschepke.wireguardautotunnel.domain.entity.TunnelConf
import com.zaneschepke.wireguardautotunnel.util.extensions.Tunnels
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

internal class RoomTunnelRepository(private val tunnelConfigDao: TunnelConfigDao, @IoDispatcher private val ioDispatcher: CoroutineDispatcher) :
	TunnelRepository {

	override val flow = tunnelConfigDao.getAllFlow().flowOn(ioDispatcher).map { it.map { it.toTunnel() } }

	override suspend fun getAll(): Tunnels = withContext(ioDispatcher) {
		tunnelConfigDao.getAll().map { it.toTunnel() }
	}

	override suspend fun save(tunnelConf: TunnelConf) {
		withContext(ioDispatcher) {
			tunnelConfigDao.save(TunnelConfig.from(tunnelConf))
		}
	}

	override suspend fun updatePrimaryTunnel(tunnelConf: TunnelConf?) {
		withContext(ioDispatcher) {
			tunnelConfigDao.resetPrimaryTunnel()
			tunnelConf?.let {
				save(
					it.copy(
						isPrimaryTunnel = true,
					),
				)
			}
		}
	}

	override suspend fun updateMobileDataTunnel(tunnelConf: TunnelConf?) {
		withContext(ioDispatcher) {
			tunnelConfigDao.resetMobileDataTunnel()
			tunnelConf?.let {
				save(
					it.copy(
						isMobileDataTunnel = true,
					),
				)
			}
		}
	}

	override suspend fun updateEthernetTunnel(tunnelConf: TunnelConf?) {
		withContext(ioDispatcher) {
			tunnelConfigDao.resetEthernetTunnel()
			tunnelConf?.let {
				save(
					it.copy(
						isEthernetTunnel = true,
					),
				)
			}
		}
	}

	override suspend fun delete(tunnelConf: TunnelConf) {
		withContext(ioDispatcher) {
			tunnelConfigDao.delete(TunnelConfig.from(tunnelConf))
		}
	}

	override suspend fun getById(id: Int): TunnelConf? = withContext(ioDispatcher) { tunnelConfigDao.getById(id.toLong())?.toTunnel() }

	override suspend fun getActive(): Tunnels = withContext(ioDispatcher) {
		tunnelConfigDao.getActive().map { it.toTunnel() }
	}

	override suspend fun count(): Int = withContext(ioDispatcher) { tunnelConfigDao.count().toInt() }

	override suspend fun findByTunnelName(name: String): TunnelConf? = withContext(ioDispatcher) { tunnelConfigDao.getByName(name)?.toTunnel() }

	override suspend fun findByTunnelNetworksName(name: String): Tunnels = withContext(ioDispatcher) {
		tunnelConfigDao.findByTunnelNetworkName(name).map { it.toTunnel() }
	}

	override suspend fun findByMobileDataTunnel(): Tunnels = withContext(ioDispatcher) { tunnelConfigDao.findByMobileDataTunnel().map { it.toTunnel() } }

	override suspend fun findPrimary(): Tunnels = withContext(ioDispatcher) { tunnelConfigDao.findByPrimary().map { it.toTunnel() } }
}
