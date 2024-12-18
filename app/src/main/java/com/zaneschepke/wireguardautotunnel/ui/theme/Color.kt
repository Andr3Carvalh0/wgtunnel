package com.zaneschepke.wireguardautotunnel.ui.theme

import androidx.compose.ui.graphics.Color

val OffWhite = Color(0xFFE5E1E5)
val LightGrey = Color(0xFFCAC4D0)
val Aqua = Color(0xFF76BEBD)
val SilverTree = Color(0xFF6DB58B)
val Plantation = Color(0xFF264A49)
val Shark = Color(0xFF21272A)
val BalticSea = Color(0xFF1C1B1F)
val Brick = Color(0xFFCE4257)
val Straw = Color(0xFFD4C483)

sealed class ThemeColors(
	val background: Color,
	val surface: Color,
	val primary: Color,
	val secondary: Color,
	val onSurface: Color,
) {

	data object Light : ThemeColors(
		background = LightGrey,
		surface = OffWhite,
		primary = Plantation,
		secondary = OffWhite,
		onSurface = BalticSea,
	)

	data object Dark : ThemeColors(
		background = BalticSea,
		surface = Shark,
		primary = Aqua,
		secondary = Plantation,
		onSurface = OffWhite,
	)
}
