package de.rki.coronawarnapp.installTime

import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.di.AppInstallTime
import de.rki.coronawarnapp.util.toLocalDateUserTz
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InstallTimeProvider @Inject constructor(
    @AppInstallTime private val installTime: Instant,
    private val timeStamper: TimeStamper
) {
    private val dayOfInstallation = installTime.toLocalDateUserTz()

    val today: LocalDate
        get() = timeStamper.nowUTC.toLocalDateUserTz()

    val daysSinceInstallation: Int
        get() = ChronoUnit.DAYS.between(dayOfInstallation, today).toInt()
}
