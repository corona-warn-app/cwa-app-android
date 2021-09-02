package de.rki.coronawarnapp.covidcertificate.pdf.core

import android.content.res.AssetManager
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificate
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.environment.presencetracing.qrcodeposter.QrCodePosterTemplate
import java.io.File
import javax.inject.Inject

class PdfTemplateRepository @Inject constructor(
    @CertificateExportCache private val cacheDir: File,
    private val assetManager: AssetManager,
) {

    fun getTemplate(certificate: CwaCovidCertificate): File {
        val templateName = getTemplateName(certificate)
        val file = getFileFromCache(templateName)
        if (!file.exists()) {
            copyAssetToCache(file, templateName)
        }
        return file
    }

    private fun getTemplateName(certificate: CwaCovidCertificate): String = when (certificate) {
        is VaccinationCertificate -> {
            VACCINATION_TEMPLATE_NAME
        }
        is RecoveryCertificate -> {
            RECOVERY_TEMPLATE_NAME
        }
        is TestCertificate -> {
            TEST_TEMPLATE_ASSET_NAME
        }
        else -> throw IllegalArgumentException("Certificate $certificate is not supported")
    }

    private fun getFileFromCache(templateName: String): File {
        val directory = File(cacheDir, "template").apply { if (!exists()) mkdirs() }
        return File(directory, templateName)
    }

    /*
     We have to copy template from asset folder to local app cache because [PdfRenderer] can only work with
     files in file system.
     */
    private fun copyAssetToCache(file: File, templateName: String) {
        val assetStream = assetManager.open(templateName)
        file.apply {
            outputStream().use {
                assetStream.copyTo(it)
            }
        }
    }

    companion object {
        const val VACCINATION_TEMPLATE_NAME = "vaccination_certificate_template.pdf"
        const val RECOVERY_TEMPLATE_NAME = "recovery_certificate_template.pdf"
        const val TEST_TEMPLATE_ASSET_NAME = "test_certificate_template.pdf"
    }
}
