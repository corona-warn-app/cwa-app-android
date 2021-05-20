package de.rki.coronawarnapp.vaccination.core.server.valueset

import androidx.annotation.VisibleForTesting
import dagger.Lazy
import dagger.Reusable
import de.rki.coronawarnapp.server.protocols.internal.dgc.ValueSetsOuterClass
import de.rki.coronawarnapp.util.ZipHelper.readIntoMap
import de.rki.coronawarnapp.util.ZipHelper.unzip
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.security.SignatureValidation
import de.rki.coronawarnapp.vaccination.core.server.valueset.internal.ValueSetInvalidSignatureException
import de.rki.coronawarnapp.vaccination.core.server.valueset.internal.toVaccinationValueSet
import kotlinx.coroutines.withContext
import okhttp3.Cache
import okhttp3.ResponseBody
import retrofit2.HttpException
import retrofit2.Response
import timber.log.Timber
import java.io.InputStream
import java.util.Locale
import javax.inject.Inject

/**
 * Talks with CWA servers
 */
@Reusable
class VaccinationServer @Inject constructor(
    @ValueSet private val cache: Cache,
    private val apiV1: Lazy<VaccinationValueSetApiV1>,
    private val dispatcherProvider: DispatcherProvider,
    private val signatureValidation: SignatureValidation
) {

    suspend fun getVaccinationValueSets(languageCode: Locale): VaccinationValueSet? =
        withContext(dispatcherProvider.Default) {
            return@withContext try {
                val response = requestValueSets(languageCode.language)
                if (!response.isSuccessful) throw HttpException(response)

                val body = requireNotNull(response.body()) { "Body of response was null" }
                val valueSetsProtobuf = body.parseBody()
                valueSetsProtobuf.toVaccinationValueSet(languageCode = languageCode)
            } catch (e: Exception) {
                Timber.e(e, "Getting vaccination value sets from server failed cause: ${e.message}")
                null
            }
        }

    private suspend fun requestValueSets(languageCode: String): Response<ResponseBody> =
        withContext(dispatcherProvider.IO) {
            Timber.d("Requesting value sets for language $languageCode from server")
            apiV1.get().getValueSets(languageCode = languageCode)
        }

    private fun ResponseBody.parseBody(): ValueSetsOuterClass.ValueSets =
        parseBody(byteStream())

    @VisibleForTesting
    internal fun parseBody(inputStream: InputStream): ValueSetsOuterClass.ValueSets {
        val fileMap = inputStream.unzip().readIntoMap()

        val exportBinary = fileMap[EXPORT_BINARY_FILE_NAME]
        val exportSignature = fileMap[EXPORT_SIGNATURE_FILE_NAME]

        if (exportBinary == null || exportSignature == null)
            throw ValueSetInvalidSignatureException(msg = "Unknown files ${fileMap.entries}")

        val hasValidSignature = signatureValidation.hasValidSignature(
            toVerify = exportBinary,
            signatureList = SignatureValidation.parseTEKStyleSignature(exportSignature)
        )

        if (!hasValidSignature) {
            throw ValueSetInvalidSignatureException(msg = "Signature of value sets did not match")
        }

        return ValueSetsOuterClass.ValueSets.parseFrom(exportBinary)
    }

    fun clear() {
        // Clear cache
        Timber.d("Clearing cache")
        cache.evictAll()
    }
}

private const val EXPORT_BINARY_FILE_NAME = "export.bin"
private const val EXPORT_SIGNATURE_FILE_NAME = "export.sig"
