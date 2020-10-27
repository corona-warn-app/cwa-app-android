package de.rki.coronawarnapp.util

import de.rki.coronawarnapp.server.protocols.AppleLegacyKeyExchange
import de.rki.coronawarnapp.server.protocols.external.exposurenotification.TemporaryExposureKeyExportOuterClass.TemporaryExposureKey

object ProtoFormatConverterExtensions {

    fun AppleLegacyKeyExchange.Key.convertToGoogleKey(): TemporaryExposureKey =
        TemporaryExposureKey.newBuilder()
            .setKeyData(this.keyData)
            .setRollingPeriod(this.rollingPeriod)
            .setRollingStartIntervalNumber(this.rollingStartNumber)
            .setTransmissionRiskLevel(this.transmissionRiskLevel)
            .build()
}
