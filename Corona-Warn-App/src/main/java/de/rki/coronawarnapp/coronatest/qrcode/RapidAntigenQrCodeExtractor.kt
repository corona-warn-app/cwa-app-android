package de.rki.coronawarnapp.coronatest.qrcode

import com.google.common.io.BaseEncoding
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import de.rki.coronawarnapp.util.serialization.fromJson
import okio.internal.commonToUtf8String
import org.joda.time.Instant
import org.joda.time.LocalDate
import timber.log.Timber
import java.util.regex.Matcher
import java.util.regex.Pattern
import javax.inject.Inject

class RapidAntigenQrCodeExtractor @Inject constructor() : QrCodeExtractor {

    private val prefix: String = "https://s.coronawarn.app?v=1#"
    private val prefix2: String = "https://s.coronawarn.app/?v=1#"
    private val hexPattern: Pattern = Pattern.compile("\\p{XDigit}+")

    override fun canHandle(rawString: String): Boolean {
        return rawString.startsWith(prefix, ignoreCase = true) || rawString.startsWith(prefix2, ignoreCase = true)
    }

    override fun extract(rawString: String): CoronaTestQRCode.RapidAntigen {
        val data = extractData(rawString).validate()
        return CoronaTestQRCode.RapidAntigen(
            hash = data.hash!!,
            createdAt = data.createdAt!!,
            firstName = data.firstName,
            lastName = data.lastName,
            dateOfBirth = data.dateOfBirth
        )
    }

    private fun Payload.validate(): Payload {
        if (hash == null || !hash.isSha256Hash()) throw InvalidQRCodeException("Hash is invalid")
        if (timestamp == null || timestamp <= 0) throw InvalidQRCodeException("Timestamp is invalid")
        createdAt = Instant.ofEpochSecond(timestamp)
        dateOfBirth = dob?.let {
            try {
                LocalDate.parse(it)
            } catch (e: Exception) {
                Timber.e("Invalid date format")
                throw InvalidQRCodeException("Date of birth has wrong format: $it. It should be YYYY-MM-DD")
            }
        }
        return this
    }

    private fun String.isSha256Hash(): Boolean {
        return length == 64 && isHexadecimal()
    }

    private fun String.isHexadecimal(): Boolean {
        val matcher: Matcher = hexPattern.matcher(this)
        return matcher.matches()
    }

    private fun extractData(rawString: String): Payload {
        return rawString
            .removePrefix(prefix)
            .removePrefix(prefix2)
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
        val hash: String?,
        val timestamp: Long?,
        @SerializedName("fn")
        val firstName: String?,
        @SerializedName("ln")
        val lastName: String?,
        val dob: String?
    ) {
        var dateOfBirth: LocalDate? = null
        var createdAt: Instant? = null
    }
}
