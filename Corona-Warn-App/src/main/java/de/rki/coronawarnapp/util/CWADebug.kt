package de.rki.coronawarnapp.util

import de.rki.coronawarnapp.BuildConfig

object CWADebug {
    val isDebugBuildOrMode: Boolean
        get() = BuildConfig.DEBUG || BuildConfig.BUILD_VARIANT == "deviceForTesters"
}