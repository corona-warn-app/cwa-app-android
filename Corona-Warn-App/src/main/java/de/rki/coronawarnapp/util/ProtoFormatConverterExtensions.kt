package de.rki.coronawarnapp.util

import KeyExportFormat
import com.google.android.gms.nearby.exposurenotification.TemporaryExposureKey
import com.google.protobuf.ByteString
import de.rki.coronawarnapp.server.protocols.AppleLegacyKeyExchange
import de.rki.coronawarnapp.submission.TransmissionRiskVector

object ProtoFormatConverterExtensions {

    private const val ROLLING_PERIOD = 144
    private const val MAXIMUM_KEYS = 14

    fun List<TemporaryExposureKey>.limitKeyCount() = toSortedHistory().take(MAXIMUM_KEYS)

    fun List<TemporaryExposureKey>.toExternalFormat(transmissionRiskVector: TransmissionRiskVector) =
        mapIndexed { index, exposureKey ->
            // The latest key we receive is from yesterday (i.e. 1 day ago),
            // thus we need use index+1
            exposureKey.toExternalFormat(transmissionRiskVector.getRiskValue(index + 1))
        }

    fun List<TemporaryExposureKey>.toSortedHistory() =
        sortedWith(compareByDescending { it.rollingStartIntervalNumber })

    private fun TemporaryExposureKey.toExternalFormat(riskValue: Int) =
        KeyExportFormat.TemporaryExposureKey.newBuilder()
            .setKeyData(ByteString.readFrom(keyData.inputStream()))
            .setRollingStartIntervalNumber(rollingStartIntervalNumber)
            .setRollingPeriod(ROLLING_PERIOD)
            .setTransmissionRiskLevel(riskValue)
            .build()

    fun AppleLegacyKeyExchange.Key.convertToGoogleKey(): KeyExportFormat.TemporaryExposureKey =
        KeyExportFormat.TemporaryExposureKey.newBuilder()
            .setKeyData(this.keyData)
            .setRollingPeriod(this.rollingPeriod)
            .setRollingStartIntervalNumber(this.rollingStartNumber)
            .setTransmissionRiskLevel(this.transmissionRiskLevel)
            .build()
}
