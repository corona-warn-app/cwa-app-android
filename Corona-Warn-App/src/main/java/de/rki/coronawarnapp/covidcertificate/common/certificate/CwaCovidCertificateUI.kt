package de.rki.coronawarnapp.covidcertificate.common.certificate

import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import de.rki.coronawarnapp.util.qrcode.QrCodeOptions
import de.rki.coronawarnapp.util.qrcode.coil.CoilQrCode
import java.util.Locale.GERMAN

fun CwaCovidCertificate.getValidQrCode(language: String) = when (getState()) {
    // TODO: update for BLOCKED
    is CwaCovidCertificate.State.Invalid -> when (language) {
        GERMAN.language -> CwaCovidCertificate.State.Invalid.URL_INVALID_SIGNATURE_DE
        else -> CwaCovidCertificate.State.Invalid.URL_INVALID_SIGNATURE_EN
    }.let { CoilQrCode(it, QrCodeOptions(correctionLevel = ErrorCorrectionLevel.M)) }
    else -> qrCodeToDisplay
}
