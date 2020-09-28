package de.rki.coronawarnapp.submission

import KeyExportFormat
import androidx.annotation.VisibleForTesting
import com.google.android.gms.nearby.exposurenotification.TemporaryExposureKey
import org.joda.time.Days
import org.joda.time.Instant

class ExposureKeyHistoryCalculations(
    private val transmissionRiskVectorDeterminator: TransmissionRiskVectorDeterminator,
    private val daysSinceOnsetOfSymptomsVectorDeterminator: DaysSinceOnsetOfSymptomsVectorDeterminator,
    private val keyConverter: KeyConverter
) {

    companion object {
        const val MAX_AGE_IN_DAYS = 14
        const val TEN_MINUTES_IN_MILLIS = (10 * 60 * 1000).toLong()
    }

    fun transformToKeyHistoryInExternalFormat(
        keys: List<TemporaryExposureKey>,
        symptoms: Symptoms
    ) =
        toExternalFormat(
            removeOldKeys(sortWithRecentKeyFirst(keys)),
            transmissionRiskVectorDeterminator.determine(symptoms),
            daysSinceOnsetOfSymptomsVectorDeterminator.determine(symptoms)
        )

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun removeOldKeys(keys: List<TemporaryExposureKey>, now: Instant = Instant()) =
        keys.filter { it.ageInDays(now) in 0..MAX_AGE_IN_DAYS }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun toExternalFormat(
        keys: List<TemporaryExposureKey>,
        transmissionRiskVector: TransmissionRiskVector,
        daysSinceOnsetOfSymptomsVector: DaysSinceOnsetOfSymptomsVector,
        now: Instant = Instant()
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
    internal fun TemporaryExposureKey.ageInDays(now: Instant = Instant()): Int =
        ageInDays(
            Instant.ofEpochMilli(rollingStartIntervalNumber * TEN_MINUTES_IN_MILLIS),
            now
        )

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun ageInDays(instant: Instant, now: Instant = Instant()) =
        Days.daysBetween(instant, now).days
}
