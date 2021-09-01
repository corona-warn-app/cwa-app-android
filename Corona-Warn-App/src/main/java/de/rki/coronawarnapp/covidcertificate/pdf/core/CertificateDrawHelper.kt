package de.rki.coronawarnapp.covidcertificate.pdf.core

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import androidx.annotation.ColorInt
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.encoder.Encoder
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import javax.inject.Inject

class CertificateDrawHelper @Inject constructor(
    font: Typeface
) {

    val paint = Paint().apply {
        typeface = font
        textSize = 35f
        color = FONT_COLOR
    }

    fun drawCertificateDetail(canvas: Canvas, certificate: CwaCovidCertificate) {
        with(canvas) {
            drawTextIntoRectangle(certificate.fullNameFormatted, paint, TextArea(115f, 2890f, 1115f))
            drawTextIntoRectangle(certificate.dateOfBirthFormatted, paint, TextArea(115f, 3080f, 1115f))
            drawTextIntoRectangle(certificate.certificateId, paint, TextArea(115f, 3265f, 1115f))
        }
    }

    fun drawQrCode(canvas: Canvas, certificate: CwaCovidCertificate) {
        val qrCode = createQrCodeBitmap(certificate, 625)
        with(canvas) {
            drawBitmap(qrCode, 550f, 1815f, null)
        }
    }

    private fun createQrCodeBitmap(certificate: CwaCovidCertificate, size: Int): Bitmap {
        val hints = mapOf(
            /**
             * We cannot use Charsets.UTF_8 as zxing calls toString internally
             * and some android version return the class name and not the charset name
             */
            EncodeHintType.CHARACTER_SET to certificate.qrCodeToDisplay.options.characterSet.name()
        )

        val qrCode = Encoder.encode(
            certificate.qrCodeToDisplay.content,
            certificate.qrCodeToDisplay.options.correctionLevel,
            hints,
        )

        return Bitmap.createScaledBitmap(qrCode.matrix.toBitmap(), size, size, false)
    }

    companion object {
        @ColorInt val FONT_COLOR: Int = 0xFF0067A0.toInt()
    }
}
