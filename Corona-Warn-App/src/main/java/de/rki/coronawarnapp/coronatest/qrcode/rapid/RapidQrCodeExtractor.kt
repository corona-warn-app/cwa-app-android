package de.rki.coronawarnapp.coronatest.qrcode.rapid

import com.google.common.io.BaseEncoding
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import de.rki.coronawarnapp.bugreporting.censors.submission.RatQrCodeCensor
import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode
import de.rki.coronawarnapp.coronatest.qrcode.InvalidQRCodeException
import de.rki.coronawarnapp.qrcode.scanner.QrCodeExtractor
import de.rki.coronawarnapp.util.HashExtensions.toSHA256
import de.rki.coronawarnapp.util.hashing.isSha256Hash
import de.rki.coronawarnapp.util.serialization.fromJson
import okio.internal.commonToUtf8String
import org.joda.time.Instant
import org.joda.time.LocalDate
import timber.log.Timber

abstract class RapidQrCodeExtractor : QrCodeExtractor<CoronaTestQRCode> {

    protected abstract val loggingTag: String
    protected abstract fun String.removeQrCodePrefix(): String
    protected abstract fun CleanPayload.toCoronaTestQRCode(rawString: String): CoronaTestQRCode.Rapid

    override suspend fun extract(rawString: String): CoronaTestQRCode.Rapid {
        Timber.tag(loggingTag).v("extract(rawString=%s)", rawString)
        val payload = CleanPayload(extractData(rawString))

        RatQrCodeCensor.dataToCensor = RatQrCodeCensor.CensorData(
            rawString = rawString,
            hash = payload.hash,
            firstName = payload.firstName,
            lastName = payload.lastName,
            dateOfBirth = payload.dateOfBirth
        )

        payload.requireValidData()

        return payload.toCoronaTestQRCode(rawString = rawString)
    }

    private fun extractData(rawString: String): RawPayload {
        return rawString
            .removeQrCodePrefix()
            .decode()
    }

    private fun String.decode(): RawPayload {
        val decoded = try {
            if (
                this.contains("+") ||
                this.contains("/") ||
                this.contains("=")
            ) {
                BaseEncoding.base64().decode(this).commonToUtf8String()
            } else {
                BaseEncoding.base64Url().decode(this).commonToUtf8String()
            }
        } catch (e: Exception) {
            Timber.tag(loggingTag).e(e)
            throw InvalidQRCodeException("Unsupported encoding. Supported encodings are base64 and base64url.")
        }

        try {
            return Gson().fromJson(decoded)
        } catch (e: Exception) {
            Timber.tag(loggingTag).e(e)
            throw InvalidQRCodeException("Malformed payload.")
        }
    }

    data class RawPayload(
        @SerializedName("hash") val hash: String?,
        @SerializedName("timestamp") val timestamp: Long?,
        @SerializedName("fn") val firstName: String?,
        @SerializedName("ln") val lastName: String?,
        @SerializedName("dob") val dateOfBirth: String?,
        @SerializedName("testid") val testid: String?,
        @SerializedName("salt") val salt: String?,
        @SerializedName("dgc") val dgc: Boolean?
    )

    data class CleanPayload(val raw: RawPayload) {

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

        val testId: String? by lazy {
            if (raw.testid.isNullOrEmpty()) null else raw.testid
        }

        val salt: String? by lazy {
            if (raw.salt.isNullOrEmpty()) null else raw.salt
        }

        val isDccSupportedByPoc: Boolean by lazy { raw.dgc == true && allPersonalData.all { it != null } }

        fun requireValidData() {
            requireValidPersonalData()
            requireValidHash()
        }

        private val allPersonalData: List<Any?> by lazy { listOf(firstName, lastName, dateOfBirth) }

        private fun requireValidPersonalData() {
            val allOrNothing = allPersonalData.map { it != null }
            val complete = allOrNothing.all { it } || allOrNothing.all { !it }
            if (!complete) throw InvalidQRCodeException("QRCode contains incomplete personal data: $raw")
        }

        private fun requireValidHash() {
            val isQrCodeWithPersonalData = allPersonalData.all { it != null }
            val generatedHash = if (isQrCodeWithPersonalData)
                "${raw.dateOfBirth}#${raw.firstName}#${raw.lastName}#${raw.timestamp}#${raw.testid}#${raw.salt}"
                    .toSHA256()
            else "${raw.timestamp}#${raw.salt}".toSHA256()
            if (!generatedHash.equals(hash, true)) {
                throw InvalidQRCodeException("Generated hash doesn't match QRCode hash")
            }
        }
    }
}
