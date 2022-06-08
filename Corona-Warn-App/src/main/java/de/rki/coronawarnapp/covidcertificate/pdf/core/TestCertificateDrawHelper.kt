package de.rki.coronawarnapp.covidcertificate.pdf.core

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import de.rki.coronawarnapp.covidcertificate.pdf.core.CertificateDrawHelper.Companion.FONT_COLOR
import de.rki.coronawarnapp.covidcertificate.pdf.core.CertificateDrawHelper.Companion.FONT_SIZE
import javax.inject.Inject

class TestCertificateDrawHelper @Inject constructor(@OpenSansTypeFace font: Typeface) {

    val paint = Paint().apply {
        typeface = font
        textSize = FONT_SIZE
        color = FONT_COLOR
    }

    fun drawCertificateDetail(canvas: Canvas, certificate: TestCertificate) {
        with(canvas) {
            save()
            rotate(180f, PdfGenerator.A4_WIDTH / 2f, PdfGenerator.A4_HEIGHT / 2f)
            drawTextIntoRectangle(certificate.targetDisease, paint, TextArea(476.20f, 489.40f, 112.75f))
            drawTextIntoRectangle(certificate.testType, paint, TextArea(476.20f, 515.79f, 112.75f))
            drawTextIntoRectangle(certificate.testName ?: "", paint, TextArea(314.27f, 581.76f, 236.89f))
            drawTextIntoRectangle(certificate.testNameAndManufacturer ?: "", paint, TextArea(314.27f, 628.54f, 236.89f))
            drawTextIntoRectangle(
                certificate.sampleCollectedAt?.toString() ?: "",
                paint,
                TextArea(476.20f, 675.32f, 112.75f)
            )
            drawTextIntoRectangle(certificate.testResult, paint, TextArea(476.20f, 712.50f, 112.75f))
            drawTextIntoRectangle(certificate.testCenter ?: "", paint, TextArea(476.20f, 741.29f, 112.75f))
            drawTextIntoRectangle(
                certificate.rawCertificate.test.certificateCountry,
                paint,
                TextArea(476.20f, 767.68f, 112.75f)
            )
            drawTextIntoRectangle(certificate.certificateIssuer, paint, TextArea(476.20f, 795.27f, 112.75f))
            restore()
        }
    }
}
