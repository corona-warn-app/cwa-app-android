package de.rki.coronawarnapp.covidcertificate.validation.core.country.server

import com.upokecenter.cbor.CBORObject
import dagger.Lazy
import dagger.Reusable
import de.rki.coronawarnapp.covidcertificate.validation.core.CertificateValidation
import de.rki.coronawarnapp.covidcertificate.validation.core.common.exception.DccValidationException
import de.rki.coronawarnapp.covidcertificate.validation.core.common.exception.DccValidationException.ErrorCode
import de.rki.coronawarnapp.util.ZipHelper.readIntoMap
import de.rki.coronawarnapp.util.ZipHelper.unzip
import de.rki.coronawarnapp.util.security.SignatureValidation
import okhttp3.Cache
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

@Reusable
class DccCountryServer @Inject constructor(
    private val api: Lazy<DccCountryApi>,
    @CertificateValidation private val cache: Cache,
    private val signatureValidation: SignatureValidation,
) {

    suspend fun dccCountryJson(): String {
        Timber.tag(TAG).d("Fetching statistics.")

        val response = api.get().onboardedCountries()
        if (!response.isSuccessful) throw HttpException(response)

        val binary = with(
            requireNotNull(response.body()) { "Response was successful but body was null" }
        ) {
            val fileMap = byteStream().unzip().readIntoMap()

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
            throw DccValidationException(ErrorCode.ONBOARDED_COUNTRIES_JSON_EXTRACTION_FAILED)
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
