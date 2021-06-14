package de.rki.coronawarnapp.covidcertificate.recovery.core.certificate

import com.google.gson.annotations.SerializedName
import de.rki.coronawarnapp.covidcertificate.common.certificate.Dcc

data class RecoveryDccV1(
    @SerializedName("ver") override val version: String,
    @SerializedName("nam") override val nameData: Dcc.NameData,
    @SerializedName("dob") override val dob: String,
    @SerializedName("t") override val payloads: List<RecoveryCertificateData>,
) : Dcc<RecoveryDccV1.RecoveryCertificateData>() {

    data class RecoveryCertificateData(
        // Disease or agent targeted, e.g. "tg": "840539006"
        @SerializedName("tg") override val targetId: String,

        // TODO

        // Country of Test (required)
        @SerializedName("co") override val certificateCountry: String,
        // Certificate Issuer, e.g. "is": "Ministry of Public Health, Welfare and Sport",
        @SerializedName("is") override val certificateIssuer: String,
        // Unique Certificate Identifier, e.g.  "ci": "urn:uvci:01:NL:PlA8UWS60Z4RZXVALl6GAZ"
        @SerializedName("ci") override val uniqueCertificateIdentifier: String
    ) : Payload
}
