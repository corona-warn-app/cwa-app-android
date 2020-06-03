@file:JvmName("FormatterInformationHelper")

package de.rki.coronawarnapp.util.formatter

import de.rki.coronawarnapp.BuildConfig
import de.rki.coronawarnapp.CoronaWarnApplication
import de.rki.coronawarnapp.R

fun formatVersion(): String {
    val appContext = CoronaWarnApplication.getAppContext()
    val versionName: String = BuildConfig.VERSION_NAME
    return appContext.getString(R.string.information_version).format(versionName)
}
