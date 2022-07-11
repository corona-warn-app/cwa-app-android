package de.rki.coronawarnapp.covidcertificate.pdf.ui

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.qrcode.encoder.ByteMatrix
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.validation.core.country.DccCountry

/**
 * Certificate could be exported only if DCC country is Germany (DE)
 */
fun CwaCovidCertificate.canBeExported() = headerIssuer == DccCountry.DE

fun ByteMatrix.toBitmap(): Bitmap = Bitmap.createBitmap(
    (0 until height).flatMap { h ->
        (0 until width).map { w -> if (get(w, h) > 0) Color.BLACK else Color.WHITE }
    }.toIntArray(),
    width, height, Bitmap.Config.RGB_565
)
