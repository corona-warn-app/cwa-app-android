package de.rki.coronawarnapp.installTime

import android.content.Context
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toLocalDateUserTz
import de.rki.coronawarnapp.util.di.AppContext
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit
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

    val today: LocalDate
        get() = Instant.now().toLocalDateUserTz()

    val daysSinceInstallation: Int
        get() = ChronoUnit.DAYS.between(dayOfInstallation, today).toInt()
}
