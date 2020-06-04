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
import java.util.Date

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
        coEvery { keyCacheRepository.deleteOutdatedEntries() } just Runs
    }

    @Test
    fun testAsyncFetchFiles() {
        val date = Date()

        coEvery { keyCacheRepository.getDates() } returns listOf()
        coEvery { keyCacheRepository.getFilesFromEntries() } returns listOf()
        every { CachedKeyFileHolder["getDatesFromServer"]() } returns arrayListOf<String>()

        runBlocking {

            CachedKeyFileHolder.asyncFetchFiles(date)

            coVerifyOrder {
                CachedKeyFileHolder.asyncFetchFiles(date)
                keyCacheRepository.deleteOutdatedEntries()
                CachedKeyFileHolder["getDatesFromServer"]()
                CachedKeyFileHolder["getMissingDaysFromDiff"](arrayListOf<String>())
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
