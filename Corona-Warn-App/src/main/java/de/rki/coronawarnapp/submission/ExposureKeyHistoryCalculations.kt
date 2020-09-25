package de.rki.coronawarnapp.submission

import androidx.annotation.VisibleForTesting
import com.google.android.gms.nearby.exposurenotification.TemporaryExposureKey

class ExposureKeyHistoryCalculations(
    private val transmissionRiskVectorDeterminator: TransmissionRiskVectorDeterminator,
    private val daysSinceOnsetOfSymptomsVectorDeterminator: DaysSinceOnsetOfSymptomsVectorDeterminator,
    private val keyConverter: KeyConverter
) {

    companion object {
        private const val MAXIMUM_KEYS = 14

        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        internal fun toSortedHistory(keys: List<TemporaryExposureKey>) =
            keys.sortedWith(compareByDescending { it.rollingStartIntervalNumber })

        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        fun getIndex(
            temporaryExposureKey: TemporaryExposureKey,
            daysSinceOnsetOfSymptomsVector: DaysSinceOnsetOfSymptomsVector
        ) = daysSinceOnsetOfSymptomsVector.size // FIXME
    }

    fun transformToKeyHistoryInExternalFormat(
        keys: List<TemporaryExposureKey>,
        symptoms: Symptoms
    ) =
        toExternalFormat(
            limitKeyCount(toSortedHistory(keys)),
            transmissionRiskVectorDeterminator.determine(symptoms),
            daysSinceOnsetOfSymptomsVectorDeterminator.determine(symptoms, keys.size)
        )

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun <T> limitKeyCount(keys: List<T>): List<T> =
        keys.take(MAXIMUM_KEYS)

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun toExternalFormat(
        keys: List<TemporaryExposureKey>,
        transmissionRiskVector: TransmissionRiskVector,
        daysSinceOnsetOfSymptomsVector: DaysSinceOnsetOfSymptomsVector
    ) =
        keys.map {
            val index = getIndex(it, daysSinceOnsetOfSymptomsVector)
            keyConverter.toExternalFormat(
                it,
                transmissionRiskVector.getRiskValue(index),
                daysSinceOnsetOfSymptomsVector[index]
            )
        }
}
