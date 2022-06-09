package de.rki.coronawarnapp.covidcertificate.pdf.core

import android.graphics.Bitmap
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.encoder.Encoder
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.pdf.ui.toBitmap
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificate
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.util.encoding.base64
import java.io.ByteArrayOutputStream
import java.lang.UnsupportedOperationException

internal fun String.inject(
    cert: CwaCovidCertificate
): String = this
    .replace("\$nam", cert.fullNameFormatted.sanitize())
    .replace("\$dob", cert.dateOfBirthFormatted.sanitize())
    .replace("\$ci", cert.uniqueCertificateIdentifier.sanitize())
    .replace("\$tg", cert.targetDisease.sanitize())
    .replace("\$co", cert.rawCertificate.payload.certificateCountry.sanitize())
    .replace("\$qr", cert.qrCodeBase64())
    .replace("\$is", cert.certificateIssuer.sanitize())
    .replaceFieldsOf(cert)

private fun String.replaceFieldsOf(cert: CwaCovidCertificate) = when (cert) {
    is VaccinationCertificate -> replace("\$vp", cert.vaccineTypeName.sanitize())
        .replace("\$mp", cert.medicalProductName.sanitize())
        .replace("\$ma", cert.vaccineManufacturer.sanitize())
        .replace("\$dn", cert.doseNumber.toString())
        .replace("\$sd", cert.totalSeriesOfDoses.toString())
        .replace("\$dt", cert.vaccinatedOnFormatted)

    is RecoveryCertificate -> replace("\$fr", cert.testedPositiveOnFormatted)
        .replace("\$df", cert.validFromFormatted)
        .replace("\$du", cert.validUntilFormatted)

    is TestCertificate -> replace("\$tt", cert.testType)
        .replace("\$nm", cert.testName.orEmpty())
        .replace("\$ma", cert.testNameAndManufacturer.orEmpty().sanitize())
        .replace("\$sc", cert.sampleCollectedAtFormatted)
        .replace("\$tr", cert.testResult.sanitize())
        .replace("\$tc", cert.testCenter.toString())

    else -> throw UnsupportedOperationException("${cert::class.simpleName} isn't supported")
}

private fun String.sanitize(): String = this
    .replace("&", "&amp;")
    .replace("<", "&lt;")
    .replace(">", "&gt;")
    .replace("\"", "&quot;")
    .replace("'", "&apos;")

internal fun CwaCovidCertificate.qrCodeBase64(): String {
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
        ByteArrayOutputStream().use {
            compress(Bitmap.CompressFormat.PNG, 100, it)
            return it.toByteArray().base64()
        }
    }
}
