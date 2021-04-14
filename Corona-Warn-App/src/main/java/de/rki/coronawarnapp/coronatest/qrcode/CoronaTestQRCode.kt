package de.rki.coronawarnapp.coronatest.qrcode

import android.os.Parcelable
import com.google.common.io.BaseEncoding
import com.google.gson.Gson
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.util.serialization.fromJson
import kotlinx.parcelize.Parcelize
import okio.internal.commonToUtf8String
import org.joda.time.Instant
import org.joda.time.LocalDate
import java.util.regex.Pattern

sealed class CoronaTestQRCode : Parcelable {

    abstract val type: CoronaTest.Type
    abstract val guid: CoronaTestGUID

    @Parcelize
    data class PCR(
        override val type: CoronaTest.Type,
        override val guid: CoronaTestGUID
    ) : CoronaTestQRCode() {

        constructor(rawString: String) :
            this(
                CoronaTest.Type.PCR,
                extractGUID(rawString)
            )

        companion object {
            const val prefix: String = "https://localhost"

            private fun extractGUID(rawString: String): CoronaTestGUID {
                if (!QR_CODE_REGEX.toRegex().matches(rawString)) throw InvalidQRCodeException()

                val matcher = QR_CODE_REGEX.matcher(rawString)
                return if (matcher.matches()) {
                    matcher.group(1) as CoronaTestGUID
                } else throw InvalidQRCodeException()
            }

            private val QR_CODE_REGEX: Pattern = (
                "^" + // Match start of string
                    "(?:https:\\/{2}localhost)" + // Match `https://localhost`
                    "(?:\\/{1}\\?)" + // Match the query param `/?`
                    "([a-f\\d]{6}[-][a-f\\d]{8}[-](?:[a-f\\d]{4}[-]){3}[a-f\\d]{12})" + // Match the UUID
                    "\$"
                ).toPattern(Pattern.CASE_INSENSITIVE)
        }
    }

    @Parcelize
    data class RapidAntigen(
        override val type: CoronaTest.Type,
        override val guid: CoronaTestGUID,
        val createdAt: Instant,
        val firstName: String?,
        val lastName: String?,
        val dateOfBirth: LocalDate?,
    ) : CoronaTestQRCode() {

        constructor(rawString: String) :
            this(
                CoronaTest.Type.RAPID_ANTIGEN,
                extractData(rawString)
            )

        private constructor(
            type: CoronaTest.Type,
            data: EncodedData
        ) : this(
            type,
            data.guid,
            data.createdAt,
            data.firstName,
            data.lastName,
            data.dateOfBirth
        )

        companion object {
            const val prefix: String = "https://s.coronawarn.app?v=1#"
            const val prefix2: String = "https://s.coronawarn.app/?v=1#"

            private fun extractData(rawString: String): EncodedData {
                return rawString.removePrefix(prefix).decode()
            }

            private fun String.decode(): EncodedData {
                val decoded = if (this.contains("+") || this.contains("/") || this.contains("=")) {
                    BaseEncoding.base64().decode(this).commonToUtf8String()
                } else {
                    BaseEncoding.base64Url().decode(this).commonToUtf8String()
                }
                val data = Gson().fromJson<Payload>(decoded)
                return EncodedData(
                    guid = data.guid,
                    createdAt = Instant.ofEpochSecond(data.timestamp),
                    firstName = data.fn,
                    lastName = data.ln,
                    dateOfBirth = LocalDate.parse(data.dob)
                )
            }
        }

        private data class EncodedData(
            val guid: CoronaTestGUID,
            val createdAt: Instant,
            val firstName: String?,
            val lastName: String?,
            val dateOfBirth: LocalDate?
        )

        private data class Payload(
            val guid: String,
            val timestamp: Long,
            val fn: String?,
            val ln: String?,
            val dob: String?
        )
    }
}

typealias CoronaTestGUID = String
