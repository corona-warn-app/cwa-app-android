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
                    legacyCacheMap["md5"] = file
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
        legacyCacheMap.remove(fileMD5)
    }

    companion object {
        private val TAG = this::class.java.simpleName
    }

//
//    enum class DateEntryType {
//        DAY,
//        HOUR
//    }
//
//    suspend fun createEntry(key: String, uri: URI, type: DateEntryType) = keyCacheDao.insertEntry(
//        KeyCacheEntity().apply {
//            this.id = key
//            this.path = uri.rawPath
//            this.type = type.ordinal
//        }
//    )
//
//    suspend fun deleteOutdatedEntries(validEntries: List<String>) =
//        keyCacheDao.getAllEntries().forEach {
//            Timber.v("valid entries for cache from server: $validEntries")
//            val file = File(it.path)
//            if (!validEntries.contains(it.id) || !file.exists()) {
//                Timber.w("${it.id} will be deleted from the cache")
//                deleteFileForEntry(it)
//                keyCacheDao.deleteEntry(it)
//            }
//        }
//
//    private fun deleteFileForEntry(entry: KeyCacheEntity) =
//
//    suspend fun getDates() = keyCacheDao.getDates()
//    suspend fun getHours() = keyCacheDao.getHours()
//
//    suspend fun clearHours() {
//        getHours().forEach { deleteFileForEntry(it) }
//        keyCacheDao.clearHours()
//    }
//
//    suspend fun clear() {
//        keyCacheDao.getAllEntries().forEach { deleteFileForEntry(it) }
//        keyCacheDao.clear()
//    }
//
//    suspend fun clear(idList: List<String>) {
//        if (idList.isNotEmpty()) {
//            val entries = keyCacheDao.getAllEntries(idList)
//            entries.forEach { deleteFileForEntry(it) }
//            keyCacheDao.deleteEntries(entries)
//        }
//    }
//
//    suspend fun getFilesFromEntries() = keyCacheDao
//        .getAllEntries()
//        .map { File(it.path) }
//        .filter { it.exists() }
}
