package de.rki.coronawarnapp.covidcertificate.validation.core.server

import androidx.annotation.VisibleForTesting
import com.upokecenter.cbor.CBORObject
import dagger.Lazy
import dagger.Reusable
import de.rki.coronawarnapp.covidcertificate.validation.core.CertificateValidation
import de.rki.coronawarnapp.covidcertificate.validation.core.common.exception.DccValidationException
import de.rki.coronawarnapp.covidcertificate.validation.core.common.exception.DccValidationException.ErrorCode
import de.rki.coronawarnapp.covidcertificate.validation.core.country.DccCountryApi
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.DccValidationRule
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.DccValidationRuleApi
import de.rki.coronawarnapp.util.ZipHelper.readIntoMap
import de.rki.coronawarnapp.util.ZipHelper.unzip
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.security.SignatureValidation
import kotlinx.coroutines.withContext
import okhttp3.Cache
import okhttp3.ResponseBody
import retrofit2.HttpException
import retrofit2.Response
import timber.log.Timber
import javax.inject.Inject

@Reusable
class DccValidationServer @Inject constructor(
    private val countryApiLazy: Lazy<DccCountryApi>,
    private val rulesApiLazy: Lazy<DccValidationRuleApi>,
    @CertificateValidation private val cache: Cache,
    private val signatureValidation: SignatureValidation,
    private val dispatcherProvider: DispatcherProvider,
) {

    private val countryApi: DccCountryApi
        get() = countryApiLazy.get()

    private val rulesApi: DccValidationRuleApi
        get() = rulesApiLazy.get()

    suspend fun ruleSetJson(ruleTypeDcc: DccValidationRule.Type): String = withContext(dispatcherProvider.IO) {
        try {
            when (ruleTypeDcc) {
                DccValidationRule.Type.ACCEPTANCE -> {
                    rulesApi.acceptanceRules().parseAndValidate(
                        ErrorCode.ACCEPTANCE_RULE_JSON_ARCHIVE_FILE_MISSING,
                        ErrorCode.ACCEPTANCE_RULE_JSON_ARCHIVE_SIGNATURE_INVALID,
                        ErrorCode.ACCEPTANCE_RULE_JSON_EXTRACTION_FAILED,
                    )
                }
                DccValidationRule.Type.INVALIDATION -> {
                    rulesApi.invalidationRules().parseAndValidate(
                        ErrorCode.INVALIDATION_RULE_JSON_ARCHIVE_FILE_MISSING,
                        ErrorCode.INVALIDATION_RULE_JSON_ARCHIVE_SIGNATURE_INVALID,
                        ErrorCode.INVALIDATION_RULE_JSON_EXTRACTION_FAILED,
                    )
                }
            }
        } catch (e: Exception) {
            if (e is DccValidationException) throw e
            Timber.e(e, "Getting rule set from server failed cause: ${e.message}")
            throw DccValidationException(ErrorCode.ACCEPTANCE_RULE_SERVER_ERROR, e)
        }
    }

    suspend fun dccCountryJson(): String = withContext(dispatcherProvider.IO) {
        Timber.tag(TAG).d("Fetching dcc countries.")
        try {
            countryApi.onboardedCountries().parseAndValidate(
                ErrorCode.ONBOARDED_COUNTRIES_JSON_ARCHIVE_FILE_MISSING,
                ErrorCode.ONBOARDED_COUNTRIES_JSON_ARCHIVE_SIGNATURE_INVALID,
                ErrorCode.ONBOARDED_COUNTRIES_JSON_EXTRACTION_FAILED,
            )
        } catch (e: Exception) {
            if (e is DccValidationException) throw e
            Timber.tag(TAG).e(e, "CBOR decoding binary to json failed.")
            throw DccValidationException(ErrorCode.ONBOARDED_COUNTRIES_JSON_DECODING_FAILED, e)
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

        if (exportBinary == null || exportSignature == null) throw DccValidationException(fileMissingErrorCode)

        val isSignatureValid = signatureValidation.hasValidSignature(
            toVerify = exportBinary,
            signatureList = SignatureValidation.parseTEKStyleSignature(exportSignature)
        )
        if (!isSignatureValid) throw DccValidationException(invalidSignatureErrorCode)

        try {
            return CBORObject.DecodeFromBytes(exportBinary).ToJSONString()
        } catch (e: Exception) {
            throw DccValidationException(extractionFailedCode, e)
        }
    }

    fun clear() {
        Timber.d("clear()")
        cache.evictAll()
    }

    companion object {
        private const val EXPORT_BINARY_FILE_NAME = "export.bin"
        private const val EXPORT_SIGNATURE_FILE_NAME = "export.sig"
        private const val TAG = "DccCountryServer"
    }
}
