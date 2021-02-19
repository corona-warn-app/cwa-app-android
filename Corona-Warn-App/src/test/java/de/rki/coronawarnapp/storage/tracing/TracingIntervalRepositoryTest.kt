package de.rki.coronawarnapp.storage.tracing

import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.just
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import testhelpers.BaseTest
import java.util.Date

/**
 * TracingIntervalRepository test.
 */
class TracingIntervalRepositoryTest : BaseTest() {

    @MockK
    private lateinit var dao: TracingIntervalDao
    private lateinit var repository: TracingIntervalRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        repository = TracingIntervalRepository(dao)

        coEvery { dao.deleteOutdatedIntervals(any()) } just Runs
        coEvery { dao.insertInterval(any()) } just Runs
        coEvery { dao.getAllIntervals() } returns listOf()
    }

    @After
    fun cleanUp() {
        clearAllMocks()
    }

    /**
     * Test DAO is called.
     */
    @Test
    fun testCreate() {
        val today = Date().time

        runBlocking {
            repository.createInterval(today - 1, today)

            coVerify {
                dao.insertInterval(any())
            }
        }
    }

    /**
     * Test DAO is called.
     */
    @Test
    fun testGet() {
        runBlocking {
            repository.getIntervals()

            coVerify {
                dao.deleteOutdatedIntervals(any())
                dao.getAllIntervals()
            }
        }
    }
}
