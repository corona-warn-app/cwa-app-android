package de.rki.coronawarnapp.storage

import com.google.android.gms.nearby.exposurenotification.ExposureSummary
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * ExposureSummaryRepository test.
 */
class ExposureSummaryRepositoryTest {

    @MockK
    private lateinit var dao: ExposureSummaryDao
    private lateinit var repository: ExposureSummaryRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        repository = ExposureSummaryRepository(dao)

        coEvery { dao.getExposureSummaryEntities() } returns listOf()
        coEvery { dao.getLatestExposureSummary() } returns null
        coEvery { dao.insertExposureSummaryEntity(any()) } returns 0
    }

    /**
     * Test DAO is called.
     */
    @Test
    fun testGet() {
        runBlocking {
            repository.getExposureSummaryEntities()

            coVerify {
                dao.getExposureSummaryEntities()
            }
        }
    }

    /**
     * Test DAO is called.
     */
    @Test
    fun testGetLatest() {
        runBlocking {
            repository.getLatestExposureSummary()

            coVerify {
                dao.getLatestExposureSummary()
            }
        }
    }

    /**
     * Test DAO is called.
     */
    @Test
    fun testInsert() {
        val es = mockk<ExposureSummary>()
        every { es.attenuationDurationsInMinutes } returns intArrayOf(0)
        every { es.daysSinceLastExposure } returns 1
        every { es.matchedKeyCount } returns 1
        every { es.maximumRiskScore } returns 0
        every { es.summationRiskScore } returns 0

        runBlocking {
            repository.insertExposureSummaryEntity(es)

            coVerify {
                dao.insertExposureSummaryEntity(any())
            }
        }
    }

    @After
    fun cleanUp() {
        unmockkAll()
    }
}
