package de.rki.coronawarnapp.submission

import KeyExportFormat
import com.google.android.gms.nearby.exposurenotification.TemporaryExposureKey
import com.google.protobuf.ByteString

class ExposureKeyDomain {

    companion object {

        private const val ROLLING_PERIOD = 144
        private const val MAXIMUM_KEYS = 14
    }

    fun transformToKeyHistoryInExternalFormat(
        keys: List<TemporaryExposureKey>,
        transmissionRiskVector: TransmissionRiskVector
    ) =
        toExternalFormat(toSortedHistory(limitKeyCount(keys)), transmissionRiskVector)

    private fun limitKeyCount(keys: List<TemporaryExposureKey>): List<TemporaryExposureKey> =
        toSortedHistory(keys).take(MAXIMUM_KEYS)

    private fun toExternalFormat(
        keys: List<TemporaryExposureKey>,
        transmissionRiskVector: TransmissionRiskVector
    ) =
        keys.mapIndexed { index, key ->
            // The latest key we receive is from yesterday (i.e. 1 day ago),
            // thus we need use index+1
            key.toExternalFormat(transmissionRiskVector.getRiskValue(index + 1))
        }

    private fun toSortedHistory(keys: List<TemporaryExposureKey>) =
        keys.sortedWith(compareByDescending { it.rollingStartIntervalNumber })

    private fun TemporaryExposureKey.toExternalFormat(riskValue: Int) =
        KeyExportFormat.TemporaryExposureKey.newBuilder()
            .setKeyData(ByteString.readFrom(keyData.inputStream()))
            .setRollingStartIntervalNumber(rollingStartIntervalNumber)
            .setRollingPeriod(ROLLING_PERIOD)
            .setTransmissionRiskLevel(riskValue)
            .build()
}
