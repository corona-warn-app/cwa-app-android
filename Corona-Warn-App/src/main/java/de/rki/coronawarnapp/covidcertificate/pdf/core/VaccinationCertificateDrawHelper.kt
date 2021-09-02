package de.rki.coronawarnapp.covidcertificate.pdf.core

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import javax.inject.Inject

class VaccinationCertificateDrawHelper @Inject constructor(@OpenSansTypeFace font: Typeface) {

    val paint = Paint().apply {
        typeface = font
        textSize = 34f
        color = CertificateDrawHelper.FONT_COLOR
    }

    fun drawCertificateDetail(canvas: Canvas, certificate: VaccinationCertificate) {
        with(canvas) {
            save()
            rotate(180f, PdfGenerator.PAGE_WIDTH / 2f, PdfGenerator.PAGE_HEIGHT / 2f)
            drawTextIntoRectangle("\$tg = ${certificate.targetDisease}", paint, TextArea(1895f, 2040f, 525f))
            drawTextIntoRectangle("\$vp = ${certificate.vaccineTypeName}", paint, TextArea(1895f, 2155f, 525f))
            drawTextIntoRectangle(
                "\$mp = ${certificate.medicalProductName}",
                paint,
                TextArea(1300f, 2450f, 525f)
            )
            drawTextIntoRectangle(
                "\$ma = ${certificate.vaccineManufacturer}",
                paint,
                TextArea(1300f, 2690f, 525f)
            )
            drawTextIntoRectangle(
                "\$dn/\$sd = ${certificate.doseNumber}/${certificate.totalSeriesOfDoses}",
                paint,
                TextArea(1895f, 2830f, 525f)
            )
            drawTextIntoRectangle(
                "\$dt = ${certificate.vaccinatedOnFormatted}",
                paint,
                TextArea(1895f, 3030f, 525f)
            )
            drawTextIntoRectangle(
                "\$co = ${certificate.certificateCountry}",
                paint,
                TextArea(1895f, 3160f, 525f)
            )
            drawTextIntoRectangle(
                "\$is = ${certificate.certificateIssuer}",
                paint,
                TextArea(1895f, 3295f, 525f)
            )
            restore()
        }
    }
}
