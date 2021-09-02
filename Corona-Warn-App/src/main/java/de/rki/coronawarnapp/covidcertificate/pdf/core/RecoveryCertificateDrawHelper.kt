package de.rki.coronawarnapp.covidcertificate.pdf.core

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificate
import javax.inject.Inject

class RecoveryCertificateDrawHelper @Inject constructor(@OpenSansTypeFace font: Typeface) {

    val paint = Paint().apply {
        typeface = font
        textSize = 34f
        color = CertificateDrawHelper.FONT_COLOR
    }

    fun drawCertificateDetail(canvas: Canvas, certificate: RecoveryCertificate) {
        with(canvas) {
            save()
            rotate(180f, PdfGenerator.PAGE_WIDTH / 2f, PdfGenerator.PAGE_HEIGHT / 2f)
            drawTextIntoRectangle(certificate.targetDisease, paint, TextArea(1890f, 2040f, 540f))
            drawTextIntoRectangle(certificate.testedPositiveOnFormatted, paint, TextArea(1890f, 2335f, 540f))
            drawTextIntoRectangle(certificate.certificateCountry, paint, TextArea(1890f, 2550f, 540f))
            drawTextIntoRectangle(certificate.certificateIssuer, paint, TextArea(1890f, 2740f, 540f))
            drawTextIntoRectangle(certificate.validFromFormatted, paint, TextArea(1890f, 2925f, 540f))
            drawTextIntoRectangle(certificate.validUntilFormatted, paint, TextArea(1890f, 3090f, 540f))
            restore()
        }
    }
}
