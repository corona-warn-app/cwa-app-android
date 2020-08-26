package de.rki.coronawarnapp.util.device

import android.content.Context
import android.os.PowerManager
import javax.inject.Inject

class DefaultPowerManagement @Inject constructor() : PowerManagement {
    override fun isIgnoringBatteryOptimizations(context: Context) =
        (context.getSystemService(Context.POWER_SERVICE) as PowerManager)
            .isIgnoringBatteryOptimizations(context.packageName)
}
