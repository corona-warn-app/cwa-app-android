package de.rki.coronawarnapp.installTime

import android.content.Context
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toLocalDateUserTz
import de.rki.coronawarnapp.util.di.AppContext
import org.joda.time.Days
import org.joda.time.Instant
import org.joda.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InstallTimeProvider @Inject constructor(
    @AppContext private val context: Context
) {
    private val dayOfInstallation: LocalDate = Instant.ofEpochMilli(
        context
            .packageManager
            .getPackageInfo(context.packageName, 0)
            .firstInstallTime
    )
        .toLocalDateUserTz()

    val today = Instant.now().toLocalDateUserTz()

    val daysSinceInstallation: Int get() = Days.daysBetween(dayOfInstallation, today).days
}
