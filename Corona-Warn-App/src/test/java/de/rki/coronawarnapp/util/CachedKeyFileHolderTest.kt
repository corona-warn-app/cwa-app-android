package de.rki.coronawarnapp.util

import android.content.Context
import de.rki.coronawarnapp.CoronaWarnApplication
import de.rki.coronawarnapp.storage.keycache.KeyCacheEntity
import de.rki.coronawarnapp.storage.keycache.KeyCacheRepository
import io.kotest.matchers.shouldBe
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

        mockkObject(CWADebug)

        coEvery { keyCacheRepository.getDates() } returns listOf()
        coEvery { keyCacheRepository.getFilesFromEntries() } returns listOf()
        every { CWADebug.isDebugBuildOrMode } returns false
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
                CachedKeyFileHolder["asyncHandleFilesFetch"](
                    listOf(
                        CountryDataWrapper(
                            country,
                            listOf()
                        )
                    )
                )
                keyCacheRepository.deleteOutdatedEntries(any())
                CachedKeyFileHolder["getMissingDaysFromDiff"](
                    listOf(
                        CountryDataWrapper(
                            country,
                            listOf()
                        )
                    )
                )
                keyCacheRepository.getDates()
                keyCacheRepository.getFilesFromEntries()
            }
        }
    }

    @Test
    fun testGetMissingDaysFromDiff() {
        val c1 = KeyCacheEntity()
        c1.id = "10008bf0-8890-356d-a4a4-dc375553160a"
        c1.path =
            "/data/user/0/de.rki.coronawarnapp.dev/cache/key-export/10008bf0-8890-356d-a4a4-dc375553160a.zip"
        c1.type = KeyCacheRepository.DateEntryType.DAY.ordinal

        val c2 = KeyCacheEntity()
        c2.id = "a8cc7b31-843e-3924-b918-023c386aec69"
        c2.path =
            "/data/user/0/de.rki.coronawarnapp.dev/cache/key-export/a8cc7b31-843e-3924-b918-023c386aec69.zip"
        c2.type = KeyCacheRepository.DateEntryType.DAY.ordinal

        val cacheEntries: Collection<KeyCacheEntity> = listOf(c1, c2)

        val countryDataWrapper =
            CountryDataWrapper("DE", listOf("2020-08-29", "2020-08-26", "2020-08-28"))

        val result = countryDataWrapper.getMissingDates(cacheEntries)

        result.size shouldBe 1
        result.elementAt(0) shouldBe "2020-08-28"
    }

    @After
    fun cleanUp() {
        unmockkAll()
    }
}
