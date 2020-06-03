package de.rki.coronawarnapp.util

import KeyExportFormat
import com.google.android.gms.nearby.exposurenotification.TemporaryExposureKey
import com.google.protobuf.ByteString
import de.rki.coronawarnapp.server.protocols.AppleLegacyKeyExchange

object ProtoFormatConverterExtensions {

    private const val ROLLING_PERIOD = 144
    private const val DEFAULT_TRANSMISSION_RISK_LEVEL = 1
    private const val MAXIMUM_KEYS = 14

    fun List<TemporaryExposureKey>.limitKeyCount() =
        this.sortedWith(compareBy({ it.rollingStartIntervalNumber })).asReversed().take(MAXIMUM_KEYS)

    fun List<TemporaryExposureKey>.transformKeyHistoryToExternalFormat() = this.map {
        KeyExportFormat.TemporaryExposureKey.newBuilder()
            .setKeyData(ByteString.readFrom(it.keyData.inputStream()))
            .setRollingStartIntervalNumber(it.rollingStartIntervalNumber)
            .setRollingPeriod(ROLLING_PERIOD)
            .setTransmissionRiskLevel(DEFAULT_TRANSMISSION_RISK_LEVEL)
            .build()
    }

    fun AppleLegacyKeyExchange.Key.convertToGoogleKey(): KeyExportFormat.TemporaryExposureKey =
        KeyExportFormat.TemporaryExposureKey.newBuilder()
            .setKeyData(this.keyData)
            .setRollingPeriod(this.rollingPeriod)
            .setRollingStartIntervalNumber(this.rollingStartNumber)
            .setTransmissionRiskLevel(this.transmissionRiskLevel)
            .build()
}
