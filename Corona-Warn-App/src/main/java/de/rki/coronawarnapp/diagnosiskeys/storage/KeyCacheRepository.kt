package de.rki.coronawarnapp.diagnosiskeys.storage

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import de.rki.coronawarnapp.diagnosiskeys.server.LocationCode
import de.rki.coronawarnapp.util.TimeStamper
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.joda.time.LocalDate
import org.joda.time.LocalTime
import timber.log.Timber
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KeyCacheRepository @Inject constructor(
    private val context: Context,
    private val databaseFactory: KeyCacheDatabase.Factory,
    private val timeStamper: TimeStamper
) {

    private val storageDir by lazy {
        File(context.cacheDir, "diagnosis_keys").apply {
            if (!exists()) {
                if (mkdirs()) {
                    Timber.d("KeyCache directory created: %s", this)
                } else {
                    throw IOException("KeyCache directory creation failed: $this")
                }
            }
        }
    }

    private val database by lazy { databaseFactory.create() }

    private var isInitDone = false
    private val initMutex = Mutex()

    private suspend fun getDao(): KeyCacheDatabase.CachedKeyFileDao {
        val dao = database.cachedKeyFiles()

        if (!isInitDone) {
            initMutex.withLock {
                if (isInitDone) return@withLock
                isInitDone = true

                doHouseKeeping()
            }
        }

        return dao
    }

    private suspend fun doHouseKeeping() {
        val dirtyInfos = getDao().getAllEntries().filter {
            it.isDownloadComplete && !getPathForKey(it).exists()
        }
        Timber.v("HouseKeeping, deleting: %s", dirtyInfos)
        delete(dirtyInfos)
    }

    fun getPathForKey(cachedKeyInfo: CachedKeyInfo): File {
        return File(storageDir, cachedKeyInfo.fileName)
    }

    suspend fun getAllCachedKeys(): List<Pair<CachedKeyInfo, File>> {
        return getDao().getAllEntries().map { it to getPathForKey(it) }
    }

    suspend fun getEntriesForType(type: CachedKeyInfo.Type): List<Pair<CachedKeyInfo, File>> {
        return getDao().getEntriesForType(type.typeValue).map { it to getPathForKey(it) }
    }

    suspend fun createCacheEntry(
        type: CachedKeyInfo.Type,
        location: LocationCode,
        dayIdentifier: LocalDate,
        hourIdentifier: LocalTime?
    ): Pair<CachedKeyInfo, File> {
        val newKeyFile = CachedKeyInfo(
            type = type,
            location = location,
            day = dayIdentifier,
            hour = hourIdentifier,
            createdAt = timeStamper.nowUTC
        )

        val targetFile = getPathForKey(newKeyFile)

        try {
            getDao().insertEntry(newKeyFile)
            if (targetFile.exists()) {
                Timber.w("Target path despite no collision exists, deleting: %s", targetFile)
            }
        } catch (e: SQLiteConstraintException) {
            Timber.e(e, "Insertion collision? Overwriting for %s", newKeyFile)
            delete(listOf(newKeyFile))

            Timber.d(e, "Retrying insertion for %s", newKeyFile)
            getDao().insertEntry(newKeyFile)
        }

        // This can't be null unless our cache dir is root `/`
        val targetParent = targetFile.parentFile!!
        if (!targetParent.exists()) {
            Timber.w("Parent folder doesn't exist, cache cleared? %s", targetParent)
            targetParent.mkdirs()
        }

        return newKeyFile to targetFile
    }

    suspend fun markKeyComplete(cachedKeyInfo: CachedKeyInfo, checksumMD5: String) {
        val update = cachedKeyInfo.toDownloadUpdate(checksumMD5)
        getDao().updateDownloadState(update)
    }

    suspend fun delete(keyInfos: Collection<CachedKeyInfo>) {
        Timber.d("delete(keyFiles=%s)", keyInfos)
        keyInfos.forEach { key ->
            getDao().deleteEntry(key)
            Timber.v("Deleted %s", key)
            val path = getPathForKey(key)
            if (path.delete()) Timber.v("Deleted cache key file at %s", path)
        }
    }

    suspend fun clear() {
        Timber.i("clear()")
        delete(getDao().getAllEntries())
    }
}
