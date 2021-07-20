package de.rki.coronawarnapp.covidcertificate.signature.core.server

import androidx.annotation.VisibleForTesting
import dagger.Lazy
import de.rki.coronawarnapp.covidcertificate.signature.core.common.exception.DscValidationException
import de.rki.coronawarnapp.covidcertificate.signature.core.common.exception.DscValidationException.ErrorCode
import de.rki.coronawarnapp.covidcertificate.validation.core.CertificateValidation
import de.rki.coronawarnapp.covidcertificate.validation.core.country.DccCountryApi
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.DccValidationRuleApi
import de.rki.coronawarnapp.util.ZipHelper.readIntoMap
import de.rki.coronawarnapp.util.ZipHelper.unzip
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.security.SignatureValidation
import okhttp3.Cache
import okhttp3.ResponseBody
import retrofit2.HttpException
import retrofit2.Response
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DscServer @Inject constructor(
    @CertificateValidation private val cache: Cache, // TODO: use right cache
    private val signatureValidation: SignatureValidation,
    private val dscApi: DscApiV1
) {

    // TODO: check app config DefaultAppConfigSource
    // update DscData
    suspend fun getDccList() {
        try {
            val result = dscApi.dscList().parseAndValidate(
                ErrorCode.FILE_MISSING,
                ErrorCode.SIGNATURE_INVALID,
                ErrorCode.EXTRACTION_FAILED
            )
            Timber.d("DSC LIST: $result")
        } catch (e: Exception) {
            Timber.e(e, "DSC LIST: error")
        }
    }

    @VisibleForTesting
    internal fun Response<ResponseBody>.parseAndValidate(
        fileMissingErrorCode: ErrorCode,
        invalidSignatureErrorCode: ErrorCode,
        extractionFailedCode: ErrorCode
    ): String {
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
//            return DscList.DSCList.parseFrom(exportBinary)
            throw NotImplementedError()
        } catch (e: Exception) {
            throw DscValidationException(extractionFailedCode, e)
        }
    }

    fun clear() {
        Timber.d("clear()")
        cache.evictAll()
    }

    companion object {
        private const val EXPORT_BINARY_FILE_NAME = "export.bin"
        private const val EXPORT_SIGNATURE_FILE_NAME = "export.sig"
    }
}
