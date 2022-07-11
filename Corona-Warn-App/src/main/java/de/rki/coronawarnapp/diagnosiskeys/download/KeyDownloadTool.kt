package de.rki.coronawarnapp.diagnosiskeys.download

import dagger.Reusable
import de.rki.coronawarnapp.appconfig.KeyDownloadConfig
import de.rki.coronawarnapp.diagnosiskeys.server.DiagnosisKeyServer
import de.rki.coronawarnapp.diagnosiskeys.storage.CachedKey
import de.rki.coronawarnapp.diagnosiskeys.storage.KeyCacheRepository
import kotlinx.coroutines.withTimeout
import timber.log.Timber
import javax.inject.Inject

@Reusable
class KeyDownloadTool @Inject constructor(
    private val keyServer: DiagnosisKeyServer,
    private val keyCache: KeyCacheRepository
) {
    suspend fun downloadKeyFile(
        cachedKey: CachedKey,
        downloadConfig: KeyDownloadConfig
    ): CachedKey = try {
        val saveTo = cachedKey.path
        val keyInfo = cachedKey.info

        val downloadInfo = withTimeout(downloadConfig.individualDownloadTimeout.toMillis()) {
            keyServer.downloadKeyFile(
                locationCode = keyInfo.location,
                day = keyInfo.day,
                hour = keyInfo.hour,
                saveTo = saveTo
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
        keyCache.deleteInfoAndFile(listOf(cachedKey.info))
        throw e
    }

    companion object {
        private const val TAG = "${KeyPackageSyncTool.TAG}:DownloadTool"
    }
}
