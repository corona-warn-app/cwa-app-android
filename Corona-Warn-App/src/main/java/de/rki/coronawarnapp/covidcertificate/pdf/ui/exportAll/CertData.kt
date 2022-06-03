package de.rki.coronawarnapp.covidcertificate.pdf.ui.exportAll

import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificate
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import java.lang.UnsupportedOperationException

internal fun String.injectData(
    certificate: CwaCovidCertificate,
    qrCodeBase64: String,
): String = this
    .replace("\$nam", certificate.fullNameStandardizedFormatted.sanitize())
    .replace("\$dob", certificate.dateOfBirthFormatted.sanitize())
    .replace("\$ci", certificate.uniqueCertificateIdentifier.sanitize())
    .replace("\$tg", certificate.rawCertificate.payload.targetId.sanitize())
    .replace("\$co", certificate.certificateCountry.sanitize())
    .replace("\$qr", qrCodeBase64)
    .replace("\$is", certificate.certificateIssuer.sanitize())
    .replaceFieldsOf(certificate)

private fun String.replaceFieldsOf(certificate: CwaCovidCertificate) = when (certificate) {
    is VaccinationCertificate ->
        this
            .replace("\$vp", certificate.vaccineTypeName.sanitize())
            .replace("\$mp", certificate.medicalProductName.sanitize())
            .replace("\$ma", certificate.vaccineManufacturer.sanitize())
            .replace("\$dn", certificate.doseNumber.toString())
            .replace("\$sd", certificate.totalSeriesOfDoses.toString())
            .replace("\$dt", certificate.vaccinatedOnFormatted)

    is RecoveryCertificate ->
        this
            .replace("\$fr", certificate.testedPositiveOnFormatted)
            .replace("\$df", certificate.validFromFormatted)
            .replace("\$du", certificate.validUntilFormatted)

    is TestCertificate ->
        this
            .replace("\$tt", certificate.testType)
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
