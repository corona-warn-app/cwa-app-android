package de.rki.coronawarnapp.util.device

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.ExternalActionException
import de.rki.coronawarnapp.exception.reporting.report
import javax.inject.Inject

class DefaultPowerManagement @Inject constructor(
    private val context: Context
) : PowerManagement {

    private val powerManager by lazy {
        context.getSystemService(Context.POWER_SERVICE) as PowerManager
    }

    override val isIgnoringBatteryOptimizations
        get() = powerManager.isIgnoringBatteryOptimizations(context.packageName)

    override val toBatteryOptimizationSettingsIntent =
        Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)

    override fun disableBatteryOptimizations() {
        try {
            context.startActivity(
                Intent(
                    Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                    Uri.parse("package:${context.packageName}")
                )
            )
        } catch (exception: Exception) {
            // catch generic exception on settings navigation
            // most likely due to device / rom specific intent issue
            ExternalActionException(exception).report(
                ExceptionCategory.UI
            )
        }
    }
}
