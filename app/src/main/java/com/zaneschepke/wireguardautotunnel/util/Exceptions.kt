package com.zaneschepke.wireguardautotunnel.util

internal object InvalidFileExtensionException : Exception() {
	private fun readResolve(): Any = InvalidFileExtensionException
}

internal object FileReadException : Exception() {
	private fun readResolve(): Any = FileReadException
}
