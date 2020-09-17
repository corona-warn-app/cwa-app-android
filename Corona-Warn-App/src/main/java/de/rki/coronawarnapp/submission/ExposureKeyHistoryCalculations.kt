package de.rki.coronawarnapp.submission

import com.google.android.gms.nearby.exposurenotification.TemporaryExposureKey

class ExposureKeyHistoryCalculations(
    private val transmissionRiskVectorDeterminator: TransmissionRiskVectorDeterminator,
    private val keyConverter: KeyConverter
) {

    fun transformToKeyHistoryInExternalFormat(
        keys: List<TemporaryExposureKey>,
        symptoms: Symptoms
    ) =
        toExternalFormat(
            toSortedHistory(limitKeyCount(keys)),
            transmissionRiskVectorDeterminator.determine(symptoms)
        )

    fun <T> limitKeyCount(keys: List<T>): List<T> =
        keys.take(MAXIMUM_KEYS)

    fun toExternalFormat(
        keys: List<TemporaryExposureKey>,
        transmissionRiskVector: TransmissionRiskVector
    ) =
        keys.mapIndexed { index, key ->
            // The latest key we receive is from yesterday (i.e. 1 day ago),
            // thus we need use index+1
            keyConverter.toExternalFormat(key, transmissionRiskVector.getRiskValue(index + 1))
        }

    fun toSortedHistory(keys: List<TemporaryExposureKey>) =
        keys.sortedWith(compareByDescending { it.rollingStartIntervalNumber })

    companion object {

        private const val MAXIMUM_KEYS = 14
    }
}
