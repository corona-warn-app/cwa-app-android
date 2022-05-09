package de.rki.coronawarnapp.statistics.local.source

import de.rki.coronawarnapp.statistics.Statistics
import de.rki.coronawarnapp.statistics.local.FederalStateToPackageId
import de.rki.coronawarnapp.util.reset.Resettable
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalStatisticsCache @Inject constructor(
    @Statistics cacheDir: File
) : Resettable {

    private val cacheFolder = File(cacheDir, "cache_raw_local")

    fun load(stateForCache: FederalStateToPackageId): ByteArray? = try {
        val cacheFile = File(cacheFolder, stateForCache.name)

        if (cacheFile.exists()) cacheFile.readBytes() else null
    } catch (e: Exception) {
        Timber.e(e, "Failed to load raw statistics from cache.")
        null
    }

    fun save(stateForCache: FederalStateToPackageId, data: ByteArray?) {
        val cacheFile = File(cacheFolder, stateForCache.name)

        if (data == null) {
            if (cacheFile.exists() && cacheFile.delete()) {
                Timber.d("Cache file was deleted.")
            }
            return
        }
        if (cacheFile.exists()) {
            Timber.d("Overwriting with new data (size=%d)", data.size)
        }
        cacheFile.parentFile?.mkdirs()
        cacheFile.writeBytes(data)
    }

    override suspend fun reset() {
        Timber.d("reset()")
        cacheFolder.deleteRecursively()
    }
}
