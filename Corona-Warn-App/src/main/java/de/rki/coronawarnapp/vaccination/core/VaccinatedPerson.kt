package de.rki.coronawarnapp.vaccination.core

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import org.joda.time.Instant
import org.joda.time.LocalDate

@Keep
data class VaccinatedPerson(
    @SerializedName("vaccinationCertificates")
    val vaccinationCertificates: Set<VaccinationCertificate>,
    @SerializedName("proofCertificates")
    val proofCertificates: Set<ProofCertificate>,
    @SerializedName("isRefreshing")
    val isRefreshing: Boolean,
    @SerializedName("lastUpdatedAt")
    val lastUpdatedAt: Instant,
) {
    val identifier: VaccinatedPersonIdentifier = ""

    val firstName: String
        get() = ""

    val lastName: String
        get() = ""

    val dateOfBirth: LocalDate
        get() = LocalDate.now()

    val vaccinationStatus: Status
        get() = if (proofCertificates.isNotEmpty()) Status.COMPLETE else Status.INCOMPLETE

    enum class Status {
        INCOMPLETE,
        COMPLETE
    }
}

typealias VaccinatedPersonIdentifier = String
