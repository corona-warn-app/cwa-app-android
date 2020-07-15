package de.rki.coronawarnapp.util

import android.content.Context
import de.rki.coronawarnapp.CoronaWarnApplication
import de.rki.coronawarnapp.http.service.DistributionService
import de.rki.coronawarnapp.storage.keycache.KeyCacheRepository
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockkObject
import io.mockk.spyk
import io.mockk.unmockkAll
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.Date

/**
 * CachedKeyFileHolder test.
 */
class CachedKeyFileHolderTest {

    @MockK
    private lateinit var keyCacheRepository: KeyCacheRepository

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var distributionService: DistributionService

    private lateinit var cachedKeyFileHolder: CachedKeyFileHolder

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        mockkObject(CoronaWarnApplication.Companion)
        mockkObject(KeyCacheRepository.Companion)
        every { CoronaWarnApplication.getAppContext() } returns context
        every { KeyCacheRepository.getDateRepository(any()) } returns keyCacheRepository

        cachedKeyFileHolder = spyk(CachedKeyFileHolder(distributionService))

        coEvery { keyCacheRepository.deleteOutdatedEntries(any()) } just Runs
    }

    /**
     * Test call order is correct.
     */
    @Test
    fun testAsyncFetchFiles() {
        val date = Date()

        coEvery { keyCacheRepository.getDates() } returns listOf()
        coEvery { keyCacheRepository.getFilesFromEntries() } returns listOf()
        every { cachedKeyFileHolder["isLast3HourFetchEnabled"]() } returns false
        every { cachedKeyFileHolder["checkForFreeSpace"]() } returns Unit
        every { cachedKeyFileHolder["getDatesFromServer"]() } returns arrayListOf<String>()

        runBlocking {

            cachedKeyFileHolder.asyncFetchFiles(date)

            coVerifyOrder {
                cachedKeyFileHolder.asyncFetchFiles(date)
                cachedKeyFileHolder["getDatesFromServer"]()
                keyCacheRepository.deleteOutdatedEntries(any())
                cachedKeyFileHolder["getMissingDaysFromDiff"](arrayListOf<String>())
                keyCacheRepository.getDates()
                keyCacheRepository.getFilesFromEntries()
            }
        }
    }

    @After
    fun cleanUp() {
        unmockkAll()
    }
}
