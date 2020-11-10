package de.rki.coronawarnapp.diagnosiskeys.download

import dagger.Reusable
import de.rki.coronawarnapp.appconfig.KeyDownloadConfig
import de.rki.coronawarnapp.diagnosiskeys.server.DiagnosisKeyServer
import de.rki.coronawarnapp.diagnosiskeys.server.DownloadInfo
import de.rki.coronawarnapp.diagnosiskeys.storage.CachedKey
import de.rki.coronawarnapp.diagnosiskeys.storage.KeyCacheRepository
import de.rki.coronawarnapp.diagnosiskeys.storage.legacy.LegacyKeyCacheMigration
import kotlinx.coroutines.withTimeout
import timber.log.Timber
import javax.inject.Inject

@Reusable
class KeyDownloadTool @Inject constructor(
    private val legacyKeyCache: LegacyKeyCacheMigration,
    private val keyServer: DiagnosisKeyServer,
    private val keyCache: KeyCacheRepository
) {
    suspend fun downloadKeyFile(
        cachedKey: CachedKey,
        downloadConfig: KeyDownloadConfig
    ): CachedKey = try {
        val saveTo = cachedKey.path
        val keyInfo = cachedKey.info

        val preconditionHook: suspend (DownloadInfo) -> Boolean =
            { downloadInfo ->
                /**
                 * To try legacy migration, we attempt to the etag as checksum.
                 * Removing the quotes, the etag can represent the file's MD5 checksum.
                 */
                val etagAsChecksum = downloadInfo.etag?.removePrefix("\"")?.removeSuffix("\"")
                val continueDownload = !legacyKeyCache.tryMigration(etagAsChecksum, saveTo)
                continueDownload // Continue download if no migration happened
            }

        val downloadInfo = withTimeout(downloadConfig.individualDownloadTimeout.millis) {
            keyServer.downloadKeyFile(
                locationCode = keyInfo.location,
                day = keyInfo.day,
                hour = keyInfo.hour,
                saveTo = saveTo,
                precondition = preconditionHook
            )
        }
        Timber.tag(TAG).v("Download finished: %s -> %s", cachedKey, saveTo)

        /**
         * If for some reason the server doesn't supply the etag, let's make our own.
         * If it later gets used, it will not match.
         * Worst case, we delete it and download the same file again,
         * hopefully then with an etag in the header.
         */
        val etag = requireNotNull(downloadInfo.etag) { "Server provided no ETAG!" }
        keyCache.markKeyComplete(keyInfo, etag)

        cachedKey
    } catch (e: Exception) {
        Timber.tag(TAG).e(e, "Download failed: %s", cachedKey)
        keyCache.delete(listOf(cachedKey.info))
        throw e
    }

    companion object {
        private const val TAG = "${KeyPackageSyncTool.TAG}:DownloadTool"
    }
}
