package de.rki.coronawarnapp.covidcertificate.common.certificate

import com.google.gson.annotations.SerializedName
import org.joda.time.LocalDate

interface Dcc<PayloadType : Dcc.Payload> {
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

    val version: String
    val nameData: NameData
    val dob: String

    val dateOfBirth: LocalDate
        get() = LocalDate.parse(dob)

    val payloads: List<PayloadType>
    val payload: PayloadType
        get() = payloads.single()

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
}
