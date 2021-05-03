package de.rki.coronawarnapp.vaccination.core.repository.storage

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import de.rki.coronawarnapp.ui.Country
import de.rki.coronawarnapp.vaccination.core.VaccinatedPersonIdentifier
import de.rki.coronawarnapp.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.vaccination.core.qrcode.VaccinationCertificateQRCode
import de.rki.coronawarnapp.vaccination.core.server.VaccinationValueSet
import org.joda.time.Instant
import org.joda.time.LocalDate

@Keep
data class VaccinationContainer(
    @SerializedName("certificate") val certificate: StoredCertificate,
    @SerializedName("certificateBase45") val certificateBase45: String,
    @SerializedName("certificateCBORBase64") val certificateCBORBase64: String,
    @SerializedName("scannedAt") val scannedAt: Instant,
) {

    val personIdentifier: VaccinatedPersonIdentifier
        get() = certificate.personIdentifier

    val certificateId: String
        get() = certificate.certificateId

    fun toVaccinationCertificate(valueSet: VaccinationValueSet?) = object : VaccinationCertificate {
        override val personIdentifier: VaccinatedPersonIdentifier
            get() = certificate.personIdentifier

        override val firstName: String
            get() = certificate.firstName
        override val lastName: String
            get() = certificate.lastName
        override val dateOfBirth: LocalDate
            get() = certificate.dateOfBirth

        override val vaccinatedAt: LocalDate
            get() = certificate.vaccinatedAt

        override val doseNumber: Int
            get() = certificate.doseNumber
        override val totalSeriesOfDoses: Int
            get() = certificate.totalSeriesOfDoses

        override val vaccineName: String
            get() = valueSet?.getDisplayText(certificate.vaccineId) ?: certificate.vaccineId
        override val vaccineManufacturer: String
            get() = valueSet?.getDisplayText(certificate.marketAuthorizationHolderId)
                ?: certificate.marketAuthorizationHolderId
        override val medicalProductName: String
            get() = valueSet?.getDisplayText(certificate.medicalProductId) ?: certificate.medicalProductId

        override val chargeId: String
            get() = certificate.chargeId

        override val certificateIssuer: String
            get() = certificate.certificateIssuer
        override val certificateCountry: Country
            get() = Country.values().singleOrNull { it.code == certificate.certificateCountryCode } ?: Country.DE
        override val certificateId: String
            get() = certificate.certificateId
    }

    @Keep
    data class StoredCertificate(
        @SerializedName("firstName") val firstName: String,
        @SerializedName("firstNameStandardized") val firstNameStandardized: String,
        @SerializedName("lastName") val lastName: String,
        @SerializedName("lastNameStandardized") val lastNameStandardized: String,

        @SerializedName("dateOfBirth") val dateOfBirth: LocalDate,

        @SerializedName("vaccinatedAt") val vaccinatedAt: LocalDate,
        @SerializedName("vaccinationLocation") val vaccinationLocation: String,

        @SerializedName("targetId") val targetId: String,
        @SerializedName("vaccineId") val vaccineId: String,
        @SerializedName("medicalProductId") val medicalProductId: String,

        @SerializedName("marketAuthorizationHolderId") val marketAuthorizationHolderId: String,

        @SerializedName("doseNumber") val doseNumber: Int,
        @SerializedName("totalSeriesOfDoses") val totalSeriesOfDoses: Int,

        @SerializedName("chargeId") val chargeId: String,
        @SerializedName("certificateIssuer") val certificateIssuer: String,
        @SerializedName("certificateCountryCode") val certificateCountryCode: String,
        @SerializedName("certificateId") val certificateId: String,
    ) {
        val personIdentifier: VaccinatedPersonIdentifier
            get() = VaccinatedPersonIdentifier(
                dateOfBirth = dateOfBirth,
                lastNameStandardized = lastNameStandardized,
                firstNameStandardized = firstNameStandardized,
            )
    }
}

fun VaccinationCertificateQRCode.toVaccinationContainer(scannedAt: Instant) = VaccinationContainer(
    certificate = VaccinationContainer.StoredCertificate(
        firstName = certificate.firstName,
        firstNameStandardized = certificate.firstNameStandardized,
        lastName = certificate.lastName,
        lastNameStandardized = certificate.lastNameStandardized,
        dateOfBirth = certificate.dateOfBirth,
        vaccinatedAt = certificate.vaccinatedAt,
        vaccinationLocation = certificate.vaccinationLocation,
        targetId = certificate.targetId,
        vaccineId = certificate.vaccineId,
        medicalProductId = certificate.medicalProductId,
        marketAuthorizationHolderId = certificate.marketAuthorizationHolderId,
        doseNumber = certificate.doseNumber,
        totalSeriesOfDoses = certificate.totalSeriesOfDoses,
        chargeId = certificate.chargeId,
        certificateIssuer = certificate.certificateIssuer,
        certificateCountryCode = certificate.certificateCountryCode,
        certificateId = certificate.certificateId
    ),
    certificateCBORBase64 = qrCodeOriginalCBOR.base64(),
    certificateBase45 = qrCodeOriginalBase45,
    scannedAt = scannedAt
)
