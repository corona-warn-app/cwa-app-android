package de.rki.coronawarnapp.covidcertificate.common.certificate

import com.google.gson.annotations.SerializedName
import org.joda.time.DateTime
import org.joda.time.Instant
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatterBuilder
import org.joda.time.format.ISODateTimeFormat
import timber.log.Timber

data class DccV1(
    @SerializedName("ver") val version: String,
    @SerializedName("nam") val nameData: NameData,
    @SerializedName("dob") val dob: String,
    @SerializedName("v") val vaccinations: List<VaccinationData>? = null,
    @SerializedName("t") val tests: List<TestCertificateData>? = null,
    @SerializedName("r") val recoveries: List<RecoveryCertificateData>? = null,
) {
    data class NameData(
        @SerializedName("fn") internal val familyName: String?,
        @SerializedName("fnt") internal val familyNameStandardized: String,
        @SerializedName("gn") internal val givenName: String?,
        @SerializedName("gnt") internal val givenNameStandardized: String?,
    ) {
        val firstName: String?
            get() = if (givenName.isNullOrBlank()) givenNameStandardized else givenName

        val lastName: String
            get() = if (familyName.isNullOrBlank()) familyNameStandardized else familyName

        val fullName: String
            get() = when {
                firstName.isNullOrBlank() -> lastName
                else -> "$firstName $lastName"
            }
    }

    // Can't use lazy because GSON will NULL it, as we have no no-args constructor
    private var dateOfBirthCache: LocalDate? = null
    val dateOfBirth: LocalDate
        get() = dateOfBirthCache ?: dob.toLocalDateLeniently().also { dateOfBirthCache = it }

    val personIdentifier: CertificatePersonIdentifier
        get() = CertificatePersonIdentifier(
            dateOfBirth = dateOfBirth,
            lastNameStandardized = nameData.familyNameStandardized,
            firstNameStandardized = nameData.givenNameStandardized
        )

    interface Payload {
        val targetId: String
        val certificateCountry: String
        val certificateIssuer: String
        val uniqueCertificateIdentifier: String
    }

    data class RecoveryCertificateData(
        // Disease or agent targeted, e.g. "tg": "840539006"
        @SerializedName("tg") override val targetId: String,
        // Date of First Positive NAA Test Result (required) e.g. "2021-04-21"
        @SerializedName("fr") val fr: String,
        // Certificate Valid From (required) e.g. "2021-05-01"
        @SerializedName("df") val df: String,
        // Certificate Valid Until (required) e.g. "2021-10-21"
        @SerializedName("du") val du: String,
        // Country of Test (required)
        @SerializedName("co") override val certificateCountry: String,
        // Certificate Issuer, e.g. "is": "Ministry of Public Health, Welfare and Sport",
        @SerializedName("is") override val certificateIssuer: String,
        // Unique Certificate Identifier, e.g.  "ci": "urn:uvci:01:NL:PlA8UWS60Z4RZXVALl6GAZ"
        @SerializedName("ci") override val uniqueCertificateIdentifier: String
    ) : Payload {
        val testedPositiveOn: LocalDate
            get() = LocalDate.parse(fr)
        val validFrom: LocalDate
            get() = LocalDate.parse(df)
        val validUntil: LocalDate
            get() = LocalDate.parse(du)
    }

    data class VaccinationData(
        // Disease or agent targeted, e.g. "tg": "840539006"
        @SerializedName("tg") override val targetId: String,
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
        @SerializedName("co") override val certificateCountry: String,
        // Certificate Issuer, e.g. "is": "Ministry of Public Health, Welfare and Sport",
        @SerializedName("is") override val certificateIssuer: String,
        // Unique Certificate Identifier, e.g.  "ci": "urn:uvci:01:NL:PlA8UWS60Z4RZXVALl6GAZ"
        @SerializedName("ci") override val uniqueCertificateIdentifier: String
    ) : Payload {
        // Can't use lazy because GSON will NULL it, as we have no no-args constructor
        private var vaccinatedAtCache: LocalDate? = null
        val vaccinatedAt: LocalDate
            get() = vaccinatedAtCache ?: dt.toLocalDateLeniently().also { vaccinatedAtCache = it }
    }

    data class TestCertificateData(
        // Disease or agent targeted, e.g. "tg": "840539006"
        @SerializedName("tg") override val targetId: String,
        // Type of Test (required) eg "LP217198-3"
        @SerializedName("tt") val testType: String,
        // Test Result (required) e. g. "tr": "260415000"
        @SerializedName("tr") val testResult: String,
        // NAA Test Name (only for PCR tests, but not required) "nm": "Roche LightCycler qPCR",
        @SerializedName("nm") val testName: String? = null,
        // RAT Test name and manufacturer (only for RAT tests, but not required)
        @SerializedName("ma") val testNameAndManufactor: String? = null,
        // Date/Time of Sample Collection (required) "sc": "2021-04-13T14:20:00+00:00"
        @SerializedName("sc") val sc: String,
        // Date/Time of Test Result "dr": "2021-04-13T14:40:01+00:00",
        @SerializedName("dr") val dr: String? = null,
        // Testing Center (required) "tc": "GGD Fryslân, L-Heliconweg",
        @SerializedName("tc") val testCenter: String,
        // Country of Test (required)
        @SerializedName("co") override val certificateCountry: String,
        // Certificate Issuer, e.g. "is": "Ministry of Public Health, Welfare and Sport",
        @SerializedName("is") override val certificateIssuer: String,
        // Unique Certificate Identifier, e.g.  "ci": "urn:uvci:01:NL:PlA8UWS60Z4RZXVALl6GAZ"
        @SerializedName("ci") override val uniqueCertificateIdentifier: String
    ) : Payload {

        val testResultAt: Instant?
            get() = dr?.let { Instant.parse(it) }

        val sampleCollectedAt: Instant
            get() = Instant.parse(sc)
    }
}

internal fun String.toLocalDateLeniently(): LocalDate = try {
    LocalDate.parse(this, DateTimeFormat.forPattern("yyyy-MM-dd"))
} catch (e: Exception) {
    Timber.w("Irregular date string: %s", this)
    try {
        DateTime.parse(
            this,
            DateTimeFormatterBuilder()
                .append(ISODateTimeFormat.date())
                .append(ISODateTimeFormat.timeParser().withOffsetParsed())
                .toFormatter()
        ).toLocalDate()
    } catch (giveUp: Exception) {
        Timber.e("Invalid date string: %s", this)
        throw giveUp
    }
}
