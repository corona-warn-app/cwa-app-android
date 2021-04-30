package de.rki.coronawarnapp.util.device

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.annotation.RequiresApi
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
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            powerManager.isIgnoringBatteryOptimizations(context.packageName)
        } else {
            true
        }

    @RequiresApi(Build.VERSION_CODES.M)
    val toBatteryOptimizationSettingsIntent =
        Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
}
