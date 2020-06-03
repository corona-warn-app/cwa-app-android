package de.rki.coronawarnapp.util

import KeyExportFormat
import com.google.android.gms.nearby.exposurenotification.TemporaryExposureKey
import com.google.protobuf.ByteString
import de.rki.coronawarnapp.server.protocols.AppleLegacyKeyExchange

object ProtoFormatConverterExtensions {

    private const val ROLLING_PERIOD = 144
    private const val DEFAULT_TRANSMISSION_RISK_LEVEL = 1
    private const val TRANSMISSION_RISK_DAY_0 = 5
    private const val TRANSMISSION_RISK_DAY_1 = 6
    private const val TRANSMISSION_RISK_DAY_2 = 7
    private const val TRANSMISSION_RISK_DAY_3 = 8
    private const val TRANSMISSION_RISK_DAY_4 = 7
    private const val TRANSMISSION_RISK_DAY_5 = 5
    private const val TRANSMISSION_RISK_DAY_6 = 3
    private const val TRANSMISSION_RISK_DAY_7 = 2
    private val DEFAULT_TRANSMISSION_RISK_VECTOR = intArrayOf(
        TRANSMISSION_RISK_DAY_0,
        TRANSMISSION_RISK_DAY_1,
        TRANSMISSION_RISK_DAY_2,
        TRANSMISSION_RISK_DAY_3,
        TRANSMISSION_RISK_DAY_4,
        TRANSMISSION_RISK_DAY_5,
        TRANSMISSION_RISK_DAY_6,
        TRANSMISSION_RISK_DAY_7)
    private const val MAXIMUM_KEYS = 14

    fun List<TemporaryExposureKey>.limitKeyCount() =
        this.sortedWith(compareBy { it.rollingStartIntervalNumber }).asReversed().take(MAXIMUM_KEYS)

    fun List<TemporaryExposureKey>.transformKeyHistoryToExternalFormat() =
        this.sortedWith(compareBy { it.rollingStartIntervalNumber })
            .mapIndexed { index, it ->
                val riskValue =
                    if (index <= DEFAULT_TRANSMISSION_RISK_VECTOR.lastIndex) DEFAULT_TRANSMISSION_RISK_VECTOR[index]
                    else DEFAULT_TRANSMISSION_RISK_LEVEL
                KeyExportFormat.TemporaryExposureKey.newBuilder()
                    .setKeyData(ByteString.readFrom(it.keyData.inputStream()))
                    .setRollingStartIntervalNumber(it.rollingStartIntervalNumber)
                    .setRollingPeriod(ROLLING_PERIOD)
                    .setTransmissionRiskLevel(riskValue)
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
