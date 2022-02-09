package de.rki.coronawarnapp.covidcertificate.pdf.core

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificate
import javax.inject.Inject
import de.rki.coronawarnapp.covidcertificate.pdf.core.CertificateDrawHelper.Companion.FONT_COLOR
import de.rki.coronawarnapp.covidcertificate.pdf.core.CertificateDrawHelper.Companion.FONT_SIZE

class RecoveryCertificateDrawHelper @Inject constructor(@OpenSansTypeFace font: Typeface) {

    val paint = Paint().apply {
        typeface = font
        textSize = FONT_SIZE
        color = FONT_COLOR
    }

    fun drawCertificateDetail(canvas: Canvas, certificate: RecoveryCertificate) {
        with(canvas) {
            save()
            rotate(180f, PdfGenerator.A4_WIDTH / 2f, PdfGenerator.A4_HEIGHT / 2f)
            drawTextIntoRectangle(certificate.targetDisease, paint, TextArea(453.41f, 489.40f, 129.55f))
            drawTextIntoRectangle(certificate.testedPositiveOnFormatted, paint, TextArea(453.41f, 560.17f, 129.55f))
            drawTextIntoRectangle(
                certificate.rawCertificate.recovery.certificateCountry,
                paint,
                TextArea(453.41f, 611.75f, 129.55f)
            )
            drawTextIntoRectangle(certificate.certificateIssuer, paint, TextArea(453.41f, 657.33f, 129.55f))
            drawTextIntoRectangle(certificate.validFromFormatted, paint, TextArea(453.41f, 701.71f, 129.55f))
            drawTextIntoRectangle(certificate.validUntilFormatted, paint, TextArea(453.41f, 741.29f, 129.55f))
            restore()
        }
    }
}
