package de.rki.coronawarnapp.submission

import androidx.annotation.VisibleForTesting
import com.google.android.gms.nearby.exposurenotification.TemporaryExposureKey

class ExposureKeyHistoryCalculations(
    private val transmissionRiskVectorDeterminator: TransmissionRiskVectorDeterminator,
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
            transmissionRiskVectorDeterminator.determine(symptoms)
        )

    @VisibleForTesting
    internal fun <T> limitKeyCount(keys: List<T>): List<T> =
        keys.take(MAXIMUM_KEYS)

    @VisibleForTesting
    internal fun toExternalFormat(
        keys: List<TemporaryExposureKey>,
        transmissionRiskVector: TransmissionRiskVector
    ) =
        keys.mapIndexed { index, key ->
            // The latest key we receive is from yesterday (i.e. 1 day ago),
            // thus we need use index+1
            keyConverter.toExternalFormat(key, transmissionRiskVector.getRiskValue(index + 1))
        }

    @VisibleForTesting
    internal fun toSortedHistory(keys: List<TemporaryExposureKey>) =
        keys.sortedWith(compareByDescending { it.rollingStartIntervalNumber })

}
