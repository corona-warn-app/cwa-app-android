package de.rki.coronawarnapp.diagnosiskeys.download

import dagger.Reusable
import de.rki.coronawarnapp.diagnosiskeys.server.DiagnosisKeyServer
import de.rki.coronawarnapp.diagnosiskeys.server.DownloadInfo
import de.rki.coronawarnapp.diagnosiskeys.storage.CachedKey
import de.rki.coronawarnapp.diagnosiskeys.storage.KeyCacheRepository
import de.rki.coronawarnapp.diagnosiskeys.storage.legacy.LegacyKeyCacheMigration
import de.rki.coronawarnapp.util.HashExtensions.hashToMD5
import timber.log.Timber
import javax.inject.Inject

@Reusable
class DownloadTool @Inject constructor(
    private val legacyKeyCache: LegacyKeyCacheMigration,
    private val keyServer: DiagnosisKeyServer,
    private val keyCache: KeyCacheRepository
) {
    suspend fun downloadKeyFile(
        cachedKey: CachedKey
    ): CachedKey? = try {
        val saveTo = cachedKey.path
        val keyInfo = cachedKey.info

        val preconditionHook: suspend (DownloadInfo) -> Boolean =
            { downloadInfo ->
                /**
                 * To try legacy migration, we attempt to the etag as checksum.
                 * Removing the quotes, the etag can represent the file's MD5 checksum.
                 */
                val etagAsChecksum = downloadInfo.etagWithoutQuotes
                val continueDownload = !legacyKeyCache.tryMigration(etagAsChecksum, saveTo)
                continueDownload // Continue download if no migration happened
            }

        val dlInfo = keyServer.downloadKeyFile(
            locationCode = keyInfo.location,
            day = keyInfo.day,
            hour = keyInfo.hour,
            saveTo = saveTo,
            precondition = preconditionHook
        )
        Timber.tag(TAG).v("Dowwnload finished: %s -> %s", cachedKey, saveTo)

        /**
         * If for some reason the server doesn't supply the etag, let's make our own.
         * If it later gets used, it will not match.
         * Worst case, we delete it and download the same file again,
         * hopefully then with an etag in the header.
         */
        val storedETag = dlInfo.etagWithoutQuotes ?: saveTo.hashToMD5()
        keyCache.markKeyComplete(keyInfo, storedETag)

        cachedKey
    } catch (e: Exception) {
        Timber.tag(TAG).e(e, "Download failed: %s", cachedKey)
        keyCache.delete(listOf(cachedKey.info))
        null
    }

    companion object {
        private const val TAG = "KFDL:DownloadTool"
    }
}
