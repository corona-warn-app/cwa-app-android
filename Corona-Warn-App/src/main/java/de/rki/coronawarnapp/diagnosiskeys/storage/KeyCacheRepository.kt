/******************************************************************************
 * Corona-Warn-App                                                            *
 *                                                                            *
 * SAP SE and all other contributors /                                        *
 * copyright owners license this file to you under the Apache                 *
 * License, Version 2.0 (the "License"); you may not use this                 *
 * file except in compliance with the License.                                *
 * You may obtain a copy of the License at                                    *
 *                                                                            *
 * http://www.apache.org/licenses/LICENSE-2.0                                 *
 *                                                                            *
 * Unless required by applicable law or agreed to in writing,                 *
 * software distributed under the License is distributed on an                *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY                     *
 * KIND, either express or implied.  See the License for the                  *
 * specific language governing permissions and limitations                    *
 * under the License.                                                         *
 ******************************************************************************/

package de.rki.coronawarnapp.diagnosiskeys.storage

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import de.rki.coronawarnapp.diagnosiskeys.server.LocationCode
import de.rki.coronawarnapp.util.TimeStamper
import org.joda.time.LocalDate
import org.joda.time.LocalTime
import timber.log.Timber
import java.io.File
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
                    Timber.w("KeyCache directory creation failed: %s", this)
                }
            }
        }
    }

    private val database by lazy { databaseFactory.create() }

    private val cacheKeyFilesDao: KeyCacheDatabase.CachedKeyFileDao
        get() = database.cachedKeyFiles()

    private fun tryMigration() {
        // TODO() from key-export
    }

    private fun checkCacheHealth() {
//        TODO()
    }

    fun getPathForKey(cachedKeyFile: CachedKeyFile): File {
        return File(storageDir, cachedKeyFile.fileName)
    }

    suspend fun getAllCachedKeys(): List<CachedKeyFile> {
        return cacheKeyFilesDao.getAllEntries()
    }

    suspend fun getEntriesForType(type: CachedKeyFile.Type): List<CachedKeyFile> {
        return cacheKeyFilesDao.getEntriesForType(type.typeValue)
    }

    suspend fun createCacheEntry(
        type: CachedKeyFile.Type,
        location: LocationCode,
        dayIdentifier: LocalDate,
        hourIdentifier: LocalTime?
    ): Pair<CachedKeyFile, File> {
        val newKeyFile = CachedKeyFile(
            type = type,
            location = location,
            day = dayIdentifier,
            hour = hourIdentifier,
            createdAt = timeStamper.nowUTC
        )

        val targetFile = getPathForKey(newKeyFile)

        try {
            cacheKeyFilesDao.insertEntry(newKeyFile)
            if (targetFile.exists()) {
                Timber.w("Target path despire no collision exists, deleting: %s", targetFile)
            }
        } catch (e: SQLiteConstraintException) {
            Timber.e(e, "Insertion collision? Overwriting for %s", newKeyFile)
            delete(listOf(newKeyFile))

            Timber.d(e, "Retrying insertion for %s", newKeyFile)
            cacheKeyFilesDao.insertEntry(newKeyFile)
        }

        // This can't be null unless our cache dir is root `/`
        val targetParent = targetFile.parentFile!!
        if (!targetParent.exists()) {
            Timber.w("Parent folder doesn't exist, cache cleared? %s", targetParent)
            targetParent.mkdirs()
        }

        return newKeyFile to targetFile
    }

    suspend fun markKeyComplete(cachedKeyFile: CachedKeyFile, checksumMD5: String) {
        val update = cachedKeyFile.toDownloadCompleted(checksumMD5)
        cacheKeyFilesDao.updateDownloadState(update)
    }

    suspend fun delete(keyFiles: Collection<CachedKeyFile>) {
        Timber.d("delete(keyFiles=%s)", keyFiles)
        keyFiles.forEach { key ->
            cacheKeyFilesDao.deleteEntry(key)
            Timber.v("Deleted %s", key)
            val path = getPathForKey(key)
            if (path.delete()) Timber.v("Deleted cache key file at %s", path)
        }
    }

    suspend fun clear() {
        Timber.i("clear()")
        delete(getAllCachedKeys())
    }
}
