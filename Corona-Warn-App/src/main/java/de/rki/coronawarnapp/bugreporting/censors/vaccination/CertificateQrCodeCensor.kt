package de.rki.coronawarnapp.bugreporting.censors.vaccination

import dagger.Reusable
import de.rki.coronawarnapp.bugreporting.censors.BugCensor
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.Companion.toNewLogLineIfDifferent
import de.rki.coronawarnapp.bugreporting.debuglog.LogLine
import de.rki.coronawarnapp.vaccination.core.certificate.VaccinationDGCV1
import de.rki.coronawarnapp.vaccination.core.qrcode.VaccinationCertificateData
import javax.inject.Inject

@Reusable
class CertificateQrCodeCensor @Inject constructor() : BugCensor {

    override suspend fun checkLog(entry: LogLine): LogLine? {
        var newMessage = entry.message

        with(dataToCensor) {
            rawString?.let {
                newMessage = newMessage.replace(
                    it,
                    PLACEHOLDER + it.takeLast(4)
                )
            }

            certificateData?.certificate?.let {
                newMessage = newMessage.replace(
                    it.dob,
                    "vaccinationCertificate/dob"
                )

                newMessage = newMessage.replace(
                    it.dateOfBirth.toString(),
                    "vaccinationCertificate/dateOfBirth"
                )

                newMessage = censorNameData(it.nameData, newMessage)

                it.vaccinationDatas.forEach { data ->
                    newMessage = censorVaccinationData(data, newMessage)
                }
            }
        }

        return entry.toNewLogLineIfDifferent(newMessage)
    }

    private fun censorVaccinationData(
        vaccinationData: VaccinationDGCV1.VaccinationData,
        message: String
    ): String {
        var newMessage = message

        newMessage = newMessage.replace(
            vaccinationData.dt,
            "vaccinationData/dt"
        )

        newMessage = newMessage.replace(
            vaccinationData.marketAuthorizationHolderId,
            "vaccinationData/marketAuthorizationHolderId"
        )

        newMessage = newMessage.replace(
            vaccinationData.medicalProductId,
            "vaccinationData/medicalProductId"
        )

        newMessage = newMessage.replace(
            vaccinationData.targetId,
            "vaccinationData/targetId"
        )

        newMessage = newMessage.replace(
            vaccinationData.certificateIssuer,
            "vaccinationData/certificateIssuer"
        )

        newMessage = newMessage.replace(
            vaccinationData.uniqueCertificateIdentifier,
            "vaccinationData/uniqueCertificateIdentifier"
        )

        newMessage = newMessage.replace(
            vaccinationData.countryOfVaccination,
            "vaccinationData/countryOfVaccination"
        )

        newMessage = newMessage.replace(
            vaccinationData.vaccineId,
            "vaccinationData/vaccineId"
        )

        newMessage = newMessage.replace(
            vaccinationData.vaccinatedAt.toString(),
            "vaccinationData/vaccinatedAt"
        )

        return newMessage
    }

    private fun censorNameData(nameData: VaccinationDGCV1.NameData, message: String): String {
        var newMessage = message

        nameData.familyName?.let { fName ->
            newMessage = newMessage.replace(
                fName,
                "nameData/familyName"
            )
        }

        newMessage = newMessage.replace(
            nameData.familyNameStandardized,
            "nameData/familyNameStandardized"
        )

        nameData.givenName?.let { gName ->
            newMessage = newMessage.replace(
                gName,
                "nameData/givenName"
            )
        }

        nameData.givenNameStandardized?.let { gName ->
            newMessage = newMessage.replace(
                gName,
                "nameData/givenNameStandardized"
            )
        }

        return newMessage
    }

    companion object {
        var dataToCensor: CensorData = CensorData(
            rawString = null,
            certificateData = null
        )
        private const val PLACEHOLDER = "########-####-####-####-########"
    }

    data class CensorData(
        val rawString: String?,
        val certificateData: VaccinationCertificateData?
    )
}
