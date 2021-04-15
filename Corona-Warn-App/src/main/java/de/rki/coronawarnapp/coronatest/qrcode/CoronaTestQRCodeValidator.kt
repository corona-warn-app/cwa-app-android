package de.rki.coronawarnapp.coronatest.qrcode

import com.google.common.io.BaseEncoding
import com.google.gson.Gson
import dagger.Reusable
import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode.PCR
import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode.RapidAntigen
import de.rki.coronawarnapp.coronatest.type.CoronaTest.Type
import de.rki.coronawarnapp.util.serialization.fromJson
import okio.internal.commonToUtf8String
import org.joda.time.Instant
import org.joda.time.LocalDate
import timber.log.Timber
import java.util.regex.Pattern
import javax.inject.Inject

@Reusable
class CoronaTestQrCodeValidator @Inject constructor() {

    private val extractors = listOf(
        RapidAntigenQrCodeExtractor(),
        PcrQrCodeExtractor()
    )

    fun validate(rawString: String): CoronaTestQRCode {
        return extractors.find { it.isOfType(rawString) }
            ?.extract(rawString)?.also {
                Timber.i("Extracted data from QR code is $it")
            } ?: throw InvalidQRCodeException()
    }
}

private interface QrCodeExtractor {
    fun isOfType(rawString: String): Boolean
    fun extract(rawString: String): CoronaTestQRCode
}

internal class PcrQrCodeExtractor : QrCodeExtractor {

    override fun isOfType(rawString: String): Boolean = rawString.startsWith(prefix, ignoreCase = true)

    override fun extract(rawString: String): PCR {
        return PCR(
            Type.PCR,
            extractGUID(rawString)
        )
    }

    private fun extractGUID(rawString: String): CoronaTestGUID {
        if (!QR_CODE_REGEX.toRegex().matches(rawString)) throw InvalidQRCodeException()

        val matcher = QR_CODE_REGEX.matcher(rawString)
        return if (matcher.matches()) {
            matcher.group(1) as CoronaTestGUID
        } else throw InvalidQRCodeException()
    }

    private val prefix: String = "https://localhost"

    private val QR_CODE_REGEX: Pattern = (
        "^" + // Match start of string
            "(?:https:\\/{2}localhost)" + // Match `https://localhost`
            "(?:\\/{1}\\?)" + // Match the query param `/?`
            "([a-f\\d]{6}[-][a-f\\d]{8}[-](?:[a-f\\d]{4}[-]){3}[a-f\\d]{12})" + // Match the UUID
            "\$"
        ).toPattern(Pattern.CASE_INSENSITIVE)
}

private class RapidAntigenQrCodeExtractor : QrCodeExtractor {

    private val prefix: String = "https://s.coronawarn.app?v=1#"
    private val prefix2: String = "https://s.coronawarn.app/?v=1#"

    override fun isOfType(rawString: String): Boolean {
        return rawString.startsWith(prefix, ignoreCase = true) || rawString.startsWith(prefix2, ignoreCase = true)
    }

    override fun extract(rawString: String): RapidAntigen {
        val data = extractData(rawString)
        return RapidAntigen(
            Type.RAPID_ANTIGEN,
            data.guid,
            data.createdAt,
            data.fn,
            data.ln,
            data.dateOfBirth
        )
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
        val guid: String,
        val timestamp: Long,
        val fn: String?,
        val ln: String?,
        val dob: String?
    ) {
        val dateOfBirth: LocalDate?
            get() = dob?.let {
                try {
                    LocalDate.parse(it)
                } catch (e: Exception) {
                    Timber.e("Invalid date format")
                    null
                }
            }

        val createdAt: Instant
            get() = Instant.ofEpochSecond(timestamp)
    }
}
