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
            rotate(180f, PdfGenerator.PAGE_WIDTH / 2f, PdfGenerator.PAGE_HEIGHT / 2f)
            drawTextIntoRectangle(certificate.targetName, paint, TextArea(1985f, 2040f, 470f))
            drawTextIntoRectangle(certificate.testType, paint, TextArea(1985f, 2150f, 470f))
            drawTextIntoRectangle(certificate.testName ?: "", paint, TextArea(1310f, 2425f, 1100f))
            drawTextIntoRectangle(certificate.testNameAndManufacturer ?: "", paint, TextArea(1310f, 2620f, 1100f))
            drawTextIntoRectangle(certificate.sampleCollectedAtFormatted, paint, TextArea(1985f, 2815f, 470f))
            drawTextIntoRectangle(certificate.testResult, paint, TextArea(1985f, 2970f, 470f))
            drawTextIntoRectangle(certificate.testCenter ?: "", paint, TextArea(1985f, 3090f, 470f))
            drawTextIntoRectangle(certificate.certificateCountry, paint, TextArea(1985f, 3200f, 470f))
            drawTextIntoRectangle(certificate.certificateIssuer, paint, TextArea(1985f, 3315f, 470f))
            restore()
        }
    }
}
