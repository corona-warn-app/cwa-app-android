package de.rki.coronawarnapp.bugreporting.censors.dcc

import de.rki.coronawarnapp.bugreporting.censors.BugCensor
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.CensorContainer
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccData
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccV1
import de.rki.coronawarnapp.covidcertificate.common.certificate.RecoveryDccV1
import de.rki.coronawarnapp.covidcertificate.common.certificate.TestDccV1
import de.rki.coronawarnapp.covidcertificate.common.certificate.VaccinationDccV1
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toShortDayFormat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DccQrCodeCensor @Inject constructor() : BugCensor {

    private val state = MutableStateFlow(Data())

    override suspend fun checkLog(message: String): CensorContainer? {
        var container = CensorContainer(message)

        with(state.first()) {
            qrCodes.forEach {
                container = container.censor(it, "#qrCode" + it.takeLast(4))
            }
            identifiers.forEach {
                container = container.censor(it, "#identifier")
            }
            names.forEach {
                container = container.censor(it, "#name")
            }
            dates.forEach {
                container = container.censor(it, "#date")
            }
            countries.forEach {
                container = container.censor(" $it ", "#country")
                container = container.censor("\"${it}\"", "#country")
            }
            testDetails.forEach {
                container = container.censor(it, "#testDetail")
            }
            issuers.forEach {
                container = container.censor(it, "#issuer")
            }
        }

        return container.nullIfEmpty()
    }

    fun addQRCodeStringToCensor(rawString: String) = state.edit {
        qrCodes += rawString
    }

    fun addCertificateToCensor(cert: DccData<out DccV1.MetaData>) = state.edit {
        dates += cert.certificate.dateOfBirthFormatted

        addNameData(cert.certificate.nameData)

        when (cert.certificate) {
            is VaccinationDccV1 -> addVaccinationData(cert.certificate.vaccination)
            is TestDccV1 -> addTestData(cert.certificate.test)
            is RecoveryDccV1 -> addRecoveryData(cert.certificate.recovery)
        }
    }

    private fun MutableData.addRecoveryData(data: DccV1.RecoveryCertificateData) = with(data) {
        issuers += certificateIssuer
        identifiers += uniqueCertificateIdentifier
        countries += certificateCountry

        dates += testedPositiveOnFormatted
        dates += validFromFormatted
        dates += validUntilFormatted

        validUntil?.let {
            dates += it.toShortDayFormat()
        }
    }

    private fun MutableData.addVaccinationData(data: DccV1.VaccinationData) = with(data) {
        issuers += certificateIssuer
        countries += certificateCountry

        identifiers += uniqueCertificateIdentifier
        identifiers += marketAuthorizationHolderId
        identifiers += medicalProductId
        identifiers += targetId
        identifiers += vaccineId

        dates += vaccinatedOnFormatted
        dates += dt
    }

    private fun MutableData.addTestData(data: DccV1.TestCertificateData) = with(data) {
        issuers += certificateIssuer
        countries += certificateCountry

        dates += sampleCollectedAtFormatted
        sampleCollectedAt?.let {
            dates += it.toShortDayFormat()
        }

        identifiers += uniqueCertificateIdentifier
        identifiers += targetId

        testCenter?.let { testDetails += it }
        testDetails += testResult
        testDetails += testType
        testName?.let { testDetails += it }
        testNameAndManufacturer?.let { testDetails += it }
    }

    private fun MutableData.addNameData(nameData: DccV1.NameData) = with(nameData) {
        names += familyNameStandardized
        familyName?.let { names += it }
        givenName?.let { names += it }
        givenNameStandardized?.let { names += it }
    }
}

private data class Data(
    val qrCodes: Set<String> = emptySet(),
    val names: Set<String> = emptySet(),
    val dates: Set<String> = emptySet(),
    val countries: Set<String> = emptySet(),
    val identifiers: Set<String> = emptySet(),
    val issuers: Set<String> = emptySet(),
    val testDetails: Set<String> = emptySet()
)

private data class MutableData(
    val qrCodes: MutableSet<String>,
    val names: MutableSet<String>,
    val dates: MutableSet<String>,
    val countries: MutableSet<String>,
    val identifiers: MutableSet<String>,
    val issuers: MutableSet<String>,
    val testDetails: MutableSet<String>
)

private fun Data.toMutableData() = MutableData(
    qrCodes = qrCodes.toMutableSet(),
    names = names.toMutableSet(),
    dates = dates.toMutableSet(),
    countries = countries.toMutableSet(),
    identifiers = identifiers.toMutableSet(),
    issuers = issuers.toMutableSet(),
    testDetails = testDetails.toMutableSet()
)

private fun MutableData.toData() = Data(
    qrCodes = qrCodes,
    names = names,
    dates = dates,
    countries = countries,
    identifiers = identifiers,
    issuers = issuers,
    testDetails = testDetails
)

private fun MutableStateFlow<Data>.edit(transform: MutableData.() -> Unit) = update {
    it.toMutableData().apply { transform(this) }.toData()
}
