package de.rki.coronawarnapp.covidcertificate.common.exception

import android.content.Context
import de.rki.coronawarnapp.coronatest.qrcode.InvalidQRCodeException
import de.rki.coronawarnapp.util.HasHumanReadableError
import de.rki.coronawarnapp.util.HumanReadableError
import de.rki.coronawarnapp.util.ui.CachedString
import de.rki.coronawarnapp.util.ui.LazyString

@Suppress("MaxLineLength")
open class InvalidHealthCertificateException(
    val errorCode: ErrorCode,
    cause: Throwable? = null,
) : HasHumanReadableError, InvalidQRCodeException(errorCode.message, cause) {
    enum class ErrorCode(
        val message: String
    ) {
        HC_BASE45_DECODING_FAILED("Base45 decoding failed."),
        HC_BASE45_ENCODING_FAILED("Base45 encoding failed."),
        HC_ZLIB_DECOMPRESSION_FAILED("Zlib decompression failed."),
        HC_ZLIB_COMPRESSION_FAILED("Zlib compression failed."),
        HC_COSE_TAG_INVALID("COSE tag invalid."),
        HC_COSE_MESSAGE_INVALID("COSE message invalid."),
        HC_CBOR_DECODING_FAILED("CBOR decoding failed."),
        NO_VACCINATION_ENTRY("Vaccination certificate missing."),
        MULTIPLE_VACCINATION_ENTRIES("Multiple vaccination certificates."),
        NO_TEST_ENTRY("Test certificate missing."),
        NO_RECOVERY_ENTRY("Recovery certificate missing."),
        PREFIX_INVALID("Prefix invalid."),
        STORING_FAILED("Storing failed."),
        JSON_SCHEMA_INVALID("Json schema invalid."),
        NAME_MISMATCH("Name does not match."),
        ALREADY_REGISTERED("Certificate already registered."),
        DOB_MISMATCH("Date of birth does not match."),
        HC_CWT_NO_DGC("Dgc missing."),
        HC_CWT_NO_EXP("Expiration date missing."),
        HC_CWT_NO_HCERT("Health certificate missing."),
        HC_CWT_NO_ISS("Issuer missing."),

        AES_DECRYPTION_FAILED("AES decryption failed"),
        RSA_DECRYPTION_FAILED("RSA decryption failed."),
        RSA_KP_GENERATION_FAILED("RSA key pair generation failed."),
    }

    open val errorMessage: LazyString
        get() = CachedString { context ->
            context.getString(ERROR_MESSAGE_GENERIC)
        }

    override fun toHumanReadableError(context: Context): HumanReadableError {
        return HumanReadableError(
            description = errorMessage.get(context) + " ($errorCode)"
        )
    }
}
