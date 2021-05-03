package de.rki.coronawarnapp.vaccination.core.repository.storage

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import de.rki.coronawarnapp.ui.Country
import de.rki.coronawarnapp.vaccination.core.VaccinatedPersonIdentifier
import de.rki.coronawarnapp.vaccination.core.VaccinationCertificate
import okio.ByteString
import org.joda.time.Instant
import org.joda.time.LocalDate

@Keep
data class VaccinationContainer(
    @SerializedName("certificate") val certificate: StoredCertificate,
    @SerializedName("certificateBase45") val certificateBase45: String,
    @SerializedName("certificateCBOR") val certificateCBOR: ByteString,
    @SerializedName("scannedAt") override val scannedAt: Instant,
) : VaccinationCertificate {

    override val personIdentifier: VaccinatedPersonIdentifier
        get() {
            val dob = certificate.dateOfBirth.toString()
            val lastName = certificate.lastNameStandardized
            val firstName = certificate.firstNameStandardized
            return "$dob#$lastName#$firstName"
        }

    override val firstName: String
        get() = certificate.firstName
    override val lastName: String
        get() = certificate.lastName
    override val dateOfBirth: LocalDate
        get() = certificate.dateOfBirth

    override val vaccinatedAt: LocalDate
        get() = certificate.vaccinatedAt

    override val vaccineName: String
        get() = TODO("Not yet implemented")
    override val vaccineManufacturer: String
        get() = TODO("Not yet implemented")
    override val medicalProductName: String
        get() = TODO("Not yet implemented")

    override val chargeId: String
        get() = certificate.chargeId
    override val certificateIssuer: String
        get() = certificate.certificateIssuer
    override val certificateCountry: Country
        get() = Country.values().singleOrNull {
            it.code == certificate.certificateCountryCode
        } ?: Country.DE
    override val certificateId: String
        get() = certificate.certificateId

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
    )
}

