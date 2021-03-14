package de.rki.coronawarnapp.util.device

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import androidx.core.content.getSystemService
import de.rki.coronawarnapp.util.di.AppContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PowerManagement @Inject constructor(
    @AppContext private val context: Context
) {

    private val powerManager by lazy { context.getSystemService<PowerManager>()!! }

    val isIgnoringBatteryOptimizations
        get() = powerManager.isIgnoringBatteryOptimizations(context.packageName)

    val toBatteryOptimizationSettingsIntent =
        Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)

    val disableBatteryOptimizationsIntent =
        Intent(
            Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
            Uri.parse("package:${context.packageName}")
        )
}
