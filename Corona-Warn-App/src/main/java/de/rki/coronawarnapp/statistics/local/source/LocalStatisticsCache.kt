package de.rki.coronawarnapp.statistics.local.source

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.statistics.Statistics
import de.rki.coronawarnapp.statistics.local.FederalStateToPackageId
import timber.log.Timber
import java.io.File

class LocalStatisticsCache @AssistedInject constructor(
    @Statistics cacheDir: File,
    @Assisted private val stateForCache: FederalStateToPackageId,
) {

    private val cacheFolder = File(cacheDir, "cache_raw_local")
    private val cacheFile = File(cacheFolder, stateForCache.toString())

    fun load(): ByteArray? = try {
        if (cacheFile.exists()) cacheFile.readBytes() else null
    } catch (e: Exception) {
        Timber.e(e, "Failed to load raw statistics from cache.")
        null
    }

    fun save(data: ByteArray?) {
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

    @AssistedFactory
    interface LocalStatisticsCacheFactory {
        fun create(stateForCache: FederalStateToPackageId): LocalStatisticsCache
    }
}
