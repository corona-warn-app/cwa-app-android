package de.rki.coronawarnapp.ccl.configuration.storage

import io.kotest.matchers.file.shouldExist
import io.kotest.matchers.file.shouldNotExist
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import testhelpers.BaseIOTest
import java.io.File

class DownloadedCCLConfigurationStorageTest : BaseIOTest() {

    private val cclFile = File(IO_TEST_BASEDIR, DownloadedCCLConfigurationStorageTest::class.java.simpleName)

    private val instance: DownloadedCCLConfigurationStorage
        get() = DownloadedCCLConfigurationStorage(cclFile = cclFile)

    @AfterEach
    fun cleanup() {
        cclFile.deleteRecursively()
    }

    @Test
    fun `write and read data`() = runBlockingTest {
        val data = "Some test data".toByteArray()

        with(instance) {
            load() shouldBe null
            save(rawData = data)
            load() shouldBe data
        }

        val data2 = "Some test data which overrides existing data".toByteArray()

        with(instance) {
            load() shouldBe data
            save(rawData = data2)
            load() shouldNotBe data
            load() shouldBe data2
        }
    }

    @Test
    fun `clears ccl file completely`() = runBlockingTest {
        val cacheDir = File(cclFile, "ccl_config_http_cache")
        val cacheFile = File(cacheDir, "data")
        cacheFile.parentFile?.mkdirs()
        val data = "Some random test data".toByteArray()
        cacheFile.writeBytes(data)

        with(instance) {
            cacheDir.shouldExist()
            cacheFile.shouldExist()
            clear()
            cacheDir.shouldNotExist()
            cacheFile.shouldNotExist()
        }
    }
}
