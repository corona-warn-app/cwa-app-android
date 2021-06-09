package de.rki.coronawarnapp.covidcertificate.test.core.certificate

import com.google.gson.annotations.SerializedName
import de.rki.coronawarnapp.covidcertificate.common.certificate.Dcc
import org.joda.time.Instant

data class TestDccV1(
    @SerializedName("ver") override val version: String,
    @SerializedName("nam") override val nameData: Dcc.NameData,
    @SerializedName("dob") override val dob: String,
    @SerializedName("t") override val payloads: List<TestCertificateData>,
) : Dcc<TestDccV1.TestCertificateData> {

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
    ) : Dcc.Payload {

        val testResultAt: Instant?
            get() = dr?.let { Instant.parse(it) }

        val sampleCollectedAt: Instant
            get() = Instant.parse(sc)
    }
}
