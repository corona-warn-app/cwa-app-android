package de.rki.coronawarnapp.statistics.source

import dagger.Lazy
import dagger.Reusable
import de.rki.coronawarnapp.statistics.Statistics
import de.rki.coronawarnapp.util.ZipHelper.readIntoMap
import de.rki.coronawarnapp.util.ZipHelper.unzip
import de.rki.coronawarnapp.util.security.VerificationKeys
import okhttp3.Cache
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

@Reusable
class StatisticsServer @Inject constructor(
    private val api: Lazy<StatisticsApiV1>,
    private val verificationKeys: VerificationKeys,
    @Statistics val cache: Cache
) {

    suspend fun getRawStatistics(): ByteArray {
        Timber.tag(TAG).d("Fetching statistics.")

        val response = api.get().getStatistics()
        if (!response.isSuccessful) throw HttpException(response)

        return with(
            requireNotNull(response.body()) { "Response was successful but body was null" }
        ) {
            val fileMap = byteStream().unzip().readIntoMap()

            val exportBinary = fileMap[EXPORT_BINARY_FILE_NAME]
            val exportSignature = fileMap[EXPORT_SIGNATURE_FILE_NAME]

            if (exportBinary == null || exportSignature == null) {
                throw IOException("Unknown files: ${fileMap.keys}")
            }

            if (verificationKeys.hasInvalidSignature(exportBinary, exportSignature)) {
                throw InvalidStatisticsSignatureException(message = "Statistics signature did not match.")
            }

            exportBinary
        }
    }

    fun clear() {
        Timber.d("clear()")
        cache.evictAll()
    }

    companion object {
        private const val EXPORT_BINARY_FILE_NAME = "export.bin"
        private const val EXPORT_SIGNATURE_FILE_NAME = "export.sig"
        private const val TAG = "StatisticsServer"
    }
}
