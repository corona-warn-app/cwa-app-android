package de.rki.coronawarnapp.covidcertificate.common.repository

import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.util.qrcode.QrCodeOptions
import de.rki.coronawarnapp.util.qrcode.coil.CoilQrCode
import java.util.Locale

interface CertificateRepoContainer {
    val containerId: CertificateContainerId

    /**
     * Returns what qr code to display based on certificate state
     * @param state Certificate state
     * @param language current use locale language such as [Locale.GERMAN.language]
     * @param certificateQrCode [String] Qr Code string
     *
     */
    fun displayQrCode(
        state: CwaCovidCertificate.State,
        language: String,
        certificateQrCode: String
    ) = when (state) {
        CwaCovidCertificate.State.Invalid -> when (language) {
            Locale.GERMAN.language -> CwaCovidCertificate.State.Invalid.URL_INVALID_SIGNATURE_DE
            else -> CwaCovidCertificate.State.Invalid.URL_INVALID_SIGNATURE_EN
        }.let { CoilQrCode(it, QrCodeOptions(correctionLevel = ErrorCorrectionLevel.M)) }
        else -> CoilQrCode(certificateQrCode)
    }
}
