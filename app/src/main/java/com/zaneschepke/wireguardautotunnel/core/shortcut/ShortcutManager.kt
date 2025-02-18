package com.zaneschepke.wireguardautotunnel.core.shortcut

internal interface ShortcutManager {

	suspend fun addShortcuts()
	suspend fun removeShortcuts()
}
