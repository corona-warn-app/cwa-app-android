package de.rki.coronawarnapp.appconfig

import android.content.Context
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseIOTest
import java.io.File

class AppConfigModuleTest : BaseIOTest() {
    @MockK
    private lateinit var context: Context

    private val testDir = File(IO_TEST_BASEDIR, this::class.java.simpleName)
    private val cacheFiles = File(testDir, "cache")
    private val privateFiles = File(testDir, "files")
    private val legacyHttpCacheDir = File(cacheFiles, "http_app-config")
    private val newHttpCacheDir = File(privateFiles, "appconfig_httpstore")

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        every { context.filesDir } returns privateFiles
        every { context.cacheDir } returns cacheFiles

        testDir.mkdirs()
        testDir.exists() shouldBe true
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
        testDir.deleteRecursively()
    }

    private fun createModule() = AppConfigModule()

    @Test
    fun `sideeffect free instantiation`() {
        shouldNotThrowAny {
            createModule()
        }
    }

    @Test
    fun `AppConfig path migration`() {
        legacyHttpCacheDir.mkdirs()
        legacyHttpCacheDir.exists() shouldBe true

        val oldFile1 = File(legacyHttpCacheDir, "file1").apply {
            createNewFile() shouldBe true
            writeText("1")
        }
        val oldFile2 = File(legacyHttpCacheDir, "file2").apply {
            createNewFile() shouldBe true
            writeText("2")
        }
        legacyHttpCacheDir.listFiles()!! shouldContainAll listOf(oldFile1, oldFile2)
        newHttpCacheDir.exists() shouldBe false

        createModule().getConfigCachePath(context)

        legacyHttpCacheDir.exists() shouldBe false
        newHttpCacheDir.exists() shouldBe true

        newHttpCacheDir.listFiles()!!.size shouldBe 2
        File(newHttpCacheDir, "file1").apply {
            exists() shouldBe true
            readText() shouldBe "1"
        }
        File(newHttpCacheDir, "file2").apply {
            exists() shouldBe true
            readText() shouldBe "2"
        }
    }
}
