package de.rki.coronawarnapp.covidcertificate.pdf.core

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Typeface
import androidx.annotation.ColorInt
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.encoder.Encoder
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import javax.inject.Inject

class CertificateDrawHelper @Inject constructor(
    @OpenSansTypeFace font: Typeface
) {

    val paint = Paint().apply {
        typeface = font
        textSize = FONT_SIZE
        color = FONT_COLOR
    }

    fun drawCertificateDetail(canvas: Canvas, certificate: CwaCovidCertificate) {
        with(canvas) {
            drawTextIntoRectangle(certificate.fullNameFormatted, paint, TextArea(28.78f, 693.31f, 267.5f))
            drawTextIntoRectangle(certificate.dateOfBirthFormatted, paint, TextArea(28.78f, 738.89f, 267.5f))
            drawTextIntoRectangle(certificate.uniqueCertificateIdentifier, paint, TextArea(28.78f, 783.27f, 267.5f))
        }
    }

    fun drawQrCode(canvas: Canvas, certificate: CwaCovidCertificate) {
        val qrCode = createQrCodeBitmap(certificate, 625)
        with(canvas) {
            save()
            drawBitmap(qrCode, Matrix().apply { scale(0.2399f, 0.2399f); translate(550f, 1815f) }, null)
            restore()
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
        @ColorInt const val FONT_COLOR = 0xFF0067A0.toInt()
        const val FONT_SIZE = 8f
    }
}
