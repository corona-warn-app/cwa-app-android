package de.rki.coronawarnapp.covidcertificate.pdf.ui.exportAll

import android.graphics.Bitmap
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.encoder.Encoder
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.pdf.core.toBitmap
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificate
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.util.encoding.base64
import java.io.ByteArrayOutputStream
import java.lang.UnsupportedOperationException

internal fun String.injectData(
    certificate: CwaCovidCertificate
): String = this
    .replace("\$nam", certificate.fullNameFormatted.sanitize())
    .replace("\$dob", certificate.dateOfBirthFormatted.sanitize())
    .replace("\$ci", certificate.uniqueCertificateIdentifier.sanitize())
    .replace("\$tg", certificate.targetDisease.sanitize())
    .replace("\$co", certificate.certificateCountry.sanitize())
    .replace("\$qr", certificate.qrCodeBase64())
    .replace("\$is", certificate.certificateIssuer.sanitize())
    .replaceFieldsOf(certificate)

private fun String.replaceFieldsOf(certificate: CwaCovidCertificate) = when (certificate) {
    is VaccinationCertificate -> replace("\$vp", certificate.vaccineTypeName.sanitize())
        .replace("\$mp", certificate.medicalProductName.sanitize())
        .replace("\$ma", certificate.vaccineManufacturer.sanitize())
        .replace("\$dn", certificate.doseNumber.toString())
        .replace("\$sd", certificate.totalSeriesOfDoses.toString())
        .replace("\$dt", certificate.vaccinatedOnFormatted)

    is RecoveryCertificate -> replace("\$fr", certificate.testedPositiveOnFormatted)
        .replace("\$df", certificate.validFromFormatted)
        .replace("\$du", certificate.validUntilFormatted)

    is TestCertificate -> replace("\$tt", certificate.testType)
        .replace("\$nm", certificate.testName.orEmpty())
        .replace("\$ma", certificate.testNameAndManufacturer.orEmpty().sanitize())
        .replace("\$sc", certificate.sampleCollectedAtFormatted)
        .replace("\$tr", certificate.testResult.sanitize())
        .replace("\$tc", certificate.testCenter.toString())

    else -> throw UnsupportedOperationException("${certificate::class.simpleName} isn't supported")
}

private fun String.sanitize(): String = this
    .replace("&", "&amp;")
    .replace("<", "&lt;")
    .replace(">", "&gt;")
    .replace("\"", "&quot;")
    .replace("'", "&apos;")

private fun CwaCovidCertificate.qrCodeBase64(): String {
    val hints = mapOf(
        /**
         * We cannot use Charsets.UTF_8 as zxing calls toString internally
         * and some android version return the class name and not the charset name
         */
        EncodeHintType.CHARACTER_SET to qrCodeToDisplay.options.characterSet.name()
    )

    val qrCode = Encoder.encode(
        qrCodeToDisplay.content,
        qrCodeToDisplay.options.correctionLevel,
        hints,
    )

    Bitmap.createScaledBitmap(
        qrCode.matrix.toBitmap(),
        625,
        625,
        false
    ).run {
        val outputStream = ByteArrayOutputStream()
        compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        return outputStream.toByteArray().base64()
    }
}
