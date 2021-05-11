package de.rki.coronawarnapp.vaccination.core.server.valueset

import dagger.Reusable
import de.rki.coronawarnapp.server.protocols.internal.dgc.ValueSetsOuterClass
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
import java.util.Locale
import javax.inject.Inject

/**
 * Talks with CWA servers
 */
@Reusable
class VaccinationServer @Inject constructor(
    @VaccinationValueSetHttpClient private val cache: Cache,
    private val apiV1: VaccinationValueSetApiV1,
    private val dispatcherProvider: DispatcherProvider,
    private val signatureValidation: SignatureValidation
) {

    suspend fun getVaccinationValueSets(languageCode: Locale): VaccinationValueSet? =
        withContext(dispatcherProvider.Default) {
            try {
                val response = requestValueSets(languageCode.language)

                if (!response.isSuccessful) throw HttpException(response)

                if (response.code() == 304) {
                    Timber.d("")
                    return@withContext null
                }

                val body = requireNotNull(response.body()) { "Body of response was null" }
                val valueSetsProtobuf = parseBody(body)
                valueSetsProtobuf.toVaccinationValueSet(languageCode = languageCode)
            } catch (e: Exception) {
                Timber.e(e)
            }

            return@withContext null
        }

    private suspend fun requestValueSets(languageCode: String): Response<ResponseBody> =
        withContext(dispatcherProvider.IO) {
            Timber.d("Getting value sets for language $languageCode")
            apiV1.getValueSets(languageCode = languageCode)
        }

    private fun parseBody(body: ResponseBody): ValueSetsOuterClass.ValueSets {
        val fileMap = body.byteStream().unzip().readIntoMap()

        val exportBinary = fileMap[EXPORT_BINARY_FILE_NAME]
        val exportSignature = fileMap[EXPORT_SIGNATURE_FILE_NAME]

        if (exportBinary == null || exportSignature == null) throw Exception("Unknown files ${fileMap.entries}")

        val hasValidSignature = signatureValidation.hasValidSignature(
            toVerify = exportBinary,
            signatureList = SignatureValidation.parseTEKStyleSignature(exportSignature)
        )

        if (!hasValidSignature) {
            throw Exception("Signature was invalid!")
        }

        return ValueSetsOuterClass.ValueSets.parseFrom(exportBinary)
    }

    private fun ValueSetsOuterClass.ValueSets.toVaccinationValueSet(languageCode: Locale): VaccinationValueSet =
        DefaultVaccinationValueSet(
            languageCode = languageCode,
            vp = vp.toValueSet(),
            mp = mp.toValueSet(),
            ma = ma.toValueSet()
        )

    private fun ValueSetsOuterClass.ValueSet.toValueSet(): VaccinationValueSet.ValueSet =
        DefaultVaccinationValueSet.DefaultValueSet(items = itemsList.map { it.toItem() })

    private fun ValueSetsOuterClass.ValueSetItem.toItem(): VaccinationValueSet.ValueSet.Item =
        DefaultVaccinationValueSet.DefaultValueSet.DefaultItem(
            key = key,
            displayText = displayText
        )

    fun clear() {
        // Clear cache
        Timber.d("Clearing cache")
        cache.evictAll()
    }
}

private const val EXPORT_BINARY_FILE_NAME = "export.bin"
private const val EXPORT_SIGNATURE_FILE_NAME = "export.sig"
