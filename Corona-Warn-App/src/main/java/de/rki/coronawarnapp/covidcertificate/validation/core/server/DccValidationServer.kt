package de.rki.coronawarnapp.covidcertificate.validation.core.server

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
import de.rki.coronawarnapp.util.security.SignatureValidation
import okhttp3.Cache
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

@Reusable
class DccValidationServer @Inject constructor(
    private val countryApi: Lazy<DccCountryApi>,
    private val rulesApi: Lazy<DccValidationRuleApi>,
    @CertificateValidation private val cache: Cache,
    private val signatureValidation: SignatureValidation,
) {

    private val dccValidationRuleApi: DccValidationRuleApi
        get() = rulesApi.get()

    suspend fun ruleSet(ruleTypeDcc: DccValidationRule.Type): Set<DccValidationRule> {
        return when (ruleTypeDcc) {
            DccValidationRule.Type.ACCEPTANCE -> dccValidationRuleApi.acceptanceRules()
            DccValidationRule.Type.INVALIDATION -> dccValidationRuleApi.invalidationRules()
        }
    }

    suspend fun dccCountryJson(): String {
        Timber.tag(TAG).d("Fetching dcc countries.")

        val response = countryApi.get().onboardedCountries()
        if (!response.isSuccessful) throw HttpException(response)

        val binary = with(
            requireNotNull(response.body()) { "Response was successful but body was null" }
        ) {
            val fileMap = try {
                byteStream().unzip().readIntoMap()
            } catch (e: Exception) {
                throw DccValidationException(ErrorCode.ONBOARDED_COUNTRIES_JSON_EXTRACTION_FAILED, e)
            }

            val exportBinary = fileMap[EXPORT_BINARY_FILE_NAME]
            val exportSignature = fileMap[EXPORT_SIGNATURE_FILE_NAME]

            if (exportBinary == null || exportSignature == null) {
                throw DccValidationException(
                    ErrorCode.ONBOARDED_COUNTRIES_JSON_ARCHIVE_FILE_MISSING,
                    IOException("Unknown files: ${fileMap.keys}")
                )
            }

            val hasValidSignature = signatureValidation.hasValidSignature(
                exportBinary,
                SignatureValidation.parseTEKStyleSignature(exportSignature)
            )

            if (!hasValidSignature) {
                throw DccValidationException(ErrorCode.ONBOARDED_COUNTRIES_JSON_ARCHIVE_SIGNATURE_INVALID)
            }

            exportBinary
        }

        return try {
            CBORObject.DecodeFromBytes(binary).ToJSONString()
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "CBOR decoding binary to json failed.")
            throw DccValidationException(ErrorCode.ONBOARDED_COUNTRIES_JSON_DECODING_FAILED, e)
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
