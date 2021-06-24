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
import java.util.LinkedList
import javax.inject.Inject

@Reusable
class DccQrCodeCensor @Inject constructor() : BugCensor {

    override suspend fun checkLog(message: String): CensorContainer? {
        var newMessage = CensorContainer(message)

        synchronized(qrCodeStringsToCensor) { qrCodeStringsToCensor.toList() }.forEach {
            newMessage = newMessage.censor(it, PLACEHOLDER + it.takeLast(4))
        }

        synchronized(certsToCensor) { certsToCensor.toList() }.forEach {
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
            data.certificateCountry,
            "recovery/certificateCountry"
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

        newMessage = newMessage.censor(
            data.validUntil.toShortDayFormat(),
            "recovery/validFromFormatted"
        )

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
            vaccinationData.certificateCountry,
            "vaccination/certificateCountry"
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
            data.sampleCollectedAt.toString(),
            "test/sampleCollectedAt"
        )

        if (data.sampleCollectedAt.toString() != data.sc) {
            newMessage = newMessage.censor(
                data.sc,
                "test/sc"
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
            data.certificateCountry,
            "test/certificateCountry"
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
        private val qrCodeStringsToCensor = LinkedList<String>()

        fun addQRCodeStringToCensor(rawString: String) = synchronized(qrCodeStringsToCensor) {
            qrCodeStringsToCensor.apply {
                if (contains(rawString)) return@apply
                addFirst(rawString)
            }
        }

        fun clearQRCodeStringToCensor() = synchronized(qrCodeStringsToCensor) { qrCodeStringsToCensor.clear() }

        private val certsToCensor = LinkedList<DccData<out DccV1.MetaData>>()
        fun addCertificateToCensor(cert: DccData<out DccV1.MetaData>) = synchronized(certsToCensor) {
            certsToCensor.apply {
                if (contains(cert)) return@apply
                addFirst(cert)
            }
        }

        fun clearCertificateToCensor() = synchronized(certsToCensor) { certsToCensor.clear() }

        private const val PLACEHOLDER = "###"
    }
}
