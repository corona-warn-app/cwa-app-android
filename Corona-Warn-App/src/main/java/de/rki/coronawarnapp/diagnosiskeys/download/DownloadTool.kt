package de.rki.coronawarnapp.diagnosiskeys.download

import dagger.Reusable
import de.rki.coronawarnapp.diagnosiskeys.server.DiagnosisKeyServer
import de.rki.coronawarnapp.diagnosiskeys.server.DownloadInfo
import de.rki.coronawarnapp.diagnosiskeys.storage.CachedKey
import de.rki.coronawarnapp.diagnosiskeys.storage.KeyCacheRepository
import de.rki.coronawarnapp.diagnosiskeys.storage.legacy.LegacyKeyCacheMigration
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
                val continueDownload = !legacyKeyCache.tryMigration(
                    downloadInfo.serverMD5, saveTo
                )
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

        keyCache.markKeyComplete(keyInfo, dlInfo.serverMD5 ?: dlInfo.localMD5!!)

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
