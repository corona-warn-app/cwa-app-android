package de.rki.coronawarnapp.diagnosiskeys.storage

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import de.rki.coronawarnapp.diagnosiskeys.server.LocationCode
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.di.AppContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
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
    @AppContext private val context: Context,
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
        val dirtyInfos = getAllCachedKeys().filter {
            it.info.isDownloadComplete && !it.path.exists()
        }
        Timber.v("HouseKeeping, deleting: %s", dirtyInfos)
        delete(dirtyInfos.map { it.info })
    }

    private fun CachedKeyInfo.toCachedKey(): CachedKey = CachedKey(
        info = this,
        path = getPathForKey(this)
    )

    fun getPathForKey(cachedKeyInfo: CachedKeyInfo): File {
        return File(storageDir, cachedKeyInfo.fileName)
    }

    suspend fun getAllCachedKeys(): List<CachedKey> {
        return allCachedKeys().first()
    }

    suspend fun allCachedKeys(): Flow<List<CachedKey>> {
        return getDao().allEntries().map { entries -> entries.map { it.toCachedKey() } }
    }

    suspend fun getEntriesForType(type: CachedKeyInfo.Type): List<CachedKey> {
        return getDao().getEntriesForType(type.typeValue).map { it.toCachedKey() }
    }

    suspend fun createCacheEntry(
        type: CachedKeyInfo.Type,
        location: LocationCode,
        dayIdentifier: LocalDate,
        hourIdentifier: LocalTime?
    ): CachedKey {
        val keyInfo = CachedKeyInfo(
            type = type,
            location = location,
            day = dayIdentifier,
            hour = hourIdentifier,
            createdAt = timeStamper.nowUTC
        )

        val targetFile = getPathForKey(keyInfo)

        try {
            getDao().insertEntry(keyInfo)
            if (targetFile.exists()) {
                Timber.w("Target path despite no collision exists, deleting: %s", targetFile)
            }
        } catch (e: SQLiteConstraintException) {
            Timber.e(e, "Insertion collision? Overwriting for %s", keyInfo)
            delete(listOf(keyInfo))

            Timber.d(e, "Retrying insertion for %s", keyInfo)
            getDao().insertEntry(keyInfo)
        }

        // This can't be null unless our cache dir is root `/`
        val targetParent = targetFile.parentFile!!
        if (!targetParent.exists()) {
            Timber.w("Parent folder doesn't exist, cache cleared? %s", targetParent)
            targetParent.mkdirs()
        }

        return CachedKey(info = keyInfo, path = targetFile)
    }

    suspend fun markKeyComplete(cachedKeyInfo: CachedKeyInfo, etag: String) {
        val update = cachedKeyInfo.toDownloadUpdate(etag)
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
        delete(getDao().allEntries().first())
    }
}
