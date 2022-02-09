package de.rki.coronawarnapp.ccl.configuration.server

import androidx.annotation.VisibleForTesting
import dagger.Lazy
import dagger.Reusable
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.util.ZipHelper.readIntoMap
import de.rki.coronawarnapp.util.ZipHelper.unzip
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.retrofit.wasModified
import de.rki.coronawarnapp.util.security.SignatureValidation
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.HttpException
import retrofit2.Response
import timber.log.Timber
import javax.inject.Inject

@Reusable
class CCLConfigurationServer @Inject constructor(
    private val cclConfigurationApiLazy: Lazy<CCLConfigurationApiV1>,
    private val dispatcherProvider: DispatcherProvider,
    private val signatureValidation: SignatureValidation,
) {

    private val cclConfigurationApi
        get() = cclConfigurationApiLazy.get()

    suspend fun getCCLConfiguration(): ByteArray? = withContext(dispatcherProvider.IO) {
        Timber.tag(TAG).d("getCCLConfiguration()")
        try {
            val response = cclConfigurationApi.getCCLConfiguration()
            when (response.wasModified) {
                true -> response.parseAndValidate()
                false -> {
                    Timber.tag(TAG).d("CCL Configuration was not modified")
                    null
                }
            }
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Failed to get ccl configuration")
            throw e
        }
    }.also { Timber.tag(TAG).d("Returning %s", it) }

    private fun Response<ResponseBody>.parseAndValidate(): ByteArray {
        if (!isSuccessful) throw HttpException(this)

        val body = requireNotNull(body()) { "Body of response was null" }
        val fileMap = body.byteStream().unzip().readIntoMap()

        val exportBinary = fileMap[EXPORT_BINARY_FILE_NAME]
        val exportSignature = fileMap[EXPORT_SIGNATURE_FILE_NAME]

        if (exportBinary == null || exportSignature == null)
            throw CCLConfigurationInvalidSignatureException(msg = "Unknown files ${fileMap.entries}")

        val hasValidSignature = signatureValidation.hasValidSignature(
            toVerify = exportBinary,
            signatureList = SignatureValidation.parseTEKStyleSignature(exportSignature)
        )

        if (!hasValidSignature)
            throw CCLConfigurationInvalidSignatureException(msg = "Signature of ccl configuration did not match")

        return exportBinary
    }

    companion object {
        @VisibleForTesting const val EXPORT_BINARY_FILE_NAME = "export.bin"
        private const val EXPORT_SIGNATURE_FILE_NAME = "export.sig"
        private val TAG = tag<CCLConfigurationServer>()
    }
}
