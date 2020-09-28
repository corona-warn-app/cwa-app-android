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
        const val MAX_DAYS_IN_PAST = 14
        const val TEN_MINUTES_IN_MILLIS = (10 * 60 * 1000).toLong()
    }

    fun transformToKeyHistoryInExternalFormat(
        keys: List<TemporaryExposureKey>,
        symptoms: Symptoms
    ) =
        toExternalFormat(
            limitKeyCount(toSortedHistory(keys)),
            transmissionRiskVectorDeterminator.determine(symptoms),
            daysSinceOnsetOfSymptomsVectorDeterminator.determine(symptoms)
        )

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun limitKeyCount(keys: List<TemporaryExposureKey>, now: Instant = Instant()) =
        keys.filter { it.daysAgo(now) <= MAX_DAYS_IN_PAST }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun toExternalFormat(
        keys: List<TemporaryExposureKey>,
        transmissionRiskVector: TransmissionRiskVector,
        daysSinceOnsetOfSymptomsVector: DaysSinceOnsetOfSymptomsVector,
        now: Instant = Instant()
    ): List<KeyExportFormat.TemporaryExposureKey> {
        val result = mutableListOf<KeyExportFormat.TemporaryExposureKey>()
        keys.groupBy { it.daysAgo(now) }.forEach { entry ->
            val daysAgo = entry.key
            entry.value.forEach {
                result.add(
                    keyConverter.toExternalFormat(
                        it,
                        transmissionRiskVector.getRiskValue(daysAgo),
                        daysSinceOnsetOfSymptomsVector[daysAgo]
                    )
                )
            }
        }
        return result.toList()
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun toSortedHistory(keys: List<TemporaryExposureKey>) =
        keys.sortedWith(compareByDescending { it.rollingStartIntervalNumber })

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun TemporaryExposureKey.daysAgo(now: Instant = Instant()): Int =
        daysAgo(
            Instant.ofEpochMilli(rollingStartIntervalNumber * TEN_MINUTES_IN_MILLIS),
            now
        )

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun daysAgo(instant: Instant, now: Instant = Instant()) =
        Days.daysBetween(instant, now).days
}
