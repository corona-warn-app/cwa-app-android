package de.rki.coronawarnapp.covidcertificate.test

import com.google.gson.annotations.SerializedName
import org.joda.time.Instant
import org.joda.time.LocalDate

data class TestCertificateDccV1(
    @SerializedName("ver") val version: String,
    @SerializedName("nam") val nameData: NameData,
    @SerializedName("dob") val dob: String,
    @SerializedName("t") val testCertificateData: List<TestCertificateData>,
) {
    data class NameData(
        @SerializedName("fn") val familyName: String?,
        @SerializedName("fnt") val familyNameStandardized: String,
        @SerializedName("gn") val givenName: String?,
        @SerializedName("gnt") val givenNameStandardized: String?,
    )

    data class TestCertificateData(
        // Disease or agent targeted, e.g. "tg": "840539006"
        @SerializedName("tg") val targetId: String,
        // Type of Test (required) eg "LP217198-3"
        @SerializedName("tt") val testType: String,
        // Test Result (required) e. g. "tr": "260415000"
        @SerializedName("tr") val testResult: String,
        // NAA Test Name (only for PCR tests, but not required) "nm": "Roche LightCycler qPCR",
        @SerializedName("nm") val testName: String,
        // Vaccine medicinal product,e.g. "mp": "EU/1/20/1528",
        @SerializedName("mp") val medicalProductId: String,
        // Marketing Authorization Holder, e.g. "ma": "ORG-100030215",
        @SerializedName("ma") val marketAuthorizationHolderId: String,
        // Date/Time of Sample Collection (required) "sc": "2021-04-13T14:20:00+00:00"
        @SerializedName("sc") val sampleCollectedAt: Instant,
        // Date/Time of Test Result "dr": "2021-04-13T14:40:01+00:00",
        @SerializedName("dr") val testResultAt: Instant,
        // Testing Center (required) "tc": "GGD Fryslân, L-Heliconweg",
        @SerializedName("tc") val testCenter: String,
        // Country of Test, e.g. "co": "NL"
        @SerializedName("co") val countryOfTest: String,
        // Certificate Issuer, e.g. "is": "Ministry of Public Health, Welfare and Sport",
        @SerializedName("is") val certificateIssuer: String,
        // Unique Certificate Identifier, e.g.  "ci": "urn:uvci:01:NL:PlA8UWS60Z4RZXVALl6GAZ"
        @SerializedName("ci") val uniqueCertificateIdentifier: String
    )

    val dateOfBirth: LocalDate
        get() = LocalDate.parse(dob)
}

// TODO check structure
//// Version (required)
//"ver": "1.0.0",
//// Name (required)
//"nam": {
//    // Family name
//    "fn": "d'Arsøns - van Halen",
//    // Given name
//    "gn": "François-Joan",
//    // Standardized family name (required)
//    "fnt": "DARSONS<VAN<HALEN",
//    // Standardized given name
//    "gnt": "FRANCOIS<JOAN"
//},
//// Date of birth (required)
//"dob": "2009-02-28",
//// Test entry (assumption: exactly one entry)
//"t": [
//{
//    // Disease or agent targeted (required)
//    "tg": "840539006",
//    // Type of Test (required)
//    "tt": "LP217198-3",
//    // Test Result (required)
//    "tr": "260415000",
//    // NAA Test Name (only for PCR tests, but not required)
//    "nm": "Roche LightCycler qPCR",
//    // RAT Test name and manufacturer (only for RAT tests, but not required)
//    "ma": "1232",
//    // Date/Time of Sample Collection (required)
//    "sc": "2021-04-13T14:20:00+00:00",
//    // Date/Time of Test Result
//    "dr": "2021-04-13T14:40:01+00:00",
//    // Testing Center (required)
//    "tc": "GGD Fryslân, L-Heliconweg",
//    // Country of Test (required)
//    "co": "NL",
//    // Certificate Issuer (required)
//    "is": "Ministry of Public Health, Welfare and Sport",
//    // Unique Certificate Identifier (required)
//    "ci": "urn:uvci:01:NL:GGD/81AAH16AZ"
//}
//]
//}
