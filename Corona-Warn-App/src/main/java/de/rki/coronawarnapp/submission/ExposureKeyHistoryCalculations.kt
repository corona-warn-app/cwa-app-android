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
        const val VECTOR_LENGTH = 15
        const val TEN_MINUTES_IN_MILLIS = (10 * 60 * 1000).toLong()
    }

    fun transformToKeyHistoryInExternalFormat(
        keys: List<TemporaryExposureKey>,
        symptoms: Symptoms
    ) =
        toExternalFormat(
            limitKeyCount(toSortedHistory(keys)),
            transmissionRiskVectorDeterminator.determine(symptoms),
            daysSinceOnsetOfSymptomsVectorDeterminator.determine(symptoms, VECTOR_LENGTH)
        )

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun limitKeyCount(keys: List<TemporaryExposureKey>) =
        keys.filter { it.daysAgo <= 14 }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun toExternalFormat(
        keys: List<TemporaryExposureKey>,
        transmissionRiskVector: TransmissionRiskVector,
        daysSinceOnsetOfSymptomsVector: DaysSinceOnsetOfSymptomsVector
    ): List<KeyExportFormat.TemporaryExposureKey> {
        val result = mutableListOf<KeyExportFormat.TemporaryExposureKey>()
        keys.groupBy { it.daysAgo }.forEach { entry ->
            val daysAgo = entry.key
            val index = daysSinceOnsetOfSymptomsVector.indexOf(daysAgo)
            entry.value.forEach {
                result.add(
                    keyConverter.toExternalFormat(
                        it,
                        transmissionRiskVector.getRiskValue(index),
                        daysSinceOnsetOfSymptomsVector[index]
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
    val TemporaryExposureKey.daysAgo: Int
        get() = daysAgo(Instant.ofEpochMilli(rollingStartIntervalNumber * TEN_MINUTES_IN_MILLIS))

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun daysAgo(instant: Instant, now: Instant = Instant()) =
        Days.daysBetween(instant, now).days
}
