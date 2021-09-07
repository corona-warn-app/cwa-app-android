package de.rki.coronawarnapp.covidcertificate.pdf.core

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificate
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class PdfGenerator @Inject constructor(
    @CertificateExportCache private val cacheDir: File,
    private val pdfTemplateRepository: PdfTemplateRepository,
    private val certificateDrawHelper: CertificateDrawHelper,
    private val vaccinationCertificateDrawHelper: VaccinationCertificateDrawHelper,
    private val recoveryCertificateDrawHelper: RecoveryCertificateDrawHelper,
    private val testCertificateDrawHelper: TestCertificateDrawHelper,
) {

    fun createDgcPdf(certificate: CwaCovidCertificate, fileName: String): File {
        return File(cacheDir, fileName).also { file ->
            PdfDocument().apply {
                startPage(createEmptyPage()).apply {
                    drawTemplate(certificate)
                    drawCertificateDetails(certificate)
                    drawBasicInfo(certificate)
                    finishPage(this)
                }
                saveToFile(file)
            }
        }
    }

    private fun PdfDocument.saveToFile(file: File) {
        FileOutputStream(file).use {
            writeTo(it)
            close()
        }
    }

    private fun PdfDocument.Page.drawTemplate(certificate: CwaCovidCertificate) {
        val templateFile = pdfTemplateRepository.getTemplate(certificate)
        val templateBitmap = renderPdfFileToBitmap(templateFile, BitmapQuality.PRINT)
        canvas.drawBitmap(templateBitmap, Matrix().apply { setScale(0.25f, 0.25f) }, null)
    }

    private fun PdfDocument.Page.drawBasicInfo(certificate: CwaCovidCertificate) {
        certificateDrawHelper.drawCertificateDetail(canvas, certificate)
        certificateDrawHelper.drawQrCode(canvas, certificate)
    }

    private fun PdfDocument.Page.drawCertificateDetails(certificate: CwaCovidCertificate) {
        when (certificate) {
            is VaccinationCertificate -> vaccinationCertificateDrawHelper.drawCertificateDetail(canvas, certificate)
            is RecoveryCertificate -> recoveryCertificateDrawHelper.drawCertificateDetail(canvas, certificate)
            is TestCertificate -> testCertificateDrawHelper.drawCertificateDetail(canvas, certificate)
            else -> throw IllegalArgumentException("Certificate $certificate is not supported")
        }
    }

    private fun createEmptyPage(): PdfDocument.PageInfo = PdfDocument.PageInfo.Builder(A4_WIDTH, A4_HEIGHT, 1).create()

    fun renderPdfFileToBitmap(file: File, quality: BitmapQuality): Bitmap {
        val (pageWidth, pageHeight) = when (quality) {
            BitmapQuality.PRINT -> Pair(A4_WIDTH * 4, A4_HEIGHT * 4)
            BitmapQuality.PREVIEW -> Pair(A4_WIDTH * 2, A4_HEIGHT * 2)
        }
        return Bitmap.createBitmap(pageWidth, pageHeight, Bitmap.Config.ARGB_8888).also { bitmap ->
            val fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            val firstPage = PdfRenderer(fileDescriptor).openPage(0)
            firstPage.render(
                bitmap,
                null,
                null,
                if (quality == BitmapQuality.PRINT)
                    PdfRenderer.Page.RENDER_MODE_FOR_PRINT
                else
                    PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
            )
        }
    }

    enum class BitmapQuality {
        PRINT, PREVIEW
    }

    companion object {
        /**
         * A4 size in PostScript
         * @see <a href="https://www.cl.cam.ac.uk/~mgk25/iso-paper-ps.txt">Iso-paper-ps</a>
         */
        const val A4_WIDTH = 595 // PostScript
        const val A4_HEIGHT = 842 // PostScript
    }
}
