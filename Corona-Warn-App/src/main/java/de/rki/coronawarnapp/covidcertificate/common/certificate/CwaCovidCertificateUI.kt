package de.rki.coronawarnapp.covidcertificate.common.certificate

import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import de.rki.coronawarnapp.util.qrcode.QrCodeOptions
import de.rki.coronawarnapp.util.qrcode.coil.CoilQrCode
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate.State.Invalid
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate.State.Blocked
import java.util.Locale.GERMAN

fun CwaCovidCertificate.getValidQrCode(language: String) = when (getState()) {
    is Invalid, is Blocked -> when (language) {
        GERMAN.language -> Invalid.URL_INVALID_SIGNATURE_DE
        else -> Invalid.URL_INVALID_SIGNATURE_EN
    }.let { CoilQrCode(it, QrCodeOptions(correctionLevel = ErrorCorrectionLevel.M)) }
    else -> qrCodeToDisplay
}
