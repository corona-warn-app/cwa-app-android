package de.rki.coronawarnapp.installTime

import android.content.Context
import de.rki.coronawarnapp.util.TimeAndDateExtensions.roundUpMsToDays
import de.rki.coronawarnapp.util.di.AppContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InstallTimeProvider @Inject constructor(
    @AppContext private val context: Context
) {
    private val installTime: Long = context
        .packageManager
        .getPackageInfo(context.packageName, 0)
        .firstInstallTime

    val daysSinceInstallation: Long get() = (System.currentTimeMillis() - installTime).roundUpMsToDays()
}
