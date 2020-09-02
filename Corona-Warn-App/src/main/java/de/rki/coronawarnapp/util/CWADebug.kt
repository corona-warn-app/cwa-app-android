package de.rki.coronawarnapp.util

import de.rki.coronawarnapp.BuildConfig
import de.rki.coronawarnapp.storage.LocalData

object CWADebug {
    val isDebugBuildOrMode: Boolean
        get() = BuildConfig.DEBUG || BuildConfig.BUILD_VARIANT == "deviceForTesters"

    val isLast3HoursFetchEnabled: Boolean
        get() = LocalData.last3HoursMode()

    val isDebugBuildOrModeAndLast3HoursFetchEnabled: Boolean
        get() = isDebugBuildOrMode && isLast3HoursFetchEnabled
}
