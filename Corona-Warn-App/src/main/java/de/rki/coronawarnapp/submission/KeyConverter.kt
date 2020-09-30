package de.rki.coronawarnapp.submission

import com.google.android.gms.nearby.exposurenotification.TemporaryExposureKey
import de.rki.coronawarnapp.server.protocols.KeyExportFormat

interface KeyConverter {

    fun toExternalFormat(
        key: TemporaryExposureKey,
        riskValue: Int,
        daysSinceOnsetOfSymptoms: Int
    ): KeyExportFormat.TemporaryExposureKey
}
