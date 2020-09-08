package de.rki.coronawarnapp.diagnosiskeys.download

import android.content.Context
import de.rki.coronawarnapp.diagnosiskeys.storage.KeyCacheRepository
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

/**
 * CachedKeyFileHolder test.
 */
class KeyFileDownloaderTest : BaseTest() {

    @MockK
    private lateinit var keyCacheRepository: KeyCacheRepository

    @MockK
    private lateinit var context: Context

//    @Before
//    fun setUp() {
//        MockKAnnotations.init(this)
//        mockkObject(CoronaWarnApplication.Companion)
//        mockkObject(KeyCacheRepository.Companion)
//        every { CoronaWarnApplication.getAppContext() } returns context
//        every { KeyCacheRepository.getDateRepository(any()) } returns keyCacheRepository
//        mockkObject(KeyFileDownloader)
//        coEvery { keyCacheRepository.deleteOutdatedEntries(any()) } just Runs
//    }
//
//    /**
//     * Test call order is correct.
//     */
//    @Test
//    fun testAsyncFetchFiles() {
//        val date = Date()
//        val countries = listOf("DE")
//        val country = "DE"
//
//        mockkObject(CWADebug)
//
//        coEvery { keyCacheRepository.getDates() } returns listOf()
//        coEvery { keyCacheRepository.getFilesFromEntries() } returns listOf()
//        every { CWADebug.isDebugBuildOrMode } returns false
//        every { KeyFileDownloader["checkForFreeSpace"]() } returns Unit
//        every { KeyFileDownloader["getDatesFromServer"](country) } returns arrayListOf<String>()
//
//        every { CoronaWarnApplication.getAppContext().cacheDir } returns File("./")
//        every { KeyFileDownloader["getCountriesFromServer"](countries) } returns countries
//
//        runBlocking {
//
//            KeyFileDownloader.asyncFetchFiles(date, countries)
//
//            coVerifyOrder {
//                KeyFileDownloader.asyncFetchFiles(date, countries)
//                KeyFileDownloader["getCountriesFromServer"](countries)
//                KeyFileDownloader["getDatesFromServer"](country)
//                KeyFileDownloader["asyncHandleFilesFetch"](
//                    listOf(
//                        CountryDataWrapper(
//                            country,
//                            listOf()
//                        )
//                    )
//                )
//                keyCacheRepository.deleteOutdatedEntries(any())
//                KeyFileDownloader["getMissingDaysFromDiff"](
//                    listOf(
//                        CountryDataWrapper(
//                            country,
//                            listOf()
//                        )
//                    )
//                )
//                keyCacheRepository.getDates()
//                keyCacheRepository.getFilesFromEntries()
//            }
//        }
//    }

//
//    @After
//    fun cleanUp() {
//        unmockkAll()
//    }

    @Test
    fun `error during country index fetch`() {
        TODO()
    }

    @Test
    fun `fetched country index is empty`() {
        TODO()
    }

    @Test
    fun `day fetch without prior data`() {
        TODO()
    }

    @Test
    fun `day fetch with existing data`() {
        TODO()
    }

    @Test
    fun `day fetch deletes stale data`() {
        TODO()
    }

    @Test
    fun `day fetch marks downloads as complete`() {
        TODO()
    }

    @Test
    fun `day fetch skips single download failures`() {
        TODO()
    }

    @Test
    fun `last3Hours fetch without prior data`() {
        TODO()
    }

    @Test
    fun `last3Hours fetch with prior data`() {
        TODO()
    }

    @Test
    fun `last3Hours fetch deletes stale data`() {
        TODO()
    }

    @Test
    fun `last3Hours fetch marks downloads as complete`() {
        TODO()
    }

    @Test
    fun `last3Hours fetch skips single download failures`() {
        TODO()
    }

    @Test
    fun `storage is checked before fetching`() {
        TODO()
    }

    @Test
    fun `fetching is aborted if not enough free storage`() {
        TODO()
    }

    @Test
    fun `not completed cache entries are overwritten`() {
        TODO()
    }

    @Test
    fun `fetch returns all currently available keyfiles`() {
        TODO()
    }
}
