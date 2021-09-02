package de.rki.coronawarnapp.covidcertificate.pdf.core

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificate
import javax.inject.Inject

class RecoveryCertificateDrawHelper @Inject constructor(font: Typeface) {

    val paint = Paint().apply {
        typeface = font
        textSize = 35f
        color = CertificateDrawHelper.FONT_COLOR
    }

    fun drawCertificateDetail(canvas: Canvas, certificate: RecoveryCertificate) {
        with(canvas) {
            save()
            rotate(180f, PdfGenerator.PAGE_WIDTH / 2f, PdfGenerator.PAGE_HEIGHT / 2f)
            drawTextIntoRectangle("\$tg = ${certificate.targetDisease}", paint, TextArea(1895f, 2040f, 525f))
            drawTextIntoRectangle("\$fr = ${certificate.testedPositiveOnFormatted}", paint, TextArea(1895f, 2335f, 525f))
            drawTextIntoRectangle("\$co = ${certificate.certificateCountry}", paint, TextArea(1895f, 2550f, 525f))
            drawTextIntoRectangle("\$is = ${certificate.certificateIssuer}", paint, TextArea(1895f, 2740f, 525f))
            drawTextIntoRectangle("\$df = ${certificate.validFromFormatted}", paint, TextArea(1895f, 2925f, 525f))
            drawTextIntoRectangle("\$du = ${certificate.validUntilFormatted}", paint, TextArea(1895f, 3090f, 525f))
            restore()
        }
    }
}
