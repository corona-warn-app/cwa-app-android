package de.rki.coronawarnapp.covidcertificate.common.certificate

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import de.rki.coronawarnapp.coronatest.qrcode.InvalidQRCodeException
import java.time.Instant
import java.time.LocalDate

data class DccV1(
    @JsonProperty("ver") val version: String,
    @JsonProperty("nam") val nameData: NameData,
    @JsonProperty("dob") val dob: String,
    @JsonProperty("v") val vaccinations: List<VaccinationData>? = null,
    @JsonProperty("t") val tests: List<TestCertificateData>? = null,
    @JsonProperty("r") val recoveries: List<RecoveryCertificateData>? = null,
) {
    data class NameData(
        @JsonProperty("fn") internal val familyName: String?,
        @JsonProperty("fnt") internal val familyNameStandardized: String?,
        @JsonProperty("gn") internal val givenName: String?,
        @JsonProperty("gnt") internal val givenNameStandardized: String?,
    ) {
        @get:JsonIgnore
        val firstName: String?
            get() = if (givenName.isNullOrBlank()) givenNameStandardized else givenName
        @get:JsonIgnore
        val lastName: String?
            get() = if (familyName.isNullOrBlank()) familyNameStandardized else familyName
        @get:JsonIgnore
        val fullName: String
            get() = when {
                firstName.isNullOrBlank() -> lastName.assertName()
                lastName.isNullOrBlank() -> firstName.assertName()
                else -> "$firstName $lastName"
            }
        @get:JsonIgnore
        val fullNameFormatted: String
            get() = when {
                firstName.isNullOrBlank() -> lastName.assertName()
                lastName.isNullOrBlank() -> firstName.assertName()
                else -> "$lastName, $firstName"
            }
        @get:JsonIgnore
        val fullNameStandardizedFormatted: String
            get() = when {
                givenNameStandardized.isNullOrBlank() -> familyNameStandardized.assertName()
                familyNameStandardized.isNullOrBlank() -> givenNameStandardized.assertName()
                else -> familyNameStandardized.trim() + "<<" + givenNameStandardized.trim()
            }

        private fun String?.assertName(): String {
            if (isNullOrBlank()) throw throw InvalidQRCodeException("Person `fnt` or `gnt` should be present!")
            return this
        }
    }
    @get:JsonIgnore
    val dateOfBirthFormatted: String
        get() = dob.formatDate()
    @get:JsonIgnore
    val personIdentifier: CertificatePersonIdentifier
        get() = CertificatePersonIdentifier(
            dateOfBirthFormatted = dateOfBirthFormatted,
            lastNameStandardized = nameData.familyNameStandardized,
            firstNameStandardized = nameData.givenNameStandardized
        )

    interface MetaData {
        val version: String
        val nameData: NameData
        val dateOfBirthFormatted: String
        val dob: String
        val payload: Payload
        val personIdentifier: CertificatePersonIdentifier
    }

    interface Payload {
        val targetId: String
        val certificateCountry: String
        val certificateIssuer: String
        val uniqueCertificateIdentifier: String
    }

    data class RecoveryCertificateData(
        // Disease or agent targeted, e.g. "tg": "840539006"
        @JsonProperty("tg") override val targetId: String,
        // Date of First Positive NAA Test Result (required) e.g. "2021-04-21"
        @JsonProperty("fr") val fr: String,
        // Certificate Valid From (required) e.g. "2021-05-01"
        @JsonProperty("df") val df: String,
        // Certificate Valid Until (required) e.g. "2021-10-21"
        @JsonProperty("du") val du: String,
        // Country of Test (required)
        @JsonProperty("co") override val certificateCountry: String,
        // Certificate Issuer, e.g. "is": "Ministry of Public Health, Welfare and Sport",
        @JsonProperty("is") override val certificateIssuer: String,
        // Unique Certificate Identifier, e.g.  "ci": "urn:uvci:01:NL:PlA8UWS60Z4RZXVALl6GAZ"
        @JsonProperty("ci") override val uniqueCertificateIdentifier: String
    ) : Payload {
        @get:JsonIgnore
        val testedPositiveOn: LocalDate?
            get() = fr.parseLocalDate()
        @get:JsonIgnore
        val testedPositiveOnFormatted: String
            get() = fr.formatDate()
        @get:JsonIgnore
        val validFromFormatted: String
            get() = df.formatDate()
        @get:JsonIgnore
        val validFrom: LocalDate?
            get() = validFromFormatted.parseLocalDate()
        @get:JsonIgnore
        val validUntilFormatted: String
            get() = du.formatDate()
        @get:JsonIgnore
        val validUntil: LocalDate?
            get() = validUntilFormatted.parseLocalDate()
    }

    data class VaccinationData(
        // Disease or agent targeted, e.g. "tg": "840539006"
        @JsonProperty("tg") override val targetId: String,
        // Vaccine or prophylaxis, e.g. "vp": "1119349007"
        @JsonProperty("vp") val vaccineId: String,
        // Vaccine medicinal product,e.g. "mp": "EU/1/20/1528",
        @JsonProperty("mp") val medicalProductId: String,
        // Marketing Authorization Holder, e.g. "ma": "ORG-100030215",
        @JsonProperty("ma") val marketAuthorizationHolderId: String,
        // Dose Number, e.g. "dn": 2
        @JsonProperty("dn") val doseNumber: Int,
        // Total Series of Doses, e.g. "sd": 2,
        @JsonProperty("sd") val totalSeriesOfDoses: Int,
        // Date of Vaccination, e.g. "dt" : "2021-04-21"
        @JsonProperty("dt") val dt: String,
        // Country of Vaccination, e.g. "co": "NL"
        @JsonProperty("co") override val certificateCountry: String,
        // Certificate Issuer, e.g. "is": "Ministry of Public Health, Welfare and Sport",
        @JsonProperty("is") override val certificateIssuer: String,
        // Unique Certificate Identifier, e.g.  "ci": "urn:uvci:01:NL:PlA8UWS60Z4RZXVALl6GAZ"
        @JsonProperty("ci") override val uniqueCertificateIdentifier: String
    ) : Payload {
        @get:JsonIgnore
        val vaccinatedOnFormatted: String
            get() = dt.formatDate()

        @get:JsonIgnore
        val vaccinatedOn: LocalDate?
            get() = vaccinatedOnFormatted.parseLocalDate()
    }

    data class TestCertificateData(
        // Disease or agent targeted, e.g. "tg": "840539006"
        @JsonProperty("tg") override val targetId: String,
        // Type of Test (required) eg "LP217198-3"
        @JsonProperty("tt") val testType: String,
        // Test Result (required) e. g. "tr": "260415000"
        @JsonProperty("tr") val testResult: String,
        // NAA Test Name (only for PCR tests, but not required) "nm": "Roche LightCycler qPCR",
        @JsonProperty("nm") val testName: String? = null,
        // RAT Test name and manufacturer (only for RAT tests, but not required)
        @JsonProperty("ma") val testNameAndManufacturer: String? = null,
        // Date/Time of Sample Collection (required) "sc": "2021-04-13T14:20:00+00:00"
        @JsonProperty("sc") val sc: String,
        // Testing Center (required) "tc": "GGD Fryslân, L-Heliconweg",
        @JsonProperty("tc") val testCenter: String?,
        // Country of Test (required)
        @JsonProperty("co") override val certificateCountry: String,
        // Certificate Issuer, e.g. "is": "Ministry of Public Health, Welfare and Sport",
        @JsonProperty("is") override val certificateIssuer: String,
        // Unique Certificate Identifier, e.g.  "ci": "urn:uvci:01:NL:PlA8UWS60Z4RZXVALl6GAZ"
        @JsonProperty("ci") override val uniqueCertificateIdentifier: String
    ) : Payload {
        @get:JsonIgnore
        val sampleCollectedAt: Instant?
            get() = sc.parseInstant()
        @get:JsonIgnore
        val sampleCollectedAtFormatted: String
            get() = sc.formatDateTime()
    }
}

data class VaccinationDccV1(
    override val version: String,
    override val nameData: DccV1.NameData,
    override val dateOfBirthFormatted: String,
    override val dob: String,
    override val personIdentifier: CertificatePersonIdentifier,
    val vaccination: DccV1.VaccinationData
) : DccV1.MetaData {
    override val payload: DccV1.Payload
        get() = vaccination

    val isSeriesCompletingShot get() = vaccination.doseNumber >= vaccination.totalSeriesOfDoses
}

data class TestDccV1(
    override val version: String,
    override val nameData: DccV1.NameData,
    override val dateOfBirthFormatted: String,
    override val dob: String,
    override val personIdentifier: CertificatePersonIdentifier,
    val test: DccV1.TestCertificateData
) : DccV1.MetaData {
    override val payload: DccV1.Payload
        get() = test

    val isPCRTestCertificate: Boolean get() = test.testType == PCR_TEST
    val isRapidAntigenTestCertificate: Boolean get() = test.testType == RAT_TEST

    companion object {
        const val PCR_TEST = "LP6464-4"
        const val RAT_TEST = "LP217198-3"
    }
}

data class RecoveryDccV1(
    override val version: String,
    override val nameData: DccV1.NameData,
    override val dateOfBirthFormatted: String,
    override val dob: String,
    override val personIdentifier: CertificatePersonIdentifier,
    val recovery: DccV1.RecoveryCertificateData
) : DccV1.MetaData {
    override val payload: DccV1.Payload
        get() = recovery
}
