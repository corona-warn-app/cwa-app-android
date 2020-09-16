package de.rki.coronawarnapp.submission

import KeyExportFormat
import com.google.android.gms.nearby.exposurenotification.TemporaryExposureKey

class ExposureKeyDomain {

    companion object {

        private const val MAXIMUM_KEYS = 14
    }

    fun transformToKeyHistoryInExternalFormat(
        keys: List<TemporaryExposureKey>,
        transmissionRiskVector: TransmissionRiskVector,
        converter: (TemporaryExposureKey, Int) -> (KeyExportFormat.TemporaryExposureKey)
    ) =
        toExternalFormat(
            toSortedHistory(limitKeyCount(keys)),
            transmissionRiskVector,
            converter
        )

    fun <T> limitKeyCount(keys: List<T>): List<T> =
        keys.take(MAXIMUM_KEYS)

    fun toExternalFormat(
        keys: List<TemporaryExposureKey>,
        transmissionRiskVector: TransmissionRiskVector,
        converter: (TemporaryExposureKey, Int) -> (KeyExportFormat.TemporaryExposureKey)
    ) =
        keys.mapIndexed { index, key ->
            // The latest key we receive is from yesterday (i.e. 1 day ago),
            // thus we need use index+1
            converter.invoke(key, transmissionRiskVector.getRiskValue(index + 1))
        }

    fun toSortedHistory(keys: List<TemporaryExposureKey>) =
        keys.sortedWith(compareByDescending { it.rollingStartIntervalNumber })
}
