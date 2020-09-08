package de.rki.coronawarnapp.diagnosiskeys.storage.legacy

import android.content.Context
import de.rki.coronawarnapp.util.HashExtensions.hashToMD5
import de.rki.coronawarnapp.util.TimeStamper
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.joda.time.Duration
import org.joda.time.Instant
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class LegacyKeyCacheMigration @Inject constructor(
    private val context: Context,
    private val legacyDao: KeyCacheLegacyDao,
    private val timeStamper: TimeStamper
) {

    private val cacheDir by lazy {
        File(context.cacheDir, "key-export")
    }

    private val workMutex = Mutex()
    private var isInit = false
    private val legacyCacheMap = mutableMapOf<String, File>()

    private suspend fun tryInit() {
        if (isInit) return
        isInit = true

        legacyDao.clear()

        try {
            cacheDir.listFiles()?.forEach { file ->
                val isExpired = Duration(
                    Instant.ofEpochMilli(file.lastModified()),
                    timeStamper.nowUTC
                ).standardDays > 15

                if (isExpired) {
                    Timber.tag(TAG).d("Deleting expired file: %s", file)
                    file.delete()
                } else {
                    val md5 = file.hashToMD5()
                    Timber.tag(TAG).v("MD5 %s for %s", md5, file)
                    legacyCacheMap[md5] = file
                }
            }
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Reading legacy cached failed. Clearing.")
            cacheDir.deleteRecursively()
        }
    }

    suspend fun getLegacyFile(fileMD5: String): File? = workMutex.withLock {
        tryInit()
        legacyCacheMap[fileMD5]
    }

    suspend fun delete(fileMD5: String) = workMutex.withLock {
        tryInit()
        Timber.tag(TAG).v("delete(md5=%s)", fileMD5)
        val removedFile = legacyCacheMap.remove(fileMD5)
        if (removedFile?.delete() == true) Timber.tag(TAG).d("Deleted %s", removedFile)
    }

    companion object {
        private val TAG = this::class.java.simpleName
    }
}
