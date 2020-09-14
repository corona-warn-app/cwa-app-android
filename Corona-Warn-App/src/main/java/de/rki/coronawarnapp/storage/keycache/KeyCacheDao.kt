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

package de.rki.coronawarnapp.storage.keycache

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface KeyCacheDao {
    @Query("SELECT * FROM date WHERE type=0")
    suspend fun getDates(): List<KeyCacheEntity>

    @Query("SELECT * FROM date WHERE type=1")
    suspend fun getHours(): List<KeyCacheEntity>

    @Query("SELECT * FROM date")
    suspend fun getAllEntries(): List<KeyCacheEntity>

    @Query("SELECT * FROM date WHERE id IN (:idList)")
    suspend fun getAllEntries(idList: List<String>): List<KeyCacheEntity>

    @Query("DELETE FROM date")
    suspend fun clear()

    @Query("DELETE FROM date WHERE type=1")
    suspend fun clearHours()

    @Delete
    suspend fun deleteEntry(entity: KeyCacheEntity)

    @Delete
    suspend fun deleteEntries(entities: List<KeyCacheEntity>)

    @Insert
    suspend fun insertEntry(keyCacheEntity: KeyCacheEntity): Long
}
