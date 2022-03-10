package de.rki.coronawarnapp.risk

import androidx.annotation.VisibleForTesting
import com.google.android.gms.nearby.exposurenotification.ExposureWindow
import de.rki.coronawarnapp.appconfig.ExposureWindowRiskCalculationConfig
import de.rki.coronawarnapp.risk.result.ExposureWindowDayRisk
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toLocalDateUtc
import org.joda.time.DateTimeZone
import org.joda.time.Instant

class ExposureWindowsFilter {

    internal fun filterByAge(
        config: ExposureWindowRiskCalculationConfig,
        list: List<ExposureWindow>,
        nowUtc: Instant
    ): List<ExposureWindow> = list.filterByAge(
        maxAgeInDays = config.getMaxEwAgeInDays(),
        nowUtc = nowUtc
    )

    internal fun filterDayRisksByAge(
        config: ExposureWindowRiskCalculationConfig,
        list: List<ExposureWindowDayRisk>,
        nowUtc: Instant
    ): List<ExposureWindowDayRisk> {
        val deadline = config.getDeadline(nowUtc).toLocalDateUtc()
        return list.filter { ewDayRisk ->
            !ewDayRisk.localDateUtc.isBefore(deadline)
        }
    }

    @VisibleForTesting
    internal fun List<ExposureWindow>.filterByAge(
        maxAgeInDays: Int,
        nowUtc: Instant
    ): List<ExposureWindow> {
        val deadline = nowUtc.minusDays(maxAgeInDays).millis
        return filter {
            it.dateMillisSinceEpoch >= deadline
        }
    }

    private fun ExposureWindowRiskCalculationConfig.getDeadline(nowUtc: Instant): Instant =
        nowUtc.minusDays(getMaxEwAgeInDays()).toInstant()

    private fun Instant.minusDays(days: Int) = toLocalDateUtc().minusDays(days).toDateTimeAtStartOfDay(DateTimeZone.UTC)

    private fun ExposureWindowRiskCalculationConfig.getMaxEwAgeInDays() =
        if (maxEncounterAgeInDays > 0) maxEncounterAgeInDays else DEFAULT_EW_AGE_IN_DAYS

}

private const val DEFAULT_EW_AGE_IN_DAYS = 14
