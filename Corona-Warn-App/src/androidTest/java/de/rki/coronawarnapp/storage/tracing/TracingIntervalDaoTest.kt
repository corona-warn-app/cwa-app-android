package de.rki.coronawarnapp.storage.tracing

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
import java.util.Date

/**
 * TracingIntervalDao test.
 */
@RunWith(AndroidJUnit4::class)
class TracingIntervalDaoTest {
    private lateinit var dao: TracingIntervalDao
    private lateinit var db: AppDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, AppDatabase::class.java
        ).build()
        dao = db.tracingIntervalDao()
    }

    /**
     * Test Create / Read / Delete DB operations.
     */
    @Test
    fun testCRDOperations() {
        runBlocking {
            val oneDay = 24 * 60 * 60 * 1000
            val today = Date().time
            val testEntity = TracingIntervalEntity().apply {
                // minus 1 day
                this.from = today - oneDay
                this.to = today
            }

            assertThat(dao.getAllIntervals().isEmpty()).isTrue()

            dao.insertInterval(testEntity)

            var select = dao.getAllIntervals()
            assertThat(select.isEmpty()).isFalse()
            assertThat(select.size).isEqualTo(1)
            assertThat(select[0].from).isEqualTo(today - oneDay)
            assertThat(select[0].to).isEqualTo(today)

            dao.deleteOutdatedIntervals(today - 1)

            select = dao.getAllIntervals()
            assertThat(select.isEmpty()).isFalse()
            assertThat(select.size).isEqualTo(1)

            dao.deleteOutdatedIntervals(today + 1)
            select = dao.getAllIntervals()
            assertThat(select.isEmpty()).isTrue()
        }
    }

    @After
    fun closeDb() {
        db.close()
    }
}
