package de.rki.coronawarnapp.covidcertificate.pdf.core

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import javax.inject.Inject

class TestCertificateDrawHelper @Inject constructor(font: Typeface) {

    val paint = Paint().apply {
        typeface = font
        textSize = 35f
        color = CertificateDrawHelper.FONT_COLOR
    }

    fun drawCertificateDetail(canvas: Canvas, certificate: TestCertificate) {
        with(canvas) {
            save()
            rotate(180f, PdfGenerator.PAGE_WIDTH / 2f, PdfGenerator.PAGE_HEIGHT / 2f)
            // TODO:
            drawTextIntoRectangle("\$tg = ${certificate.targetName}", paint, TextArea(1990f, 2040f, 525f))
            drawTextIntoRectangle("\$tt = ${certificate.testType}", paint, TextArea(1990f, 2150f, 525f))
            drawTextIntoRectangle("\$nm = ${certificate.testName}", paint, TextArea(1310f, 2425f, 525f))
            drawTextIntoRectangle("\$ma = ${certificate.testNameAndManufacturer}", paint, TextArea(1310f, 2620f, 525f))
            drawTextIntoRectangle(
                "\$sc = ${certificate.sampleCollectedAtFormatted}",
                paint,
                TextArea(1990f, 2815f, 525f)
            )
            drawTextIntoRectangle("\$tr = ${certificate.testResult}", paint, TextArea(1990f, 2970f, 525f))
            drawTextIntoRectangle("\$tc = ${certificate.testCenter}", paint, TextArea(1990f, 3090f, 525f))
            drawTextIntoRectangle("\$co = ${certificate.certificateCountry}", paint, TextArea(1990f, 3200f, 525f))
            drawTextIntoRectangle("\$is = ${certificate.certificateIssuer}", paint, TextArea(1990f, 3315f, 525f))
            restore()
        }
    }
}
