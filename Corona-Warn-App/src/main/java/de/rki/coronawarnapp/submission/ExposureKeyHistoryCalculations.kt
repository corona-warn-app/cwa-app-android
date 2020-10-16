package de.rki.coronawarnapp.submission

import androidx.annotation.VisibleForTesting
import com.google.android.gms.nearby.exposurenotification.TemporaryExposureKey
import de.rki.coronawarnapp.server.protocols.KeyExportFormat
import de.rki.coronawarnapp.util.TimeAndDateExtensions.ageInDays
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toLocalDate
import de.rki.coronawarnapp.util.TimeStamper
import org.joda.time.Duration
import org.joda.time.Instant
import org.joda.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExposureKeyHistoryCalculations @Inject constructor(
    private val transmissionRiskVectorDeterminator: TransmissionRiskVectorDeterminator,
    private val daysSinceOnsetOfSymptomsVectorDeterminator: DaysSinceOnsetOfSymptomsVectorDeterminator,
    private val keyConverter: KeyConverter,
    private val timeStamper: TimeStamper
) {

    fun transformToKeyHistoryInExternalFormat(
        keys: List<TemporaryExposureKey>,
        symptoms: Symptoms
    ) = toExternalFormat(
        removeOldKeys(sortWithRecentKeyFirst(keys)),
        transmissionRiskVectorDeterminator.determine(symptoms),
        daysSinceOnsetOfSymptomsVectorDeterminator.determine(symptoms)
    )

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun removeOldKeys(
        keys: List<TemporaryExposureKey>,
        now: LocalDate = timeStamper.nowUTC.toLocalDate()
    ) = keys.filter { it.ageInDays(now) in 0..MAX_AGE_IN_DAYS }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun toExternalFormat(
        keys: List<TemporaryExposureKey>,
        transmissionRiskVector: TransmissionRiskVector,
        daysSinceOnsetOfSymptomsVector: DaysSinceOnsetOfSymptomsVector,
        now: LocalDate = timeStamper.nowUTC.toLocalDate()
    ): List<KeyExportFormat.TemporaryExposureKey> {
        val result = mutableListOf<KeyExportFormat.TemporaryExposureKey>()
        keys.groupBy { it.ageInDays(now) }.forEach { entry ->
            val ageInDays = entry.key
            entry.value.forEach {
                result.add(
                    keyConverter.toExternalFormat(
                        it,
                        transmissionRiskVector[ageInDays],
                        daysSinceOnsetOfSymptomsVector[ageInDays]
                    )
                )
            }
        }
        return result.toList()
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun sortWithRecentKeyFirst(keys: List<TemporaryExposureKey>) =
        keys.sortedWith(compareByDescending { it.rollingStartIntervalNumber })

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun TemporaryExposureKey.ageInDays(now: LocalDate): Int =
        Instant.ofEpochMilli(rollingStartIntervalNumber * TEN_MINUTES_IN_MILLIS)
            .toLocalDate().ageInDays(now)

    companion object {
        const val MAX_AGE_IN_DAYS = 14
        val TEN_MINUTES_IN_MILLIS = Duration.standardMinutes(10).millis
    }
}
