package de.rki.coronawarnapp.diagnosiskeys.storage

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import de.rki.coronawarnapp.diagnosiskeys.server.LocationCode
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.reset.Resettable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KeyCacheRepository @Inject constructor(
    @AppContext private val context: Context,
    private val databaseFactory: KeyCacheDatabase.Factory,
    private val timeStamper: TimeStamper
) : Resettable {

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
        val cachedKeys = getAllCachedKeys()
        // delete meta data and files that have been marked as complete, but do not exists nor have been checked
        val dirtyInfos = cachedKeys.filter {
            it.info.isDownloadComplete && !it.path.exists() && !it.info.checkedForExposures
        }
        Timber.v("House keeping, deleting dirty entries: %s", dirtyInfos)
        deleteInfoAndFile(dirtyInfos.map { it.info })

        // delete files that have been checked (keep meta data)
        cachedKeys.filter {
            it.info.isDownloadComplete && it.info.checkedForExposures
        }.forEach {
            Timber.v("House keeping, deleting checked file: %s", it)
            deleteFile(it.info)
        }
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
            deleteInfoAndFile(listOf(keyInfo))

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

    suspend fun markKeyChecked(cachedKeyInfos: List<CachedKeyInfo>) {
        with(getDao()) {
            cachedKeyInfos.forEach {
                setChecked(it.id)
            }
        }
    }

    suspend fun deleteInfoAndFile(keyInfos: Collection<CachedKeyInfo>) {
        Timber.d("delete(keyFiles=%s)", keyInfos)
        keyInfos.forEach { key ->
            getDao().deleteEntry(key)
            Timber.v("Deleted %s", key)
            deleteFile(key)
        }
    }

    private fun deleteFile(keyInfo: CachedKeyInfo) {
        val path = getPathForKey(keyInfo)
        if (path.delete()) Timber.v("Deleted cache key file at %s", path)
    }

    override suspend fun reset() {
        Timber.i("reset()")
        deleteInfoAndFile(getDao().allEntries().first())
    }
}
