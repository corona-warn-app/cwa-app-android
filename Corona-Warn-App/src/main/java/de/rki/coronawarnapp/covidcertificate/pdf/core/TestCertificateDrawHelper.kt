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
            restore()
        }
    }
}
