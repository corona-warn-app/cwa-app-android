package de.rki.coronawarnapp.bugreporting.censors.vaccination

import dagger.Reusable
import de.rki.coronawarnapp.bugreporting.censors.BugCensor
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.Companion.toNewLogLineIfDifferent
import de.rki.coronawarnapp.bugreporting.debuglog.LogLine
import de.rki.coronawarnapp.vaccination.core.certificate.VaccinationDGCV1
import de.rki.coronawarnapp.vaccination.core.qrcode.VaccinationCertificateData
import java.util.LinkedList
import javax.inject.Inject

@Reusable
class CertificateQrCodeCensor @Inject constructor() : BugCensor {

    override suspend fun checkLog(entry: LogLine): LogLine? {
        var newMessage = entry.message

        synchronized(qrCodeStringsToCensor) { qrCodeStringsToCensor.toList() }.forEach {
            newMessage = newMessage.replace(
                it,
                PLACEHOLDER + it.takeLast(4)
            )
        }

        synchronized(certsToCensor) { certsToCensor.toList() }.forEach {
            it.certificate.apply {
                newMessage = newMessage.replace(
                    dob,
                    "vaccinationCertificate/dob"
                )

                newMessage = newMessage.replace(
                    dateOfBirth.toString(),
                    "vaccinationCertificate/dateOfBirth"
                )

                newMessage = censorNameData(nameData, newMessage)

                vaccinationDatas.forEach { data ->
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
        private val qrCodeStringsToCensor = LinkedList<String>()

        fun addQRCodeStringToCensor(rawString: String) = synchronized(qrCodeStringsToCensor) {
            qrCodeStringsToCensor.apply {
                if (contains(rawString)) return@apply
                addFirst(rawString)
                // Max certs is at 4, but we may scan invalid qr codes that are not added which will be shown in raw
                if (size > 8) removeLast()
            }
        }

        fun clearQRCodeStringToCensor() = synchronized(qrCodeStringsToCensor) { qrCodeStringsToCensor.clear() }

        private val certsToCensor = LinkedList<VaccinationCertificateData>()
        fun addCertificateToCensor(cert: VaccinationCertificateData) = synchronized(certsToCensor) {
            certsToCensor.apply {
                if (contains(cert)) return@apply
                addFirst(cert)
                // max certs we should have is 2, 50% leeway
                if (size > 4) removeLast()
            }
        }

        fun clearCertificateToCensor() = synchronized(certsToCensor) { certsToCensor.clear() }

        private const val PLACEHOLDER = "########-####-####-####-########"
    }
}
