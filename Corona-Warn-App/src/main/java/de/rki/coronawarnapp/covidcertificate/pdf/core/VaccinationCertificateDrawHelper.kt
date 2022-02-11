package de.rki.coronawarnapp.covidcertificate.pdf.core

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import de.rki.coronawarnapp.covidcertificate.pdf.core.CertificateDrawHelper.Companion.FONT_COLOR
import de.rki.coronawarnapp.covidcertificate.pdf.core.CertificateDrawHelper.Companion.FONT_SIZE
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import javax.inject.Inject

class VaccinationCertificateDrawHelper @Inject constructor(@OpenSansTypeFace font: Typeface) {

    val paint = Paint().apply {
        typeface = font
        textSize = FONT_SIZE
        color = FONT_COLOR
    }

    fun drawCertificateDetail(canvas: Canvas, certificate: VaccinationCertificate) {
        with(canvas) {
            save()
            rotate(180f, PdfGenerator.A4_WIDTH / 2f, PdfGenerator.A4_HEIGHT / 2f)
            drawTextIntoRectangle(certificate.targetDisease, paint, TextArea(450.05f, 489.40f, 127.15f))
            drawTextIntoRectangle(certificate.vaccineTypeName, paint, TextArea(450.05f, 516.98f, 127.15f))
            drawTextIntoRectangle(certificate.medicalProductName, paint, TextArea(311.87f, 587.75f, 263.89f))
            drawTextIntoRectangle(certificate.vaccineManufacturer, paint, TextArea(311.87f, 645.33f, 263.89f))
            drawTextIntoRectangle(
                "${certificate.doseNumber}  ${certificate.totalSeriesOfDoses}",
                paint,
                TextArea(450.05f, 678.44f, 127.15f)
            )
            drawTextIntoRectangle(certificate.vaccinatedOnFormatted, paint, TextArea(450.05f, 726.90f, 127.15f))
            drawTextIntoRectangle(
                certificate.rawCertificate.vaccination.certificateCountry,
                paint,
                TextArea(450.05f, 758.08f, 127.15f)
            )
            drawTextIntoRectangle(certificate.certificateIssuer, paint, TextArea(450.05f, 790.47f, 127.15f))
            restore()
        }
    }
}
