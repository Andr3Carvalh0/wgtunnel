package com.zaneschepke.wireguardautotunnel.ui.enums

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.Remove
import androidx.compose.ui.graphics.vector.ImageVector
import com.zaneschepke.wireguardautotunnel.R
import com.zaneschepke.wireguardautotunnel.util.StringValue

internal enum class SplitOptions {
	INCLUDE,
	ALL,
	EXCLUDE,
	;

	fun icon(): ImageVector = when (this) {
		ALL -> Icons.Filled.AllInclusive
		INCLUDE -> Icons.Filled.Add
		EXCLUDE -> Icons.Filled.Remove
	}

	fun text(): StringValue = when (this) {
		ALL -> StringValue.StringResource(R.string.all)
		INCLUDE -> StringValue.StringResource(R.string.include)
		EXCLUDE -> StringValue.StringResource(R.string.exclude)
	}
}
