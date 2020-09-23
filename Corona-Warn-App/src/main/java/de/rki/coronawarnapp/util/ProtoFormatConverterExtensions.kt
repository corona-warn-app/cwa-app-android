package de.rki.coronawarnapp.util

import KeyExportFormat
import de.rki.coronawarnapp.server.protocols.AppleLegacyKeyExchange

object ProtoFormatConverterExtensions {

    fun AppleLegacyKeyExchange.Key.convertToGoogleKey(): KeyExportFormat.TemporaryExposureKey =
        KeyExportFormat.TemporaryExposureKey.newBuilder()
            .setKeyData(this.keyData)
            .setRollingPeriod(this.rollingPeriod)
            .setRollingStartIntervalNumber(this.rollingStartNumber)
            .setTransmissionRiskLevel(this.transmissionRiskLevel)
            .build()
}
