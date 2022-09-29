package de.rki.coronawarnapp.installTime

import android.content.Context
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.toLocalDateUserTz
import java.time.Instant
import java.time.LocalDate
import java.time.Period

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
        get() = Period.between(dayOfInstallation, today).days
}
