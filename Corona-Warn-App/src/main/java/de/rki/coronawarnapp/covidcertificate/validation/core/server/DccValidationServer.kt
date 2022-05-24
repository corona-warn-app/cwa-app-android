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
import de.rki.coronawarnapp.exception.http.CwaUnknownHostException
import de.rki.coronawarnapp.util.ZipHelper.readIntoMap
import de.rki.coronawarnapp.util.ZipHelper.unzip
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.reset.Resettable
import de.rki.coronawarnapp.util.retrofit.wasModified
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
) : Resettable {

    private val countryApi: DccCountryApi
        get() = countryApiLazy.get()

    private val rulesApi: DccValidationRuleApi
        get() = rulesApiLazy.get()

    suspend fun ruleSetJson(ruleTypeDcc: DccValidationRule.Type): RuleSetResult = withContext(dispatcherProvider.IO) {
        try {
            Timber.tag(TAG).v("Fetching $ruleTypeDcc rule set...")
            when (ruleTypeDcc) {
                DccValidationRule.Type.ACCEPTANCE -> {
                    val response = rulesApi.acceptanceRules()
                    val source = getSource(response)
                    Timber.tag(TAG).v("Source of acceptanceRules: %s", source)

                    val ruleSet = response.parseAndValidate(
                        ErrorCode.ACCEPTANCE_RULE_JSON_ARCHIVE_FILE_MISSING,
                        ErrorCode.ACCEPTANCE_RULE_JSON_ARCHIVE_SIGNATURE_INVALID,
                        ErrorCode.ACCEPTANCE_RULE_JSON_EXTRACTION_FAILED,
                    )
                    RuleSetResult(ruleSet, source)
                }
                DccValidationRule.Type.INVALIDATION -> {

                    val response = rulesApi.invalidationRules()
                    val source = getSource(response)
                    Timber.tag(TAG).v("Source of invalidationRules: %s", source)

                    val ruleSet = response.parseAndValidate(
                        ErrorCode.INVALIDATION_RULE_JSON_ARCHIVE_FILE_MISSING,
                        ErrorCode.INVALIDATION_RULE_JSON_ARCHIVE_SIGNATURE_INVALID,
                        ErrorCode.INVALIDATION_RULE_JSON_EXTRACTION_FAILED,
                    )

                    RuleSetResult(ruleSet, source)
                }
                DccValidationRule.Type.BOOSTER_NOTIFICATION -> {

                    val response = rulesApi.boosterNotificationRules()
                    val source = getSource(response)
                    Timber.tag(TAG).v("Source of boosterNotificationRules: %s", source)

                    val ruleSet = response.parseAndValidate(
                        ErrorCode.BOOSTER_NOTIFICATION_RULE_JSON_ARCHIVE_FILE_MISSING,
                        ErrorCode.BOOSTER_NOTIFICATION_RULE_JSON_ARCHIVE_SIGNATURE_INVALID,
                        ErrorCode.BOOSTER_NOTIFICATION_RULE_JSON_EXTRACTION_FAILED
                    )

                    RuleSetResult(ruleSet, source)
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Getting $ruleTypeDcc rule set failed.")
            throw when (e) {
                is DccValidationException -> e
                is CwaUnknownHostException -> DccValidationException(ErrorCode.NO_NETWORK, e)
                else -> {
                    val type = when (ruleTypeDcc) {
                        DccValidationRule.Type.ACCEPTANCE -> ErrorCode.ACCEPTANCE_RULE_SERVER_ERROR
                        DccValidationRule.Type.INVALIDATION -> ErrorCode.INVALIDATION_RULE_SERVER_ERROR
                        DccValidationRule.Type.BOOSTER_NOTIFICATION -> ErrorCode.BOOSTER_NOTIFICATION_RULE_SERVER_ERROR
                    }
                    DccValidationException(type, e)
                }
            }
        }
    }

    suspend fun dccCountryJson(): String = withContext(dispatcherProvider.IO) {
        try {
            Timber.tag(TAG).v("Fetching dcc countries...")
            countryApi.onboardedCountries().parseAndValidate(
                ErrorCode.ONBOARDED_COUNTRIES_JSON_ARCHIVE_FILE_MISSING,
                ErrorCode.ONBOARDED_COUNTRIES_JSON_ARCHIVE_SIGNATURE_INVALID,
                ErrorCode.ONBOARDED_COUNTRIES_JSON_EXTRACTION_FAILED,
            )
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Getting dcc countries failed.")
            throw when (e) {
                is DccValidationException -> e
                is CwaUnknownHostException -> DccValidationException(ErrorCode.NO_NETWORK, e)
                else -> DccValidationException(ErrorCode.ONBOARDED_COUNTRIES_SERVER_ERROR, e)
            }
        }
    }

    override suspend fun reset() {
        Timber.tag(TAG).d("clear()")
        runCatching { cache.evictAll() }
            .onFailure { Timber.tag(TAG).e(it, "Failed to clear cache") }
    }

    private fun Response<ResponseBody>.parseAndValidate(
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

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun getSource(response: Response<ResponseBody>) = if (response.wasModified) {
        RuleSetSource.SERVER
    } else {
        RuleSetSource.CACHE
    }

    data class RuleSetResult(
        val ruleSetJson: String,
        val source: RuleSetSource
    )

    enum class RuleSetSource {
        SERVER, CACHE
    }

    companion object {
        private const val EXPORT_BINARY_FILE_NAME = "export.bin"
        private const val EXPORT_SIGNATURE_FILE_NAME = "export.sig"
        private const val TAG = "DccValidationServer"
    }
}
