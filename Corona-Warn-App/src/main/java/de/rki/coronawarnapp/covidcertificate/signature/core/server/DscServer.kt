package de.rki.coronawarnapp.covidcertificate.signature.core.server

import androidx.annotation.VisibleForTesting
import de.rki.coronawarnapp.covidcertificate.signature.core.common.exception.DscValidationException
import de.rki.coronawarnapp.covidcertificate.signature.core.common.exception.DscValidationException.ErrorCode
import de.rki.coronawarnapp.server.protocols.internal.dgc.DscListOuterClass.DscList
import de.rki.coronawarnapp.util.ZipHelper.readIntoMap
import de.rki.coronawarnapp.util.ZipHelper.unzip
import de.rki.coronawarnapp.util.security.SignatureValidation
import okhttp3.ResponseBody
import okio.ByteString.Companion.decodeBase64
import retrofit2.HttpException
import retrofit2.Response
import timber.log.Timber
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@Suppress("MaxLineLength")
class DscServer @Inject constructor(
    private val signatureValidation: SignatureValidation,
    private val dscApi: DscApiV1
) {

    suspend fun getDscList(): String? {
        return try {
            dscApi.dscList().parseAndValidate()
        } catch (e: Exception) {
            Timber.e(e, "Can't get DSC list")
            null
        }
    }

    suspend fun getDscCDN(): DscList? {
        return try {
            dscApi.dscListCDN().parseAndValidate(
                ErrorCode.FILE_MISSING,
                ErrorCode.SIGNATURE_INVALID,
                ErrorCode.EXTRACTION_FAILED
            )
        } catch (e: Exception) {
            Timber.e(e, "Can't get DSC list")
            null
        }
    }

    @VisibleForTesting
    internal fun Response<ResponseBody>.parseAndValidate(
        fileMissingErrorCode: ErrorCode,
        invalidSignatureErrorCode: ErrorCode,
        extractionFailedCode: ErrorCode
    ): DscList {
        if (!isSuccessful) throw HttpException(this)

        val fileMap = requireNotNull(body()) { "Response was successful but body was null" }
            .byteStream().unzip().readIntoMap()

        val exportBinary = fileMap[EXPORT_BINARY_FILE_NAME]
        val exportSignature = fileMap[EXPORT_SIGNATURE_FILE_NAME]

        if (exportBinary == null || exportSignature == null) throw DscValidationException(fileMissingErrorCode)

        val isSignatureValid = signatureValidation.hasValidSignature(
            toVerify = exportBinary,
            signatureList = SignatureValidation.parseTEKStyleSignature(exportSignature)
        )
        if (!isSignatureValid) throw DscValidationException(invalidSignatureErrorCode)

        try {
            return DscList.parseFrom(exportBinary)
        } catch (e: Exception) {
            throw DscValidationException(extractionFailedCode, e)
        }
    }

    @VisibleForTesting
    internal fun Response<ResponseBody>.parseAndValidate(): String? {
        if (!isSuccessful) throw HttpException(this)

        val data = requireNotNull(body()) { "Response was successful but body was null" }.string()

        val encodedSignature = data.substringBefore("{")
        val signature = encodedSignature.decodeBase64()?.toByteArray() ?: return null
        val trustedList = data.substring(encodedSignature.length).trim()

        // TODO: move somewhere else
        // v1:
        val keyProd = readPemKeys(
            """-----BEGIN PUBLIC KEY-----
MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAETHfi8foQF4UtSNVxSFxeu7W+gMxd
SGElhdo7825SD3Lyb+Sqh4G6Kra0ro1BdrM6Qx+hsUx4Qwdby7QY0pzxyA==
-----END PUBLIC KEY-----
            """.trimIndent()
        )

        val keyDev = readPemKeys(
            """-----BEGIN PUBLIC KEY-----
MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEIxHvrv8jQx9OEzTZbsx1prQVQn/3
ex0gMYf6GyaNBW0QKLMjrSDeN6HwSPM0QzhvhmyQUixl6l88A7Zpu5OWSw==
-----END PUBLIC KEY-----
            """.trimIndent()
        )

        // v2:
        val publicKeyString =
            "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEIxHvrv8jQx9OEzTZbsx1prQVQn/3ex0gMYf6GyaNBW0QKLMjrSDeN6HwSPM0QzhvhmyQUixl6l88A7Zpu5OWSw=="
        val publicKeySpec: EncodedKeySpec = X509EncodedKeySpec(publicKeyString.decodeBase64()?.toByteArray())
        val keyFactory: KeyFactory = KeyFactory.getInstance("EC")
        val publicKey: PublicKey = keyFactory.generatePublic(publicKeySpec)

        return try {
            validateSignature(publicKey, trustedList.toByteArray(), signature, "SHA256withECDSA")
            Timber.d("Signature OK")
            trustedList
        } catch (exception: Exception) {
            Timber.e(exception, "Signature FAIL")
            null
        }
    }

    companion object {
        private const val EXPORT_BINARY_FILE_NAME = "export.bin"
        private const val EXPORT_SIGNATURE_FILE_NAME = "export.sig"
    }
}
