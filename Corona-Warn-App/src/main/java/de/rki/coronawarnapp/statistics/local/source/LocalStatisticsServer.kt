package de.rki.coronawarnapp.statistics.local.source

import dagger.Lazy
import dagger.Reusable
import de.rki.coronawarnapp.statistics.Statistics
import de.rki.coronawarnapp.statistics.local.FederalStateToPackageId
import de.rki.coronawarnapp.statistics.source.InvalidStatisticsSignatureException
import de.rki.coronawarnapp.util.ZipHelper.readIntoMap
import de.rki.coronawarnapp.util.ZipHelper.unzip
import de.rki.coronawarnapp.util.reset.Resettable
import de.rki.coronawarnapp.util.security.SignatureValidation
import okhttp3.Cache
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

@Reusable
class LocalStatisticsServer @Inject constructor(
    private val api: Lazy<LocalStatisticsApiV1>,
    private val signatureValidation: SignatureValidation,
    @Statistics val cache: Cache
) : Resettable {

    suspend fun getRawLocalStatistics(federalState: FederalStateToPackageId): ByteArray {
        Timber.d("Fetching Local statistics.")

        val response = api.get().getLocalStatistics(federalState.packageId)
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

            val hasValidSignature = signatureValidation.hasValidSignature(
                exportBinary,
                SignatureValidation.parseTEKStyleSignature(exportSignature)
            )

            if (!hasValidSignature) {
                throw InvalidStatisticsSignatureException(message = "Statistics signature did not match.")
            }

            exportBinary
        }
    }

    override suspend fun reset() {
        Timber.d("reset()")
        runCatching { cache.evictAll() }
            .onFailure { Timber.e(it, "Failed to clear cache") }
    }

    companion object {
        private const val EXPORT_BINARY_FILE_NAME = "export.bin"
        private const val EXPORT_SIGNATURE_FILE_NAME = "export.sig"
    }
}
