package de.rki.coronawarnapp.bugreporting.censors.dcc

import dagger.Reusable
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

@Reusable
class DccQrCodeCensor @Inject constructor() : BugCensor {

    override suspend fun checkLog(message: String): CensorContainer? {
        var newMessage = CensorContainer(message)

        qrCodeFlow.first().forEach {
            newMessage = newMessage.censor(it, PLACEHOLDER + it.takeLast(4))
        }

        certificateFlow.first().forEach {
            it.certificate.apply {
                newMessage = newMessage.censor(
                    dateOfBirthFormatted,
                    "dcc/dateOfBirth"
                )

                newMessage = censorNameData(nameData, newMessage)

                newMessage = when (it.certificate) {
                    is VaccinationDccV1 -> censorVaccinationData(it.certificate.vaccination, newMessage)
                    is TestDccV1 -> censorTestData(it.certificate.test, newMessage)
                    is RecoveryDccV1 -> censorRecoveryData(it.certificate.recovery, newMessage)
                    else -> newMessage
                }
            }
        }

        return newMessage.nullIfEmpty()
    }

    private fun censorRecoveryData(
        data: DccV1.RecoveryCertificateData,
        message: CensorContainer
    ): CensorContainer {
        var newMessage = message
        newMessage = newMessage.censor(
            data.certificateIssuer,
            "recovery/certificateIssuer"
        )

        newMessage = newMessage.censor(
            data.uniqueCertificateIdentifier,
            "recovery/uniqueCertificateIdentifier"
        )

        newMessage = newMessage.censor(
            " ${data.certificateCountry} ",
            " recovery/certificateCountry "
        )

        newMessage = newMessage.censor(
            "\"${data.certificateCountry}\"",
            "\"recovery/certificateCountry\""
        )

        newMessage = newMessage.censor(
            data.testedPositiveOnFormatted,
            "recovery/testedPositiveOnFormatted"
        )

        newMessage = newMessage.censor(
            data.validFromFormatted,
            "recovery/validFromFormatted"
        )

        newMessage = newMessage.censor(
            data.validUntilFormatted,
            "recovery/validUntilFormatted"
        )

        data.validUntil?.toShortDayFormat()?.let { validUntil ->
            newMessage = newMessage.censor(
                validUntil,
                "recovery/validFromFormatted"
            )
        }

        return newMessage
    }

    private fun censorVaccinationData(
        vaccinationData: DccV1.VaccinationData,
        message: CensorContainer
    ): CensorContainer {
        var newMessage = message

        newMessage = newMessage.censor(
            vaccinationData.marketAuthorizationHolderId,
            "vaccination/marketAuthorizationHolderId"
        )

        newMessage = newMessage.censor(
            vaccinationData.medicalProductId,
            "vaccination/medicalProductId"
        )

        newMessage = newMessage.censor(
            vaccinationData.targetId,
            "vaccination/targetId"
        )

        newMessage = newMessage.censor(
            vaccinationData.certificateIssuer,
            "vaccination/certificateIssuer"
        )

        newMessage = newMessage.censor(
            vaccinationData.uniqueCertificateIdentifier,
            "vaccination/uniqueCertificateIdentifier"
        )

        newMessage = newMessage.censor(
            " ${vaccinationData.certificateCountry} ",
            " vaccination/certificateCountry "
        )

        newMessage = newMessage.censor(
            "\"${vaccinationData.certificateCountry}\"",
            "\"vaccination/certificateCountry\""
        )

        newMessage = newMessage.censor(
            vaccinationData.vaccineId,
            "vaccination/vaccineId"
        )

        val vaccinatedOn = vaccinationData.vaccinatedOnFormatted
        newMessage = newMessage.censor(
            vaccinatedOn,
            "vaccination/vaccinatedOnFormatted"
        )
        if (vaccinatedOn != vaccinationData.dt) {
            newMessage = newMessage.censor(
                vaccinationData.dt,
                "vaccination/dt"
            )
        }

        return newMessage
    }

    private fun censorTestData(
        data: DccV1.TestCertificateData,
        message: CensorContainer
    ): CensorContainer {
        var newMessage = message

        data.testCenter?.let {
            newMessage = newMessage.censor(
                data.testCenter,
                "test/testCenter"
            )
        }

        newMessage = newMessage.censor(
            data.testResult,
            "test/testResult"
        )

        newMessage = newMessage.censor(
            data.testType,
            "test/testType"
        )

        data.testName?.let {
            newMessage = newMessage.censor(
                data.testName,
                "test/testName"
            )
        }

        data.testNameAndManufacturer?.let {
            newMessage = newMessage.censor(
                data.testNameAndManufacturer,
                "test/testNameAndManufacturer"
            )
        }

        newMessage = newMessage.censor(
            data.sampleCollectedAtFormatted,
            "test/sampleCollectedAtFormatted"
        )

        data.sampleCollectedAt?.toShortDayFormat()?.let { sampleCollectedAt ->
            newMessage = newMessage.censor(
                sampleCollectedAt,
                "test/sampleCollectedAt"
            )
        }

        newMessage = newMessage.censor(
            data.targetId,
            "test/targetId"
        )

        newMessage = newMessage.censor(
            data.certificateIssuer,
            "test/certificateIssuer"
        )

        newMessage = newMessage.censor(
            data.uniqueCertificateIdentifier,
            "test/uniqueCertificateIdentifier"
        )

        newMessage = newMessage.censor(
            " ${data.certificateCountry} ",
            " test/certificateCountry "
        )

        newMessage = newMessage.censor(
            "\"${data.certificateCountry}\"",
            "\"test/certificateCountry\""
        )

        return newMessage
    }

    private fun censorNameData(nameData: DccV1.NameData, message: CensorContainer): CensorContainer {
        var newMessage = message

        nameData.familyName?.let { fName ->
            newMessage = newMessage.censor(
                fName,
                "nameData/familyName"
            )
        }

        newMessage = newMessage.censor(
            nameData.familyNameStandardized,
            "nameData/familyNameStandardized"
        )

        nameData.givenName?.let { gName ->
            newMessage = newMessage.censor(
                gName,
                "nameData/givenName"
            )
        }

        nameData.givenNameStandardized?.let { gName ->
            newMessage = newMessage.censor(
                gName,
                "nameData/givenNameStandardized"
            )
        }

        return newMessage
    }

    companion object {

        private val qrCodeFlow = MutableStateFlow<Set<String>>(value = emptySet())
        private val certificateFlow = MutableStateFlow<Set<DccData<out DccV1.MetaData>>>(value = emptySet())

        fun addQRCodeStringToCensor(rawString: String) = qrCodeFlow.update {
            it.plus(rawString)
        }

        fun clearQRCodeStringToCensor() = qrCodeFlow.update { emptySet() }

        fun addCertificateToCensor(cert: DccData<out DccV1.MetaData>) = certificateFlow.update {
            it.plus(cert)
        }

        fun clearCertificateToCensor() = certificateFlow.update { emptySet() }

        private const val PLACEHOLDER = "###"
    }
}
