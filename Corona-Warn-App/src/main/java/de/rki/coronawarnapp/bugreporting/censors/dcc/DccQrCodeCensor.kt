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

    private val qrCodeFlow = MutableStateFlow<Set<String>>(value = emptySet())
    private val names = MutableStateFlow<Set<String>>(value = emptySet())
    private val dates = MutableStateFlow<Set<String>>(value = emptySet())
    private val countries = MutableStateFlow<Set<String>>(value = emptySet())
    private val identifiers = MutableStateFlow<Set<String>>(value = emptySet())
    private val issuers = MutableStateFlow<Set<String>>(value = emptySet())
    private val testDetails = MutableStateFlow<Set<String>>(value = emptySet())

    override suspend fun checkLog(message: String): CensorContainer? {
        var container = CensorContainer(message)

        qrCodeFlow.first().forEach {
            container = container.censor(it, "#qrCode" + it.takeLast(4))
        }
        identifiers.first().forEach {
            container = container.censor(it, "#identifier")
        }
        names.first().forEach {
            container = container.censor(it, "#name")
        }
        dates.first().forEach {
            container = container.censor(it, "#date")
        }

        countries.first().forEach {
            container = container.censor(" $it ", "#date")
            container = container.censor("\"${it}\"", "#date")
        }

        return container.nullIfEmpty()
    }

    fun addQRCodeStringToCensor(rawString: String) = qrCodeFlow.update {
        it.plus(rawString)
    }

    fun addCertificateToCensor(cert: DccData<out DccV1.MetaData>)  {
        dates.update {
            it.plus(cert.certificate.dateOfBirthFormatted)
        }

        censorNameData(cert.certificate.nameData)

        when (cert.certificate) {
            is VaccinationDccV1 -> censorVaccinationData(cert.certificate.vaccination)
            is TestDccV1 -> censorTestData(cert.certificate.test)
            is RecoveryDccV1 -> censorRecoveryData(cert.certificate.recovery)
        }
    }

    private fun censorRecoveryData(
        data: DccV1.RecoveryCertificateData
    ) {

        issuers.update {
            it.plus(data.certificateIssuer)
        }

        identifiers.update {
            it.plus(data.uniqueCertificateIdentifier)
        }

        countries.update {
            it.plus(data.certificateCountry)
        }

        dates.update {
            it
                .plus(data.testedPositiveOnFormatted)
                .plus(data.validFromFormatted)
                .plus(data.validUntilFormatted)
        }

        data.validUntil?.toShortDayFormat()?.let { validUntil ->
            dates.update {
                it.plus(validUntil)
            }
        }
    }

    private fun censorVaccinationData(
        data: DccV1.VaccinationData,
    ) {
        issuers.update {
            it.plus(data.certificateIssuer)
        }

        identifiers.update {
            it.plus(data.uniqueCertificateIdentifier)
        }

        countries.update {
            it.plus(data.certificateCountry)
        }

        identifiers.update {
            it.plus(data.marketAuthorizationHolderId)
        }

        identifiers.update {
            it.plus(data.medicalProductId)
        }

        identifiers.update {
            it.plus(data.targetId)
        }

        identifiers.update {
            it.plus(
                data.vaccineId
            )
        }

        dates.update {
            it.plus(data.vaccinatedOnFormatted).plus(data.dt)
        }
    }

    private fun censorTestData(
        data: DccV1.TestCertificateData,
    ) {

        issuers.update {
            it.plus(data.certificateIssuer)
        }

        identifiers.update {
            it.plus(data.uniqueCertificateIdentifier)
        }

        countries.update {
            it.plus(data.certificateCountry)
        }

        dates.update {
            it.plus(
                data.sampleCollectedAtFormatted,
            )
        }

        data.sampleCollectedAt?.toShortDayFormat()?.let { sampleCollectedAt ->
            dates.update {
                it.plus(
                    sampleCollectedAt,
                )
            }
        }

        identifiers.update {
            it.plus(data.targetId)
        }

        data.testCenter?.let {
            testDetails.update {
                it.plus(data.testCenter)
            }
        }

        testDetails.update {
            it.plus(data.testResult).plus(data.testType)
        }


        data.testName?.let {
            testDetails.update {
                it.plus(
                    data.testName
                )
            }
        }

        data.testNameAndManufacturer?.let {
            testDetails.update {
                it.plus(data.testNameAndManufacturer)
            }
        }
    }

    private fun censorNameData(nameData: DccV1.NameData) {
        nameData.familyName?.let { name ->
            names.update {
                it.plus(name)
            }
        }
        names.update {
            it.plus(nameData.familyNameStandardized)
        }
        nameData.givenName?.let { name ->
            names.update {
                it.plus(name)
            }
        }
        nameData.givenNameStandardized?.let { name ->
            names.update {
                it.plus(name)
            }
        }
    }
}
