package de.rki.coronawarnapp.vaccination.core.certificate

import com.google.gson.annotations.SerializedName
import org.joda.time.DateTime
import org.joda.time.LocalDate
import timber.log.Timber

data class VaccinationDGCV1(
    @SerializedName("ver") val version: String,
    @SerializedName("nam") val nameData: NameData,
    @SerializedName("dob") val dob: String,
    @SerializedName("v") val vaccinationDatas: List<VaccinationData>,
) {
    data class NameData(
        @SerializedName("fn") val familyName: String?,
        @SerializedName("fnt") val familyNameStandardized: String,
        @SerializedName("gn") val givenName: String?,
        @SerializedName("gnt") val givenNameStandardized: String?,
    )

    data class VaccinationData(
        // Disease or agent targeted, e.g. "tg": "840539006"
        @SerializedName("tg") val targetId: String,
        // Vaccine or prophylaxis, e.g. "vp": "1119349007"
        @SerializedName("vp") val vaccineId: String,
        // Vaccine medicinal product,e.g. "mp": "EU/1/20/1528",
        @SerializedName("mp") val medicalProductId: String,
        // Marketing Authorization Holder, e.g. "ma": "ORG-100030215",
        @SerializedName("ma") val marketAuthorizationHolderId: String,
        // Dose Number, e.g. "dn": 2
        @SerializedName("dn") val doseNumber: Int,
        // Total Series of Doses, e.g. "sd": 2,
        @SerializedName("sd") val totalSeriesOfDoses: Int,
        // Date of Vaccination, e.g. "dt" : "2021-04-21"
        @SerializedName("dt") val dt: String,
        // Country of Vaccination, e.g. "co": "NL"
        @SerializedName("co") val countryOfVaccination: String,
        // Certificate Issuer, e.g. "is": "Ministry of Public Health, Welfare and Sport",
        @SerializedName("is") val certificateIssuer: String,
        // Unique Certificate Identifier, e.g.  "ci": "urn:uvci:01:NL:PlA8UWS60Z4RZXVALl6GAZ"
        @SerializedName("ci") val uniqueCertificateIdentifier: String
    ) {
        // Can't use lazy because GSON will NULL it, as we have no no-args constructor
        private var vaccinatedAtCache: LocalDate? = null
        val vaccinatedAt: LocalDate
            get() = vaccinatedAtCache ?: dt.toLocalDateLeniently().also {
                vaccinatedAtCache = it
            }
    }

    // Can't use lazy because GSON will NULL it, as we have no no-args constructor
    private var dateOfBirthCache: LocalDate? = null
    val dateOfBirth: LocalDate
        get() = dateOfBirthCache ?: dob.toLocalDateLeniently().also {
            dateOfBirthCache = it
        }
}

private fun String.toLocalDateLeniently(): LocalDate = try {
    LocalDate.parse(this)
} catch (e: Exception) {
    Timber.w("Irregular date string: %s", this)
    try {
        DateTime.parse(this).toLocalDate()
    } catch (giveUp: Exception) {
        throw giveUp
    }
}
