package de.rki.coronawarnapp.risk

import androidx.annotation.VisibleForTesting
import com.google.android.gms.nearby.exposurenotification.ExposureWindow
import dagger.Reusable
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.ExposureWindowRiskCalculationConfig
import de.rki.coronawarnapp.risk.result.ExposureWindowDayRisk
import de.rki.coronawarnapp.util.toLocalDateUtc
import kotlinx.coroutines.flow.first
import java.time.Instant
import java.time.ZoneOffset
import javax.inject.Inject

@Reusable
class ExposureWindowsFilter @Inject constructor(
    private val appConfigProvider: AppConfigProvider,
) {

    internal fun filterByAge(
        config: ExposureWindowRiskCalculationConfig,
        list: List<ExposureWindow>,
        nowUtc: Instant
    ): List<ExposureWindow> = list.filterByAge(
        maxAgeInDays = config.maxEncounterAgeInDays,
        nowUtc = nowUtc
    )

    internal suspend fun filterDayRisksByAge(
        list: List<ExposureWindowDayRisk>,
        calculatedAt: Instant
    ): List<ExposureWindowDayRisk> {
        val config = appConfigProvider.currentConfig.first()
        val deadline = config.getDeadline(calculatedAt).toLocalDateUtc()
        return list.filter { ewDayRisk ->
            !ewDayRisk.localDateUtc.isBefore(deadline)
        }
    }

    private fun ExposureWindowRiskCalculationConfig.getDeadline(nowUtc: Instant): Instant =
        nowUtc.minusDays(maxEncounterAgeInDays).toInstant()
}

@VisibleForTesting
internal fun List<ExposureWindow>.filterByAge(
    maxAgeInDays: Long,
    nowUtc: Instant
): List<ExposureWindow> {
    val deadline = nowUtc.minusDays(maxAgeInDays).toInstant().toEpochMilli()
    return filter {
        it.dateMillisSinceEpoch >= deadline
    }
}

private fun Instant.minusDays(days: Long) = toLocalDateUtc().minusDays(days).atStartOfDay(ZoneOffset.UTC)
