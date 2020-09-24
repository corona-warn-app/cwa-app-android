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
    }

    fun transformToKeyHistoryInExternalFormat(
        keys: List<TemporaryExposureKey>,
        symptoms: Symptoms
    ) =
    toExternalFormat(
        toSortedHistory(limitKeyCount(keys)),
        transmissionRiskVectorDeterminator.determine(symptoms),
        daysSinceOnsetOfSymptomsVectorDeterminator.determine(symptoms, keys.size)
        )

    @VisibleForTesting
    internal fun <T> limitKeyCount(keys: List<T>): List<T> =
        keys.take(MAXIMUM_KEYS)

    @VisibleForTesting
    internal fun toExternalFormat(
        keys: List<TemporaryExposureKey>,
        transmissionRiskVector: TransmissionRiskVector,
        daysSinceOnsetOfSymptomsVector: DaysSinceOnsetOfSymptomsVector
    ) =
        keys.mapIndexed { index, key ->
            // The latest key we receive is from yesterday (i.e. 1 day ago),
            // thus we need use index+1
            val i = index + 1
            keyConverter.toExternalFormat(
                key,
                transmissionRiskVector.getRiskValue(i),
                daysSinceOnsetOfSymptomsVector[i])
        }

    @VisibleForTesting
    internal fun toSortedHistory(keys: List<TemporaryExposureKey>) =
        keys.sortedWith(compareByDescending { it.rollingStartIntervalNumber })
}
