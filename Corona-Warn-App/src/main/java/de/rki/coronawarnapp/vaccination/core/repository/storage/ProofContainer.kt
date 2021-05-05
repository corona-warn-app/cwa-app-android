package de.rki.coronawarnapp.vaccination.core.repository.storage

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import de.rki.coronawarnapp.vaccination.core.ProofCertificate
import de.rki.coronawarnapp.vaccination.core.VaccinatedPersonIdentifier
import de.rki.coronawarnapp.vaccination.core.server.ProofCertificateServerData
import de.rki.coronawarnapp.vaccination.core.server.VaccinationValueSet
import okio.ByteString
import org.joda.time.Instant
import org.joda.time.LocalDate

@Keep
data class ProofContainer(
    @SerializedName("proof") val proof: StoredProof,
    @SerializedName("expiresAt") val expiresAt: Instant,
    @SerializedName("issuedAt") val issuedAt: Instant,
    @SerializedName("issuedBy") val issuedBy: String,
    @SerializedName("proofCOSE") val proofCOSE: ByteString,
) {

    val personIdentifier: VaccinatedPersonIdentifier
        get() = proof.personIdentifier

    fun toProofCertificate(valueSet: VaccinationValueSet?): ProofCertificate = object : ProofCertificate {
        override val expiresAt: Instant
            get() = this@ProofContainer.expiresAt

        override val personIdentifier: VaccinatedPersonIdentifier
            get() = proof.personIdentifier

        override val firstName: String
            get() = proof.firstName
        override val lastName: String
            get() = proof.lastName
        override val dateOfBirth: LocalDate
            get() = proof.dateOfBirth

        override val vaccinatedAt: LocalDate
            get() = proof.vaccinatedAt

        override val doseNumber: Int
            get() = proof.doseNumber
        override val totalSeriesOfDoses: Int
            get() = proof.totalSeriesOfDoses

        override val vaccineName: String
            get() = valueSet?.getDisplayText(proof.vaccineId) ?: proof.vaccineId
        override val vaccineManufacturer: String
            get() = valueSet?.getDisplayText(proof.marketAuthorizationHolderId)
                ?: proof.marketAuthorizationHolderId
        override val medicalProductName: String
            get() = valueSet?.getDisplayText(proof.medicalProductId) ?: proof.medicalProductId

        override val certificateIssuer: String
            get() = proof.certificateIssuer
        override val certificateId: String
            get() = proof.certificateId
    }

    data class StoredProof(
        @SerializedName("firstName") val firstName: String,
        @SerializedName("firstNameStandardized") val firstNameStandardized: String,
        @SerializedName("lastName") val lastName: String,
        @SerializedName("lastNameStandardized") val lastNameStandardized: String,

        @SerializedName("dateOfBirth") val dateOfBirth: LocalDate,

        @SerializedName("targetId") val targetId: String,

        @SerializedName("vaccineId") val vaccineId: String,
        @SerializedName("medicalProductId") val medicalProductId: String,
        @SerializedName("marketAuthorizationHolderId") val marketAuthorizationHolderId: String,

        @SerializedName("doseNumber") val doseNumber: Int,
        @SerializedName("totalSeriesOfDoses") val totalSeriesOfDoses: Int,

        @SerializedName("vaccinatedAt") val vaccinatedAt: LocalDate,

        @SerializedName("certificateIssuer") val certificateIssuer: String,
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

fun ProofCertificateServerData.toProofContainer() = ProofContainer(
    proof = ProofContainer.StoredProof(
        firstName = firstName,
        firstNameStandardized = firstNameStandardized,
        lastName = lastName,
        lastNameStandardized = lastNameStandardized,
        dateOfBirth = dateOfBirth,
        targetId = targetId,
        vaccineId = vaccineId,
        medicalProductId = medicalProductId,
        marketAuthorizationHolderId = marketAuthorizationHolderId,
        doseNumber = doseNumber,
        totalSeriesOfDoses = totalSeriesOfDoses,
        vaccinatedAt = vaccinatedAt,
        certificateIssuer = certificateIssuer,
        certificateId = certificateId,
    ),
    expiresAt = expiresAt,
    issuedAt = issuedAt,
    issuedBy = issuerCountryCode,
    proofCOSE = proofCertificateCBOR,
)
