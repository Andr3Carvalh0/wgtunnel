package com.zaneschepke.wireguardautotunnel.util.extensions

import com.zaneschepke.wireguardautotunnel.data.model.TunnelConfig
import com.zaneschepke.wireguardautotunnel.domain.entity.TunnelConf
import java.math.BigDecimal
import java.text.DecimalFormat

internal fun BigDecimal.toThreeDecimalPlaceString(): String {
	val df = DecimalFormat("#.###")
	return df.format(this)
}

internal fun <T> List<T>.update(index: Int, item: T): List<T> = toMutableList().apply { this[index] = item }

internal typealias Tunnels = List<TunnelConf>

internal typealias TunnelConfigs = List<TunnelConfig>
