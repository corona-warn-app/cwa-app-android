package de.rki.coronawarnapp.storage

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * ExposureSummaryDao test.
 */
@RunWith(AndroidJUnit4::class)
class ExposureSummaryDaoTest {
    private lateinit var dao: ExposureSummaryDao
    private lateinit var db: AppDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, AppDatabase::class.java
        ).build()
        dao = db.exposureSummaryDao()
    }

    /**
     * Test Create / Read DB operations.
     */
    @Test
    fun testCROperations() {
        runBlocking {
            val testEntity1 = ExposureSummaryEntity().apply {
                this.daysSinceLastExposure = 1
                this.matchedKeyCount = 1
                this.maximumRiskScore = 1
                this.summationRiskScore = 1
            }

            val testEntity2 = ExposureSummaryEntity().apply {
                this.daysSinceLastExposure = 2
                this.matchedKeyCount = 2
                this.maximumRiskScore = 2
                this.summationRiskScore = 2
            }

            assertThat(dao.getExposureSummaryEntities().isEmpty()).isTrue()

            val id1 = dao.insertExposureSummaryEntity(testEntity1)
            var selectAll = dao.getExposureSummaryEntities()
            var selectLast = dao.getLatestExposureSummary()
            assertThat(dao.getExposureSummaryEntities().isEmpty()).isFalse()
            assertThat(selectAll.size).isEqualTo(1)
            assertThat(selectAll[0].id).isEqualTo(id1)
            assertThat(selectLast).isNotNull()
            assertThat(selectLast?.id).isEqualTo(id1)

            val id2 = dao.insertExposureSummaryEntity(testEntity2)
            selectAll = dao.getExposureSummaryEntities()
            selectLast = dao.getLatestExposureSummary()
            assertThat(selectAll.isEmpty()).isFalse()
            assertThat(selectAll.size).isEqualTo(2)
            assertThat(selectLast).isNotNull()
            assertThat(selectLast?.id).isEqualTo(id2)
        }
    }

    @After
    fun closeDb() {
        db.close()
    }
}
