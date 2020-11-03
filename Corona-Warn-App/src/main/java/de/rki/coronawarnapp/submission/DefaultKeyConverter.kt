package de.rki.coronawarnapp.submission

import com.google.android.gms.nearby.exposurenotification.TemporaryExposureKey
import com.google.protobuf.ByteString
import de.rki.coronawarnapp.server.protocols.external.exposurenotification.TemporaryExposureKeyExportOuterClass
import dagger.Reusable
import javax.inject.Inject

@Reusable
class DefaultKeyConverter @Inject constructor() : KeyConverter {
    override fun toExternalFormat(
        key: TemporaryExposureKey,
        riskValue: Int,
        daysSinceOnsetOfSymptoms: Int
    ) =
        TemporaryExposureKeyExportOuterClass.TemporaryExposureKey.newBuilder()
            .setKeyData(ByteString.readFrom(key.keyData.inputStream()))
            .setRollingStartIntervalNumber(key.rollingStartIntervalNumber)
            .setRollingPeriod(key.rollingPeriod)
            .setTransmissionRiskLevel(riskValue)
            .setDaysSinceOnsetOfSymptoms(daysSinceOnsetOfSymptoms)
            .build()
}
