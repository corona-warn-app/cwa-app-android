package de.rki.coronawarnapp.diagnosiskeys.storage.legacy

import de.rki.coronawarnapp.diagnosiskeys.storage.KeyCacheRepository
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseIOTest
import java.io.File

class LegacyKeyCacheMigrationTest : BaseIOTest() {

    @MockK
    lateinit var keyCacheRepository: KeyCacheRepository

    @MockK
    lateinit var legacyDao: KeyCacheLegacyDao

    private val testDir = File(IO_TEST_BASEDIR, this::class.simpleName!!)

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        testDir.mkdirs()
        testDir.exists() shouldBe true

        coEvery { legacyDao.getAllEntries() } returns emptyList()
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
        testDir.deleteRecursively()
    }

    private fun createTool() = LegacyKeyCacheMigration(
        legacyDao = legacyDao
    )

    @Test
    fun `no legacy files to migrate`() {
        val tool = createTool()
        runBlocking {
            tool.migrate(keyCacheRepository)
        }

        coVerify(exactly = 0) {
            keyCacheRepository.createCacheEntry(
                type = any(),
                location = any(),
                dayIdentifier = any(),
                hourIdentifier = any()
            )
        }
    }

    @Test
    fun `migrate two legacy files`() {
        val tool = createTool()
        val legacyItem1 = KeyCacheLegacyEntity(

        )
        val legacyItem2 = KeyCacheLegacyEntity(

        )

        coEvery { legacyDao.getAllEntries() } returns listOf(legacyItem1, legacyItem2)

        runBlocking {
            tool.migrate(keyCacheRepository)
        }

        coVerify(exactly = 0) {
            keyCacheRepository.createCacheEntry(
                type = any(),
                location = any(),
                dayIdentifier = any(),
                hourIdentifier = any()
            )
        }
    }
}
