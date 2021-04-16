package de.rki.coronawarnapp.coronatest.qrcode

import com.google.common.io.BaseEncoding
import com.google.gson.Gson
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.util.serialization.fromJson
import okio.internal.commonToUtf8String
import org.joda.time.Instant
import org.joda.time.LocalDate
import timber.log.Timber

internal class RapidAntigenQrCodeExtractor : QrCodeExtractor {

    private val prefix: String = "https://s.coronawarn.app?v=1#"
    private val prefix2: String = "https://s.coronawarn.app/?v=1#"

    override fun canHandle(rawString: String): Boolean {
        return rawString.startsWith(prefix, ignoreCase = true) || rawString.startsWith(prefix2, ignoreCase = true)
    }

    override fun extract(rawString: String): CoronaTestQRCode.RapidAntigen {
        val data = extractData(rawString)
        return CoronaTestQRCode.RapidAntigen(
            CoronaTest.Type.RAPID_ANTIGEN,
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
