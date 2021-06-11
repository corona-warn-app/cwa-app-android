package de.rki.coronawarnapp.covidcertificate.common.certificate

import com.google.gson.annotations.SerializedName
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatterBuilder
import org.joda.time.format.ISODateTimeFormat
import timber.log.Timber

abstract class Dcc<PayloadType : Dcc.Payload> {
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

    abstract val version: String
    abstract val nameData: NameData
    abstract val dob: String

    // Can't use lazy because GSON will NULL it, as we have no no-args constructor
    private var dateOfBirthCache: LocalDate? = null
    val dateOfBirth: LocalDate
        get() = dateOfBirthCache ?: dob.toLocalDateLeniently().also { dateOfBirthCache = it }

    abstract val payloads: List<PayloadType>
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
