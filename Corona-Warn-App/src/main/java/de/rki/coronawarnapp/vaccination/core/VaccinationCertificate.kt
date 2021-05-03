package de.rki.coronawarnapp.vaccination.core

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import de.rki.coronawarnapp.ui.Country
import org.joda.time.Instant
import org.joda.time.LocalDate

@Keep
data class VaccinationCertificate(
    @SerializedName("firstName")
    val firstName: String,
    @SerializedName("lastName")
    val lastName: String,
    @SerializedName("dateOfBirth")
    val dateOfBirth: LocalDate,
    @SerializedName("vaccinatedAt")
    val vaccinatedAt: Instant,
    @SerializedName("vaccineName")
    val vaccineName: String,
    @SerializedName("vaccineManufacturer")
    val vaccineManufacturer: String,
    @SerializedName("chargeId")
    val chargeId: String,
    @SerializedName("certificateIssuer")
    val certificateIssuer: String,
    @SerializedName("certificateCountry")
    val certificateCountry: Country,
    @SerializedName("certificateId")
    val certificateId: String,
) {
    val identifier: VaccinatedPersonIdentifier get() = ""
}
