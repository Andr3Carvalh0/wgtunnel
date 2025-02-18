package com.zaneschepke.wireguardautotunnel.util.extensions

import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import timber.log.Timber

internal fun Job.cancelWithMessage(message: String) {
	kotlin.runCatching {
		cancel()
		Timber.i(message)
	}
}

internal suspend fun <T> StateFlow<T?>.withData(callback: suspend (T) -> Unit) = this.filterNotNull().first().let { callback(it) }
