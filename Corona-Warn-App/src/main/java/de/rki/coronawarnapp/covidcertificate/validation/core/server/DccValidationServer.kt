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
    private val countryApi: Lazy<DccCountryApi>,
    private val rulesApi: Lazy<DccValidationRuleApi>,
    @CertificateValidation private val cache: Cache,
    private val signatureValidation: SignatureValidation,
    private val dispatcherProvider: DispatcherProvider,
) {

    private val dccValidationRuleApi: DccValidationRuleApi
        get() = rulesApi.get()

    suspend fun ruleSetJson(ruleTypeDcc: DccValidationRule.Type): String =
        withContext(dispatcherProvider.IO) {
            return@withContext try {
                when (ruleTypeDcc) {
                    DccValidationRule.Type.ACCEPTANCE -> {
                        parseAndValidate(
                            dccValidationRuleApi.acceptanceRules(),
                            ErrorCode.ACCEPTANCE_RULE_JSON_ARCHIVE_FILE_MISSING,
                            ErrorCode.ACCEPTANCE_RULE_JSON_ARCHIVE_SIGNATURE_INVALID
                        )
                    }
                    DccValidationRule.Type.INVALIDATION -> {
                        parseAndValidate(
                            dccValidationRuleApi.invalidationRules(),
                            ErrorCode.INVALIDATION_RULE_JSON_ARCHIVE_FILE_MISSING,
                            ErrorCode.INVALIDATION_RULE_JSON_ARCHIVE_SIGNATURE_INVALID
                        )
                    }
                }
            } catch (e: Exception) {
                if (e is DccValidationException) throw e
                Timber.e(e, "Getting rule set from server failed cause: ${e.message}")
                throw DccValidationException(ErrorCode.ACCEPTANCE_RULE_SERVER_ERROR, e)
            }
        }

    suspend fun dccCountryJson(): String {
        Timber.tag(TAG).d("Fetching dcc countries.")
        return countryApi.get().onboardedCountries().let {
            try {
                parseAndValidate(
                    it,
                    ErrorCode.ONBOARDED_COUNTRIES_JSON_ARCHIVE_FILE_MISSING,
                    ErrorCode.ONBOARDED_COUNTRIES_JSON_ARCHIVE_SIGNATURE_INVALID
                )
            } catch (e: Exception) {
                if (e is DccValidationException) throw e
                Timber.tag(TAG).e(e, "CBOR decoding binary to json failed.")
                throw DccValidationException(ErrorCode.ONBOARDED_COUNTRIES_JSON_DECODING_FAILED, e)
            }
        }
    }

    @VisibleForTesting
    internal fun parseAndValidate(
        response: Response<ResponseBody>,
        fileMissingErrorCode: ErrorCode,
        invalidSignatureErrorCode: ErrorCode
    ): String {
        if (!response.isSuccessful) throw HttpException(response)

        val fileMap = requireNotNull(response.body()) { "Response was successful but body was null" }
            .byteStream().unzip().readIntoMap()

        val exportBinary = fileMap[EXPORT_BINARY_FILE_NAME]
        val exportSignature = fileMap[EXPORT_SIGNATURE_FILE_NAME]

        if (exportBinary == null || exportSignature == null) throw DccValidationException(fileMissingErrorCode)

        val isSignatureValid = signatureValidation.hasValidSignature(
            toVerify = exportBinary,
            signatureList = SignatureValidation.parseTEKStyleSignature(exportSignature)
        )
        if (!isSignatureValid) throw DccValidationException(invalidSignatureErrorCode)

        return CBORObject.DecodeFromBytes(exportBinary).ToJSONString()
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
