package de.rki.coronawarnapp.coronatest.qrcode

import com.google.common.io.BaseEncoding
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import de.rki.coronawarnapp.util.hashing.isSha256Hash
import de.rki.coronawarnapp.util.serialization.fromJson
import okio.internal.commonToUtf8String
import org.joda.time.Instant
import org.joda.time.LocalDate
import timber.log.Timber
import javax.inject.Inject

class RapidAntigenQrCodeExtractor @Inject constructor() : QrCodeExtractor<CoronaTestQRCode> {

    override fun canHandle(rawString: String): Boolean {
        return rawString.startsWith(PREFIX1, ignoreCase = true) || rawString.startsWith(PREFIX2, ignoreCase = true)
    }

    override fun extract(rawString: String): CoronaTestQRCode.RapidAntigen {
        Timber.v("extract(rawString=%s)", rawString)
        val payload = CleanPayload(extractData(rawString))

        payload.requireValidPersonalData()

        return CoronaTestQRCode.RapidAntigen(
            hash = payload.hash,
            createdAt = payload.createdAt,
            firstName = payload.firstName,
            lastName = payload.lastName,
            dateOfBirth = payload.dateOfBirth
        )
    }

    private fun extractData(rawString: String): RawPayload {
        return rawString
            .removePrefix(PREFIX1)
            .removePrefix(PREFIX2)
            .decode()
    }

    private fun String.decode(): RawPayload {
        val decoded = if (
            this.contains("+") ||
            this.contains("/") ||
            this.contains("=")
        ) {
            BaseEncoding.base64().decode(this)
        } else {
            BaseEncoding.base64Url().decode(this)
        }
        return Gson().fromJson(decoded.commonToUtf8String())
    }

    private data class RawPayload(
        @SerializedName("hash") val hash: String?,
        @SerializedName("timestamp") val timestamp: Long?,
        @SerializedName("fn") val firstName: String?,
        @SerializedName("ln") val lastName: String?,
        @SerializedName("dob") val dateOfBirth: String?
    )

    private data class CleanPayload(val raw: RawPayload) {

        val hash: String by lazy {
            if (raw.hash == null || !raw.hash.isSha256Hash()) throw InvalidQRCodeException("Hash is invalid")
            raw.hash
        }

        val createdAt: Instant by lazy {
            if (raw.timestamp == null || raw.timestamp <= 0) throw InvalidQRCodeException("Timestamp is invalid")
            Instant.ofEpochSecond(raw.timestamp)
        }

        val firstName: String? by lazy {
            if (raw.firstName.isNullOrEmpty()) null else raw.firstName
        }

        val lastName: String? by lazy {
            if (raw.lastName.isNullOrEmpty()) null else raw.lastName
        }

        val dateOfBirth: LocalDate? by lazy {
            if (raw.dateOfBirth.isNullOrEmpty()) return@lazy null

            try {
                LocalDate.parse(raw.dateOfBirth)
            } catch (e: Exception) {
                Timber.e("Invalid date format")
                throw InvalidQRCodeException(
                    "Date of birth has wrong format: ${raw.dateOfBirth}. It should be YYYY-MM-DD"
                )
            }
        }

        fun requireValidPersonalData() {
            val allOrNothing = listOf(
                firstName != null,
                lastName != null,
                dateOfBirth != null,
            )
            val complete = allOrNothing.all { it } || allOrNothing.all { !it }
            if (!complete) throw InvalidQRCodeException("QRCode contains incomplete personal data: $raw")
        }
    }

    companion object {
        private const val PREFIX1: String = "https://s.coronawarn.app?v=1#"
        private const val PREFIX2: String = "https://s.coronawarn.app/?v=1#"
    }
}
