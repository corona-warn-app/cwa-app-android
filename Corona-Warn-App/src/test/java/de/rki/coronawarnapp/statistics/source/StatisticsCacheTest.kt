package de.rki.coronawarnapp.statistics.source

import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseIOTest
import java.io.File

class StatisticsCacheTest : BaseIOTest() {

    private val testDir = File(IO_TEST_BASEDIR, this::class.java.simpleName)
    private val androidCacheDir = File(testDir, "cache")
    private val statisticsCacheDir = File(androidCacheDir, "statistics")

    private val cacheFile = File(statisticsCacheDir, "cache_raw")

    private val testData = "Row, Row, Row Your Boat".encodeToByteArray()

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    @AfterEach
    fun teardown() {
        testDir.deleteRecursively()
    }

    fun createInstance() = StatisticsCache(
        cacheDir = statisticsCacheDir
    )

    @Test
    fun `empty start`() {
        createInstance().load() shouldBe null
        cacheFile.exists() shouldBe false
    }

    @Test
    fun `loading data`() {
        cacheFile.parentFile?.mkdirs()
        cacheFile.writeBytes(testData)
        createInstance().load() shouldBe testData
    }

    @Test
    fun `saving data`() {
        cacheFile.exists() shouldBe false
        createInstance().save(testData)
        cacheFile.readBytes() shouldBe testData
    }

    @Test
    fun `reset cache`() = runBlockingTest {
        cacheFile.parentFile?.mkdirs()
        cacheFile.writeBytes(testData)
        createInstance().reset()
        cacheFile.exists() shouldBe false
    }
}
