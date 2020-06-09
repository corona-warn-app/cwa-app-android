package de.rki.coronawarnapp.storage.keycache

import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.unmockkAll
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.net.URI

/**
 * KeyCacheRepository test.
 */
class KeyCacheRepositoryTest {

    @MockK
    private lateinit var keyCacheDao: KeyCacheDao

    private lateinit var keyCacheRepository: KeyCacheRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        keyCacheRepository = KeyCacheRepository(keyCacheDao)

        // DAO tests in another test
        coEvery { keyCacheDao.getAllEntries() } returns listOf()
        coEvery { keyCacheDao.getHours() } returns listOf()
        coEvery { keyCacheDao.clear() } just Runs
        coEvery { keyCacheDao.clearHours() } just Runs
        coEvery { keyCacheDao.insertEntry(any()) } returns 0
    }

    /**
     * Test clear order.
     */
    @Test
    fun testClear() {
        runBlocking {
            keyCacheRepository.clear()

            coVerifyOrder {
                keyCacheDao.getAllEntries()

                keyCacheDao.clear()
            }
        }

        runBlocking {
            keyCacheRepository.clearHours()

            coVerifyOrder {
                keyCacheDao.getHours()

                keyCacheDao.clearHours()
            }
        }
    }

    /**
     * Test insert order.
     */
    @Test
    fun testInsert() {
        runBlocking {
            keyCacheRepository.createEntry(
                key = "1",
                type = KeyCacheRepository.DateEntryType.DAY,
                uri = URI("1")
            )

            coVerify {
                keyCacheDao.insertEntry(any())
            }
        }
    }

    @After
    fun cleanUp() {
        unmockkAll()
    }
}
