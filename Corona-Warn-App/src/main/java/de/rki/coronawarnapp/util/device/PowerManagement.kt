package de.rki.coronawarnapp.util.device

import android.content.Context

interface PowerManagement {
    /**
     * Checks if app is excluded from battery optimizations
     */
    fun isIgnoringBatteryOptimizations(context: Context): Boolean
}
