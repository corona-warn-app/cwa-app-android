package de.rki.coronawarnapp.covidcertificate.test

import com.google.gson.annotations.SerializedName
import org.joda.time.Instant
import org.joda.time.LocalDate

data class TestCertificateDccV1(
    @SerializedName("ver") val version: String,
    @SerializedName("nam") val nameData: NameData,
    @SerializedName("dob") val dob: String,
    @SerializedName("v") val testCertificateData: List<TestCertificateData>,
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
        // Type of Test (required)
        @SerializedName("tt") val testType: String,
        // Test Result (required)
        @SerializedName("tr") val testResult: String,
        // NAA Test Name (only for PCR tests, but not required)
        @SerializedName("nm") val testName: String?,
        // RAT Test name and manufacturer (only for RAT tests, but not required)
        @SerializedName("ma") val testNameAndManufactor: String?,
        // Date/Time of Sample Collection (required)
        // "sc": "2021-04-13T14:20:00+00:00",
        @SerializedName("sc") val sampleCollectedAt: Instant,
        // Date/Time of Test Result
        // "dr": "2021-04-13T14:40:01+00:00",
        @SerializedName("dr") val testResultAt: Instant,
        // Testing Center (required)
        // "tc": "GGD Fryslân, L-Heliconweg",
        @SerializedName("tc") val testCenter: String,
        // Country of Test (required)
        @SerializedName("co") val countryOfTest: String,
        // Certificate Issuer, e.g. "is": "Ministry of Public Health, Welfare and Sport",
        @SerializedName("is") val certificateIssuer: String,
        // Unique Certificate Identifier, e.g.  "ci": "urn:uvci:01:NL:PlA8UWS60Z4RZXVALl6GAZ"
        @SerializedName("ci") val uniqueCertificateIdentifier: String
    )

    val dateOfBirth: LocalDate
        get() = LocalDate.parse(dob)
}
