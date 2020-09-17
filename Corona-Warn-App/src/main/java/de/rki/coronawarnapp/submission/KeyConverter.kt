package de.rki.coronawarnapp.submission

import KeyExportFormat
import com.google.android.gms.nearby.exposurenotification.TemporaryExposureKey

interface KeyConverter {

    fun toExternalFormat(
        key: TemporaryExposureKey,
        riskValue: Int
    ): KeyExportFormat.TemporaryExposureKey
}
