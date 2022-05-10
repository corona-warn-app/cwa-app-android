package de.rki.coronawarnapp.covidcertificate.valueset.server

import androidx.annotation.VisibleForTesting
import dagger.Lazy
import dagger.Reusable
import de.rki.coronawarnapp.covidcertificate.valueset.internal.ValueSetInvalidSignatureException
import de.rki.coronawarnapp.covidcertificate.valueset.internal.toValueSetsContainer
import de.rki.coronawarnapp.covidcertificate.valueset.valuesets.ValueSetsContainer
import de.rki.coronawarnapp.server.protocols.internal.dgc.ValueSetsOuterClass
import de.rki.coronawarnapp.util.ZipHelper.readIntoMap
import de.rki.coronawarnapp.util.ZipHelper.unzip
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.reset.Resettable
import de.rki.coronawarnapp.util.security.SignatureValidation
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
class CertificateValueSetServer @Inject constructor(
    @CertificateValueSet private val cache: Cache,
    private val apiV1: Lazy<CertificateValueSetApiV1>,
    private val dispatcherProvider: DispatcherProvider,
    private val signatureValidation: SignatureValidation
) : Resettable {

    suspend fun getVaccinationValueSets(languageCode: Locale): ValueSetsContainer? =
        withContext(dispatcherProvider.Default) {
            return@withContext try {
                val response = requestValueSets(languageCode.language)
                if (!response.isSuccessful) throw HttpException(response)

                val body = requireNotNull(response.body()) { "Body of response was null" }
                val valueSetsProtobuf = body.parseBody()
                valueSetsProtobuf.toValueSetsContainer(languageCode = languageCode)
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

    override suspend fun reset() {
        Timber.d("Clearing cache")
        runCatching { cache.evictAll() }
            .onFailure { Timber.e(it, "Failed to clear cache") }
    }
}

private const val EXPORT_BINARY_FILE_NAME = "export.bin"
private const val EXPORT_SIGNATURE_FILE_NAME = "export.sig"
