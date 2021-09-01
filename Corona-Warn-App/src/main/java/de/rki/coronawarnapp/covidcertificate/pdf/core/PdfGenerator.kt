package de.rki.coronawarnapp.covidcertificate.pdf.core

import android.graphics.Bitmap
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificate
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.environment.presencetracing.qrcodeposter.QrCodePosterTemplate
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class PdfGenerator @Inject constructor(
    @QrCodePosterTemplate private val cacheDir: File,
    private val pdfTemplateRepository: PdfTemplateRepository,
    private val certificateDrawHelper: CertificateDrawHelper,
    private val vaccinationCertificateDrawHelper: VaccinationCertificateDrawHelper,
    private val recoveryCertificateDrawHelper: RecoveryCertificateDrawHelper,
    private val testCertificateDrawHelper: TestCertificateDrawHelper,
) {

    fun createDgcPdf(certificate: CwaCovidCertificate): File {
        return File(cacheDir, "test.pdf").also { file ->
            PdfDocument().apply {
                startPage(createEmptyPage()).apply {
                    drawTemplate(certificate)
                    drawBasicInfo(certificate)
                    drawCertificateDetails(certificate)
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
        val templateBitmap = renderPdfFileToBitmap(templateFile)
        canvas.drawBitmap(templateBitmap, 0f, 0f, null)
    }

    private fun PdfDocument.Page.drawBasicInfo(certificate: CwaCovidCertificate) {
        certificateDrawHelper.drawCertificateDetail(canvas, certificate)
        certificateDrawHelper.drawQrCode(canvas, certificate)
    }

    private fun PdfDocument.Page.drawCertificateDetails(certificate: CwaCovidCertificate) {
        when (certificate) {
            is VaccinationCertificate -> {
                vaccinationCertificateDrawHelper.drawCertificateDetail(canvas, certificate)
            }
            is RecoveryCertificate -> {
                recoveryCertificateDrawHelper.drawCertificateDetail(canvas, certificate)
            }
            is TestCertificate -> {
                testCertificateDrawHelper.drawCertificateDetail(canvas, certificate)
            }
            else -> throw IllegalArgumentException("Certificate $certificate is not supported")
        }
    }

    private fun createEmptyPage(): PdfDocument.PageInfo =
        PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()

    private fun renderPdfFileToBitmap(file: File): Bitmap {
        return Bitmap.createBitmap(PAGE_WIDTH, PAGE_HEIGHT, Bitmap.Config.ARGB_8888).also { bitmap ->
            val fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            val firstPage = PdfRenderer(fileDescriptor).openPage(0)
            firstPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)
        }
    }

    companion object {
        // 300ppi A4 = 2480x3508
        const val PAGE_WIDTH = 2480
        const val PAGE_HEIGHT = 3508
    }
}
