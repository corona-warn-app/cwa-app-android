package de.rki.coronawarnapp.presencetracing.risk

import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.presencetracing.checkins.CheckIn
import de.rki.coronawarnapp.presencetracing.risk.calculation.CheckInWarningOverlap
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toLocalDateUtc
import de.rki.coronawarnapp.util.TimeStamper
import kotlinx.coroutines.flow.first
import org.joda.time.DateTimeZone
import org.joda.time.Instant
import javax.inject.Inject

class CheckInsFilter @Inject constructor(
    private val appConfigProvider: AppConfigProvider,
    private val timeStamper: TimeStamper,
) {

    fun filterCheckInWarningsByAge(
        list: List<CheckInWarningOverlap>,
        deadline: Instant
    ): List<CheckInWarningOverlap> {
        return list.filter { it.startTime.isAfter(deadline) }
    }

    suspend fun filterCheckIns(
        list: List<CheckIn>,
        now: Instant = timeStamper.nowUTC
    ): List<CheckIn> = list.filterByAge(
        getMaxAgeInDays(),
        now
    )

    private suspend fun getMaxAgeInDays() = appConfigProvider.currentConfig.first().maxCheckInAgeInDays

    suspend fun calculateDeadline(now: Instant): Instant =
        now.minusDaysAtStartOfDayUtc(getMaxAgeInDays()).toInstant()
}

fun List<CheckIn>.filterByAge(
    maxAgeInDays: Int,
    now: Instant,
): List<CheckIn> {
    val deadline = now.minusDaysAtStartOfDayUtc(maxAgeInDays).millis
    return filter { it.checkInEnd.millis >= deadline }
}

internal fun Instant.minusDaysAtStartOfDayUtc(days: Int) = toLocalDateUtc()
    .minusDays(days)
    .toDateTimeAtStartOfDay(DateTimeZone.UTC)

internal val ConfigData.maxCheckInAgeInDays
    get() = presenceTracing.riskCalculationParameters.maxCheckInAgeInDays
