package de.rki.coronawarnapp.util.device

import android.content.Intent

interface PowerManagement {

    /**
     * Checks if app is excluded from battery optimizations
     */
    val isIgnoringBatteryOptimizations: Boolean

    val toBatteryOptimizationSettingsIntent: Intent

    fun disableBatteryOptimizations()
}
