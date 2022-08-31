package de.rki.coronawarnapp.covidcertificate.common.certificate

import com.google.gson.annotations.SerializedName
import de.rki.coronawarnapp.coronatest.qrcode.InvalidQRCodeException
import org.joda.time.Instant
import org.joda.time.LocalDate

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
        @SerializedName("fnt") internal val familyNameStandardized: String?,
        @SerializedName("gn") internal val givenName: String?,
        @SerializedName("gnt") internal val givenNameStandardized: String?,
    ) {
        val firstName: String?
            get() = if (givenName.isNullOrBlank()) givenNameStandardized else givenName

        val lastName: String?
            get() = if (familyName.isNullOrBlank()) familyNameStandardized else familyName

        val fullName: String
            get() = when {
                firstName.isNullOrBlank() -> lastName.assertName()
                lastName.isNullOrBlank() -> firstName.assertName()
                else -> "$firstName $lastName"
            }

        val fullNameFormatted: String
            get() = when {
                firstName.isNullOrBlank() -> lastName.assertName()
                lastName.isNullOrBlank() -> firstName.assertName()
                else -> "$lastName, $firstName"
            }

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

    val dateOfBirthFormatted: String
        get() = dob.formatDate()

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
        val testedPositiveOn: LocalDate?
            get() = fr.parseLocalDate()

        val testedPositiveOnFormatted: String
            get() = fr.formatDate()

        val validFromFormatted: String
            get() = df.formatDate()
        val validFrom: LocalDate?
            get() = validFromFormatted.parseLocalDate()

        val validUntilFormatted: String
            get() = du.formatDate()
        val validUntil: LocalDate?
            get() = validUntilFormatted.parseLocalDate()
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
        val vaccinatedOnFormatted: String
            get() = dt.formatDate()

        val vaccinatedOn: LocalDate?
            get() = vaccinatedOnFormatted.parseLocalDate()
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
        @SerializedName("ma") val testNameAndManufacturer: String? = null,
        // Date/Time of Sample Collection (required) "sc": "2021-04-13T14:20:00+00:00"
        @SerializedName("sc") val sc: String,
        // Testing Center (required) "tc": "GGD Fryslân, L-Heliconweg",
        @SerializedName("tc") val testCenter: String?,
        // Country of Test (required)
        @SerializedName("co") override val certificateCountry: String,
        // Certificate Issuer, e.g. "is": "Ministry of Public Health, Welfare and Sport",
        @SerializedName("is") override val certificateIssuer: String,
        // Unique Certificate Identifier, e.g.  "ci": "urn:uvci:01:NL:PlA8UWS60Z4RZXVALl6GAZ"
        @SerializedName("ci") override val uniqueCertificateIdentifier: String
    ) : Payload {

        val sampleCollectedAt: Instant?
            get() = sc.parseInstant()

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
