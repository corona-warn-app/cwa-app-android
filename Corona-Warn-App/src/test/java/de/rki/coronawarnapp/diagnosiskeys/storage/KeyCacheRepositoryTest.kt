package de.rki.coronawarnapp.diagnosiskeys.storage

import de.rki.coronawarnapp.diagnosiskeys.storage.legacy.KeyCacheDao
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class KeyCacheRepositoryTest : BaseTest() {

    @MockK
    private lateinit var keyCacheDao: KeyCacheDao

    private lateinit var keyCacheRepository: KeyCacheRepository

//    @Before
//    fun setUp() {
//        MockKAnnotations.init(this)
//        keyCacheRepository = KeyCacheRepository(keyCacheDao)
//
//        // DAO tests in another test
//        coEvery { keyCacheDao.getAllEntries() } returns listOf()
//        coEvery { keyCacheDao.getHours() } returns listOf()
//        coEvery { keyCacheDao.clear() } just Runs
//        coEvery { keyCacheDao.clearHours() } just Runs
//        coEvery { keyCacheDao.insertEntry(any()) } returns 0
//    }
//
//    /**
//     * Test clear order.
//     */
//    @Test
//    fun testClear() {
//        runBlocking {
//            keyCacheRepository.clear()
//
//            coVerifyOrder {
//                keyCacheDao.getAllEntries()
//
//                keyCacheDao.clear()
//            }
//        }
//
//        runBlocking {
//            keyCacheRepository.clearHours()
//
//            coVerifyOrder {
//                keyCacheDao.getHours()
//
//                keyCacheDao.clearHours()
//            }
//        }
//    }
//
//    /**
//     * Test insert order.
//     */
//    @Test
//    fun testInsert() {
//        runBlocking {
//            keyCacheRepository.createCacheEntry(
//                key = "1",
//                type = KeyCacheRepository.DateEntryType.DAY,
//                uri = URI("1")
//            )
//
//            coVerify {
//                keyCacheDao.insertEntry(any())
//            }
//        }
//    }
//
//    @After
//    fun cleanUp() {
//        unmockkAll()
//    }

    @Test
    fun `migration of old data`() {
        TODO()
    }

    @Test
    fun `migration does nothing when there is no old data`() {
        TODO()
    }

    @Test
    fun `migration consumes old data and runs only once`() {
        TODO()
    }

    @Test
    fun `migration runs before creation`() {
        TODO()
    }

    @Test
    fun `migration runs before download update`() {
        TODO()
    }

    @Test
    fun `health check runs before data creation`() {
        TODO()
    }

    @Test
    fun `health check runs before data update`() {
        TODO()
    }

    @Test
    fun `insert and retrieve`() {
        TODO()
    }

    @Test
    fun `update download state`() {
        TODO()
    }

    @Test
    fun `delete only selected entries`() {
        TODO()
    }

    @Test
    fun `clear all files`() {
        TODO()
    }

    @Test
    fun `path is based on private cache dir`() {
        TODO()
    }

    @Test
    fun `check for missing key files and change download state`() {
        TODO()
    }

}
