package de.rki.coronawarnapp.statistics.source

import de.rki.coronawarnapp.statistics.Statistics
import de.rki.coronawarnapp.util.reset.Resettable
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatisticsCache @Inject constructor(
    @Statistics cacheDir: File
) : Resettable {

    private val cacheFile = File(cacheDir, "cache_raw")

    fun load(): ByteArray? = try {
        if (cacheFile.exists()) cacheFile.readBytes() else null
    } catch (e: Exception) {
        Timber.tag(TAG).e(e, "Failed to load raw statistics from cache.")
        null
    }

    fun save(data: ByteArray) {
        if (cacheFile.exists()) {
            Timber.tag(TAG).d("Overwriting with new data (size=%d)", data.size)
        }
        cacheFile.parentFile?.mkdirs()
        cacheFile.writeBytes(data)
    }

    override suspend fun reset() {
        Timber.tag(TAG).d("reset()")
        if (cacheFile.exists() && cacheFile.delete()) {
            Timber.tag(TAG).d("Cache file was deleted.")
        }
        return
    }

    companion object {
        const val TAG = "StatisticsCache"
    }
}
