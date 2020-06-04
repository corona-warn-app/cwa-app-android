package de.rki.coronawarnapp.http

import de.rki.coronawarnapp.util.PropertyLoader
import okhttp3.CertificatePinner

class CertificatePinnerFactory {
    fun getCertificatePinner(): CertificatePinner = PropertyLoader().run {
        CertificatePinner.Builder()
            .add(
                DynamicURLs.DOWNLOAD_CDN_URL.removePrefix(DynamicURLs.PATTERN_PREFIX_HTTPS),
                *this.getDistributionPins()
            )
            .add(
                DynamicURLs.SUBMISSION_CDN_URL.removePrefix(DynamicURLs.PATTERN_PREFIX_HTTPS),
                *this.getSubmissionPins()
            )
            .add(
                DynamicURLs.VERIFICATION_CDN_URL.removePrefix(DynamicURLs.PATTERN_PREFIX_HTTPS),
                *this.getVerificationPins()
            )
            .build()
    }
}
