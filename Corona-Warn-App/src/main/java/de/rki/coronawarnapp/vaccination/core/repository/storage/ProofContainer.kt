package de.rki.coronawarnapp.vaccination.core.repository.storage

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import de.rki.coronawarnapp.vaccination.core.ProofCertificate
import de.rki.coronawarnapp.vaccination.core.VaccinatedPersonIdentifier
import de.rki.coronawarnapp.vaccination.core.common.RawCOSEObject
import de.rki.coronawarnapp.vaccination.core.personIdentifier
import de.rki.coronawarnapp.vaccination.core.server.ProofCertificateV1
import de.rki.coronawarnapp.vaccination.core.server.proof.ProofCertificateCOSEParser
import de.rki.coronawarnapp.vaccination.core.server.proof.ProofCertificateData
import de.rki.coronawarnapp.vaccination.core.server.proof.ProofCertificateResponse
import de.rki.coronawarnapp.vaccination.core.server.valueset.VaccinationValueSet
import de.rki.coronawarnapp.vaccination.core.server.valueset.getDisplayText

import org.joda.time.Instant
import org.joda.time.LocalDate

@Keep
data class ProofContainer(
    @SerializedName("proofCOSE") val proofCOSE: RawCOSEObject,
    @SerializedName("receivedAt") val receivedAt: Instant,
) {
    @Transient internal var preParsedData: ProofCertificateData? = null

    // Otherwise GSON unsafes reflection to create this class, and sets the LAZY to null
    @Suppress("unused")
    constructor() : this(RawCOSEObject.EMPTY, Instant.EPOCH)

    @delegate:Transient
    private val proofData: ProofCertificateData by lazy {
        preParsedData ?: ProofCertificateCOSEParser().parse(proofCOSE)
    }

    val proof: ProofCertificateV1
        get() = proofData.proofCertificate

    val vaccination: ProofCertificateV1.VaccinationData
        get() = proof.vaccinationDatas.single()

    val personIdentifier: VaccinatedPersonIdentifier
        get() = proof.personIdentifier

    fun toProofCertificate(valueSet: VaccinationValueSet?): ProofCertificate = object : ProofCertificate {
        override val expiresAt: Instant
            get() = proofData.expiresAt

        override val personIdentifier: VaccinatedPersonIdentifier
            get() = proof.personIdentifier

        override val firstName: String?
            get() = proof.nameData.givenName
        override val lastName: String
            get() = proof.nameData.familyName ?: proof.nameData.familyNameStandardized
        override val dateOfBirth: LocalDate
            get() = proof.dateOfBirth

        override val vaccinatedAt: LocalDate
            get() = vaccination.vaccinatedAt

        override val doseNumber: Int
            get() = vaccination.doseNumber
        override val totalSeriesOfDoses: Int
            get() = vaccination.totalSeriesOfDoses

        override val vaccineName: String
            get() = valueSet?.getDisplayText(vaccination.vaccineId) ?: vaccination.vaccineId
        override val vaccineManufacturer: String
            get() = valueSet?.getDisplayText(vaccination.marketAuthorizationHolderId)
                ?: vaccination.marketAuthorizationHolderId
        override val medicalProductName: String
            get() = valueSet?.getDisplayText(vaccination.medicalProductId) ?: vaccination.medicalProductId

        override val certificateIssuer: String
            get() = vaccination.certificateIssuer
        override val certificateId: String
            get() = vaccination.uniqueCertificateIdentifier
    }
}

fun ProofCertificateResponse.toProofContainer(receivedAt: Instant) = ProofContainer(
    proofCOSE = proofCertificateCOSE,
    receivedAt = receivedAt,
).apply {
    preParsedData = proofCertificateData
}
