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
            rotate(180f, PdfGenerator.PAGE_WIDTH / 2f, PdfGenerator.PAGE_HEIGHT / 2f)
            drawTextIntoRectangle(certificate.targetDisease, paint, TextArea(1876f, 2040f, 530f))
            drawTextIntoRectangle(certificate.vaccineTypeName, paint, TextArea(1876f, 2155f, 530f))
            drawTextIntoRectangle(certificate.medicalProductName, paint, TextArea(1300f, 2450f, 1100f))
            drawTextIntoRectangle(certificate.vaccineManufacturer, paint, TextArea(1300f, 2690f, 1100f))
            drawTextIntoRectangle(
                "${certificate.doseNumber}  ${certificate.totalSeriesOfDoses}",
                paint,
                TextArea(1876f, 2828f, 530f)
            )
            drawTextIntoRectangle(certificate.vaccinatedOnFormatted, paint, TextArea(1876f, 3030f, 530f))
            drawTextIntoRectangle(certificate.certificateCountry, paint, TextArea(1876f, 3160f, 530f))
            drawTextIntoRectangle(certificate.certificateIssuer, paint, TextArea(1876f, 3295f, 530f))
            restore()
        }
    }
}
