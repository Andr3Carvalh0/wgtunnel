package com.zaneschepke.wireguardautotunnel.util.extensions

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Context.POWER_SERVICE
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.service.quicksettings.TileService
import android.widget.Toast
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.core.location.LocationManagerCompat
import com.zaneschepke.wireguardautotunnel.R
import com.zaneschepke.wireguardautotunnel.core.service.tile.AutoTunnelControlTile
import com.zaneschepke.wireguardautotunnel.core.service.tile.TunnelControlTile
import com.zaneschepke.wireguardautotunnel.util.Constants

private const val BASELINE_HEIGHT = 2201
private const val BASELINE_WIDTH = 1080
private const val BASELINE_DENSITY = 2.625

internal fun Context.openWebUrl(url: String): Result<Unit> = kotlin.runCatching {
	val webpage: Uri = Uri.parse(url)
	val intent = Intent(Intent.ACTION_VIEW, webpage).apply {
		addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
	}
	startActivity(intent)
}.onFailure {
	showToast(R.string.no_browser_detected)
}

internal fun Context.isBatteryOptimizationsDisabled(): Boolean {
	val pm = getSystemService(POWER_SERVICE) as PowerManager
	return pm.isIgnoringBatteryOptimizations(packageName)
}

internal val Context.actionBarSize
	get() = theme.obtainStyledAttributes(intArrayOf(android.R.attr.actionBarSize))
		.let { attrs -> attrs.getDimension(0, 0F).toInt().also { attrs.recycle() } }

internal fun Context.resizeHeight(dp: Dp): Dp {
	val displayMetrics = resources.displayMetrics
	val density = displayMetrics.density
	val height = (displayMetrics.heightPixels - this.actionBarSize)
	val resizeHeightPercentage =
		(height.toFloat() / BASELINE_HEIGHT) * (BASELINE_DENSITY.toFloat() / density)
	return dp * resizeHeightPercentage
}

internal fun Context.resizeHeight(textUnit: TextUnit): TextUnit {
	val displayMetrics = resources.displayMetrics
	val density = displayMetrics.density
	val height = (displayMetrics.heightPixels - actionBarSize)
	val resizeHeightPercentage =
		(height.toFloat() / BASELINE_HEIGHT) * (BASELINE_DENSITY.toFloat() / density)
	return textUnit * resizeHeightPercentage * 1.1
}

internal fun Context.resizeWidth(dp: Dp): Dp {
	val displayMetrics = resources.displayMetrics
	val density = displayMetrics.density
	val width = displayMetrics.widthPixels
	val resizeWidthPercentage =
		(width.toFloat() / BASELINE_WIDTH) * (BASELINE_DENSITY.toFloat() / density)
	return dp * resizeWidthPercentage
}

internal fun Context.launchNotificationSettings() {
	val settingsIntent: Intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
		.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
		.putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
	this.startActivity(settingsIntent)
}

internal fun Context.launchShareFile(file: Uri) {
	val shareIntent = Intent().apply {
		action = Intent.ACTION_SEND
		type = "*/*"
		putExtra(Intent.EXTRA_STREAM, file)
		addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
	}
	this.startActivity(Intent.createChooser(shareIntent, ""))
}

internal fun Context.isLocationServicesEnabled(): Boolean {
	val locationManager =
		getSystemService(
			Context.LOCATION_SERVICE,
		) as LocationManager
	return LocationManagerCompat.isLocationEnabled(locationManager)
}

internal fun Context.showToast(resId: Int) {
	Toast.makeText(
		this,
		this.getString(resId),
		Toast.LENGTH_LONG,
	).show()
}

internal fun Context.launchVpnSettings(): Result<Unit> = kotlin.runCatching {
	val intent = Intent(Constants.VPN_SETTINGS_PACKAGE).apply {
		setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
	}
	startActivity(intent)
}

internal fun Context.launchAppSettings() {
	kotlin.runCatching {
		val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
			data = Uri.fromParts("package", packageName, null)
			flags = Intent.FLAG_ACTIVITY_NEW_TASK
		}
		startActivity(intent)
	}
}

internal fun Context.requestTunnelTileServiceStateUpdate() {
	TileService.requestListeningState(
		this,
		ComponentName(this, TunnelControlTile::class.java),
	)
}

internal fun Context.requestAutoTunnelTileServiceUpdate() {
	TileService.requestListeningState(
		this,
		ComponentName(this, AutoTunnelControlTile::class.java),
	)
}

internal fun Context.getAllInternetCapablePackages(): List<PackageInfo> {
	val permissions = arrayOf(Manifest.permission.INTERNET)
	return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
		packageManager.getPackagesHoldingPermissions(
			permissions,
			PackageManager.PackageInfoFlags.of(0L),
		)
	} else {
		packageManager.getPackagesHoldingPermissions(permissions, 0)
	}
}
