package de.rki.coronawarnapp.covidcertificate.recovery.core.certificate

import com.google.gson.annotations.SerializedName
import de.rki.coronawarnapp.covidcertificate.common.certificate.Dcc
import org.joda.time.LocalDate

data class RecoveryDccV1(
    @SerializedName("ver") override val version: String,
    @SerializedName("nam") override val nameData: Dcc.NameData,
    @SerializedName("dob") override val dob: String,
    @SerializedName("t") override val payloads: List<RecoveryCertificateData>,
) : Dcc<RecoveryDccV1.RecoveryCertificateData> {

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
    ) : Dcc.Payload {
        val testedPositiveOn: LocalDate
            get() = LocalDate.parse(fr)
        val validFrom: LocalDate
            get() = LocalDate.parse(df)
        val validUntil: LocalDate
            get() = LocalDate.parse(du)
    }
}
