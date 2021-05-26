package de.rki.coronawarnapp.bugreporting.censors.vaccination

import dagger.Reusable
import de.rki.coronawarnapp.bugreporting.censors.BugCensor
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.CensoredString
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.Companion.censor
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.Companion.plus
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.Companion.toNullIfUnmodified
import de.rki.coronawarnapp.vaccination.core.certificate.VaccinationDGCV1
import de.rki.coronawarnapp.vaccination.core.qrcode.VaccinationCertificateData
import java.util.LinkedList
import javax.inject.Inject

@Reusable
class CertificateQrCodeCensor @Inject constructor() : BugCensor {

    override suspend fun checkLog(message: String): CensoredString? {
        var newMessage = CensoredString.fromOriginal(message)

        synchronized(qrCodeStringsToCensor) { qrCodeStringsToCensor.toList() }.forEach {
            newMessage += newMessage.censor(it, PLACEHOLDER + it.takeLast(4))
        }

        synchronized(certsToCensor) { certsToCensor.toList() }.forEach {
            it.certificate.apply {
                newMessage += newMessage.censor(
                    dob,
                    "vaccinationCertificate/dob"
                )

                newMessage += newMessage.censor(
                    dateOfBirth.toString(),
                    "vaccinationCertificate/dateOfBirth"
                )

                newMessage += censorNameData(nameData, newMessage)

                vaccinationDatas.forEach { data ->
                    newMessage += censorVaccinationData(data, newMessage)
                }
            }
        }

        return newMessage.toNullIfUnmodified()
    }

    private fun censorVaccinationData(
        vaccinationData: VaccinationDGCV1.VaccinationData,
        message: CensoredString
    ): CensoredString {
        var newMessage = message

        newMessage += newMessage.censor(
            vaccinationData.dt,
            "vaccinationData/dt"
        )

        newMessage += newMessage.censor(
            vaccinationData.marketAuthorizationHolderId,
            "vaccinationData/marketAuthorizationHolderId"
        )

        newMessage += newMessage.censor(
            vaccinationData.medicalProductId,
            "vaccinationData/medicalProductId"
        )

        newMessage += newMessage.censor(
            vaccinationData.targetId,
            "vaccinationData/targetId"
        )

        newMessage += newMessage.censor(
            vaccinationData.certificateIssuer,
            "vaccinationData/certificateIssuer"
        )

        newMessage += newMessage.censor(
            vaccinationData.uniqueCertificateIdentifier,
            "vaccinationData/uniqueCertificateIdentifier"
        )

        newMessage += newMessage.censor(
            vaccinationData.countryOfVaccination,
            "vaccinationData/countryOfVaccination"
        )

        newMessage += newMessage.censor(
            vaccinationData.vaccineId,
            "vaccinationData/vaccineId"
        )

        newMessage += newMessage.censor(
            vaccinationData.vaccinatedAt.toString(),
            "vaccinationData/vaccinatedAt"
        )

        return newMessage
    }

    private fun censorNameData(nameData: VaccinationDGCV1.NameData, message: CensoredString): CensoredString {
        var newMessage = message

        nameData.familyName?.let { fName ->
            newMessage += newMessage.censor(
                fName,
                "nameData/familyName"
            )
        }

        newMessage += newMessage.censor(
            nameData.familyNameStandardized,
            "nameData/familyNameStandardized"
        )

        nameData.givenName?.let { gName ->
            newMessage += newMessage.censor(
                gName,
                "nameData/givenName"
            )
        }

        nameData.givenNameStandardized?.let { gName ->
            newMessage += newMessage.censor(
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
