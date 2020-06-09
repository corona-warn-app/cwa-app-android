package de.rki.coronawarnapp.storage.keycache

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import de.rki.coronawarnapp.storage.AppDatabase
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * KeyCacheDao test.
 */
@RunWith(AndroidJUnit4::class)
class KeyCacheDaoTest {
    private lateinit var keyCacheDao: KeyCacheDao
    private lateinit var db: AppDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, AppDatabase::class.java
        ).build()
        keyCacheDao = db.dateDao()
    }

    /**
     * Test Create / Read / Delete DB operations.
     */
    @Test
    fun testCRDOperations() {
        runBlocking {
            val dates = KeyCacheEntity().apply {
                this.id = "0"
                this.path = "0"
                this.type = 0
            }
            val hours = KeyCacheEntity().apply {
                this.id = "1"
                this.path = "1"
                this.type = 1
            }

            assertThat(keyCacheDao.getAllEntries().isEmpty()).isTrue()

            keyCacheDao.insertEntry(dates)
            keyCacheDao.insertEntry(hours)

            var all = keyCacheDao.getAllEntries()

            assertThat(all.size).isEqualTo(2)

            val selectedDates = keyCacheDao.getDates()
            assertThat(selectedDates.size).isEqualTo(1)
            assertThat(selectedDates[0].type).isEqualTo(0)
            assertThat(selectedDates[0].id).isEqualTo(dates.id)

            val selectedHours = keyCacheDao.getHours()
            assertThat(selectedHours.size).isEqualTo(1)
            assertThat(selectedHours[0].type).isEqualTo(1)
            assertThat(selectedHours[0].id).isEqualTo(hours.id)

            keyCacheDao.clearHours()

            all = keyCacheDao.getAllEntries()
            assertThat(all.size).isEqualTo(1)
            assertThat(all[0].type).isEqualTo(0)

            keyCacheDao.insertEntry(hours)

            assertThat(keyCacheDao.getAllEntries().size).isEqualTo(2)

            keyCacheDao.clear()

            assertThat(keyCacheDao.getAllEntries().isEmpty()).isTrue()
        }
    }

    @After
    fun closeDb() {
        db.close()
    }
}
