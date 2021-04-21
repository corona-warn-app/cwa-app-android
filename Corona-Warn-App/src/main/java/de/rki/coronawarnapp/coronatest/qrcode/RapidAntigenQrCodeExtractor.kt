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
        val payload = extractData(rawString)
        return CoronaTestQRCode.RapidAntigen(
            hash = payload.hash,
            createdAt = payload.createdAt,
            firstName = payload.firstName,
            lastName = payload.lastName,
            dateOfBirth = payload.dateOfBirth
        )
    }

    private fun extractData(rawString: String): Payload {
        return rawString
            .removePrefix(PREFIX1)
            .removePrefix(PREFIX2)
            .decode()
    }

    private fun String.decode(): Payload {
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

    private data class Payload(
        @SerializedName("hash")
        val rawHash: String?,
        @SerializedName("timestamp")
        val rawTimestamp: Long?,
        @SerializedName("fn")
        val rawFirstName: String?,
        @SerializedName("ln")
        val rawLastName: String?,
        @SerializedName("dob")
        val rawDateOfBirth: String?
    ) {
        val hash: String
            get() {
                if (rawHash == null || !rawHash.isSha256Hash()) throw InvalidQRCodeException("Hash is invalid")
                return rawHash
            }

        val createdAt: Instant
            get() {
                if (rawTimestamp == null || rawTimestamp <= 0) throw InvalidQRCodeException("Timestamp is invalid")
                return Instant.ofEpochSecond(rawTimestamp)
            }

        val firstName: String?
            get() {
                if (rawFirstName.isNullOrEmpty()) return null
                return rawFirstName
            }

        val lastName: String?
            get() {
                if (rawLastName.isNullOrEmpty()) return null
                return rawLastName
            }

        val dateOfBirth: LocalDate?
            get() {
                if (rawDateOfBirth.isNullOrEmpty()) return null
                return try {
                    LocalDate.parse(rawDateOfBirth)
                } catch (e: Exception) {
                    Timber.e("Invalid date format")
                    throw InvalidQRCodeException(
                        "Date of birth has wrong format: $rawDateOfBirth. It should be YYYY-MM-DD"
                    )
                }
            }
    }

    companion object {
        private const val PREFIX1: String = "https://s.coronawarn.app?v=1#"
        private const val PREFIX2: String = "https://s.coronawarn.app/?v=1#"
    }
}
