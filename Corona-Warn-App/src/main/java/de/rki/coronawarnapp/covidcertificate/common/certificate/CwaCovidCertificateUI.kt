package de.rki.coronawarnapp.covidcertificate.common.certificate

import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import de.rki.coronawarnapp.util.qrcode.QrCodeOptions
import de.rki.coronawarnapp.util.qrcode.coil.CoilQrCode
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate.State.Invalid
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate.State.Blocked
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate.State.Revoked
import java.util.Locale
import java.util.Locale.GERMAN

fun CwaCovidCertificate.getValidQrCode(locale: Locale = Locale.getDefault(), showBlocked: Boolean = false): CoilQrCode {

    fun getInvalidQrCode() = when (locale.language) {
        GERMAN.language -> Invalid.URL_INVALID_SIGNATURE_DE
        else -> Invalid.URL_INVALID_SIGNATURE_EN
    }.let { CoilQrCode(it, QrCodeOptions(correctionLevel = ErrorCorrectionLevel.M)) }

    return when {
        getState() is Invalid ||
            (getState() is Blocked && !showBlocked) ||
            getState() is Revoked -> getInvalidQrCode()
        else -> qrCodeToDisplay
    }
}
