package de.rki.coronawarnapp.diagnosiskeys.storage.legacy

import android.content.Context
import dagger.Lazy
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
    private val legacyDao: Lazy<KeyCacheLegacyDao>,
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

        try {
            legacyDao.get().clear()
        } catch (e: Exception) {
            // Not good, but not a problem, we don't need the actual entities for migration.
            Timber.tag(TAG).w(e, "Failed to clear legacy key cache from db.")
        }

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

    suspend fun tryMigration(fileMD5: String?, targetPath: File): Boolean = workMutex.withLock {
        if (fileMD5 == null) return false
        tryInit()

        val legacyFile = legacyCacheMap[fileMD5] ?: return false
        Timber.tag(TAG).i("Migrating legacy file for %s to %s", fileMD5, targetPath)

        return try {
            legacyFile.inputStream().use { from ->
                targetPath.outputStream().use { to ->
                    from.copyTo(to, DEFAULT_BUFFER_SIZE)
                }
            }
            true
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Failed to migrate %s", legacyFile)
            false
        } finally {
            try {
                val removedFile = legacyCacheMap.remove(fileMD5)
                if (removedFile?.delete() == true) Timber.tag(TAG).d("Deleted %s", removedFile)
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Failed to delete %s", legacyFile)
            }
        }
    }

    companion object {
        private val TAG = this::class.java.simpleName
    }
}
