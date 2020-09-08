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

import de.rki.coronawarnapp.diagnosiskeys.storage.KeyCacheRepository
import timber.log.Timber
import javax.inject.Inject

class LegacyKeyCacheMigration @Inject constructor(
    private val legacyDao: KeyCacheLegacyDao
) {

    suspend fun migrate(keyCacheRepository: KeyCacheRepository) {
        val items = legacyDao.getAllEntries()
        if (items.isEmpty()) {
            Timber.tag(TAG).d("Nothing to migrate.")
            return
        }

//        val (keyInfo, path) = keyCacheRepository.createCacheEntry(
//
//        )
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
