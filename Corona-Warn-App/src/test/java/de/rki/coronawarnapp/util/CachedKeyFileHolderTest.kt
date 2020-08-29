package de.rki.coronawarnapp.util

import android.content.Context
import de.rki.coronawarnapp.CoronaWarnApplication
import de.rki.coronawarnapp.storage.keycache.KeyCacheRepository
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File
import java.util.Date

/**
 * CachedKeyFileHolder test.
 */
class CachedKeyFileHolderTest {

    @MockK
    private lateinit var keyCacheRepository: KeyCacheRepository

    @MockK
    private lateinit var context: Context

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        mockkObject(CoronaWarnApplication.Companion)
        mockkObject(KeyCacheRepository.Companion)
        every { CoronaWarnApplication.getAppContext() } returns context
        every { KeyCacheRepository.getDateRepository(any()) } returns keyCacheRepository
        mockkObject(CachedKeyFileHolder)
        coEvery { keyCacheRepository.deleteOutdatedEntries(any()) } just Runs
    }

    /**
     * Test call order is correct.
     */
    @Test
    fun testAsyncFetchFiles() {
        val date = Date()
        val countries = listOf("DE")
        val country = "DE"

        coEvery { keyCacheRepository.getDates() } returns listOf()
        coEvery { keyCacheRepository.getFilesFromEntries() } returns listOf()
        every { CachedKeyFileHolder["isLast3HourFetchEnabled"]() } returns false
        every { CachedKeyFileHolder["checkForFreeSpace"]() } returns Unit
        every { CachedKeyFileHolder["getDatesFromServer"](country) } returns arrayListOf<String>()

        every { CoronaWarnApplication.getAppContext().cacheDir } returns File("./")
        every { CachedKeyFileHolder["getCountriesFromServer"](countries) } returns countries

        runBlocking {

            CachedKeyFileHolder.asyncFetchFiles(date, countries)

            coVerifyOrder {
                CachedKeyFileHolder.asyncFetchFiles(date, countries)
                CachedKeyFileHolder["getCountriesFromServer"](countries)
                CachedKeyFileHolder["getDatesFromServer"](country)
                keyCacheRepository.deleteOutdatedEntries(any())
                CachedKeyFileHolder["getMissingDaysFromDiff"](listOf(Pair(country, listOf<String>() )))
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
