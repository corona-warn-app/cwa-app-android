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

package de.rki.coronawarnapp.util.security

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import de.rki.coronawarnapp.storage.AppDatabase
import de.rki.coronawarnapp.storage.keycache.KeyCacheEntity
import kotlinx.coroutines.runBlocking
import net.sqlcipher.database.SQLiteException
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.not
import org.junit.Assert.assertThat
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.util.UUID
import kotlin.random.Random

@RunWith(JUnit4::class)
class DBPasswordTest {

    private val appContext: Context
        get() = ApplicationProvider.getApplicationContext()

    private val db: AppDatabase
        get() = AppDatabase.getInstance(appContext)

    @Before
    fun setUp() {
        clearSharedPreferences()
        AppDatabase.reset(appContext)
    }

    @Test
    fun generatesPassphraseInCorrectLength() {
        val passphrase = SecurityHelper.getDBPassword()
        assertTrue(passphrase.size in 32..48)
    }

    @Test
    fun secondPassphraseShouldBeDifferFromFirst() {
        val passphrase1 = SecurityHelper.getDBPassword()

        clearSharedPreferences()
        val passphrase2 = SecurityHelper.getDBPassword()

        assertThat(passphrase1, not(equalTo(passphrase2)))
    }

    @Test
    fun canLoadDataFromEncryptedDatabase() {
        runBlocking {
            val id = UUID.randomUUID().toString()
            val path = UUID.randomUUID().toString()
            val type = Random.nextInt(1000)

            insertFakeEntity(id, path, type)
            val keyCacheEntity = loadFakeEntity()

            assertThat(keyCacheEntity.id, equalTo(id))
            assertThat(keyCacheEntity.path, equalTo(path))
            assertThat(keyCacheEntity.type, equalTo(type))
        }
    }

    @Test
    fun testDbInstanceIsActuallyResetWhenCalled() {
        val before = this.db
        AppDatabase.reset(appContext)
        val after = this.db

        assertTrue(before != after)
    }

    @Test(expected = SQLiteException::class)
    fun loadingDataFromDatabaseWillFailWhenPassphraseIsIncorrect() {
        runBlocking {
            val id = UUID.randomUUID().toString()
            val path = UUID.randomUUID().toString()
            val type = Random.nextInt(1000)
            insertFakeEntity(id, path, type)

            clearSharedPreferences()
            AppDatabase.resetInstance()

            val keyCacheEntity = loadFakeEntity()
            assertThat(keyCacheEntity.id, equalTo(id))
            assertThat(keyCacheEntity.path, equalTo(path))
            assertThat(keyCacheEntity.type, equalTo(type))
        }
    }

    private suspend fun insertFakeEntity(
        id: String,
        path: String,
        type: Int
    ) {
        db.dateDao().insertEntry(KeyCacheEntity().apply {
            this.id = id
            this.path = path
            this.type = type
        })
    }

    private suspend fun loadFakeEntity(): KeyCacheEntity = db.dateDao().getAllEntries().first()

    private fun clearSharedPreferences() =
        SecurityHelper.globalEncryptedSharedPreferencesInstance.edit().clear().commit()
}
