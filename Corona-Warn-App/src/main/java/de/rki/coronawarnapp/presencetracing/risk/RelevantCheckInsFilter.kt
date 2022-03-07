package de.rki.coronawarnapp.presencetracing.risk

import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.presencetracing.checkins.CheckIn
import de.rki.coronawarnapp.presencetracing.risk.calculation.CheckInWarningOverlap
import de.rki.coronawarnapp.util.TimeStamper
import org.joda.time.Days
import org.joda.time.Instant
import javax.inject.Inject

class RelevantCheckInsFilter @Inject constructor(
    private val appConfigProvider: AppConfigProvider,
    private val timeStamper: TimeStamper,
) {

    suspend fun filterCheckInWarnings(list: List<CheckInWarningOverlap>): List<CheckInWarningOverlap> {
        val deadline = getDeadline()
        return list.filter { it.startTime.isAfter(deadline) }
    }

    suspend fun filterCheckIns(list: List<CheckIn>): List<CheckIn> = list.filterByAge(
        getMaxAgeInDays(),
        timeStamper.nowUTC
    )

    private suspend fun getMaxAgeInDays() = appConfigProvider.getAppConfig().maxCheckInAgeInDays

    private suspend fun getDeadline(now: Instant = timeStamper.nowUTC): Instant =
        now.calculateDeadline(getMaxAgeInDays())
}

internal fun List<CheckIn>.filterByAge(
    maxAgeInDays: Int,
    now: Instant,
): List<CheckIn> {
    val deadline = now.calculateDeadline(maxAgeInDays).millis
    return filter { it.checkInEnd.millis >= deadline }
}

internal fun Instant.calculateDeadline(maxAgeInDays: Int): Instant = this - Days.days(maxAgeInDays).toStandardDuration()

internal val ConfigData.maxCheckInAgeInDays
    get()= presenceTracing.riskCalculationParameters.maxCheckInAgeInDays
