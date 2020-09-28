package de.rki.coronawarnapp.appconfig

import android.content.Context
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

class AppConfigStorageTest : BaseIOTest() {

    @MockK private lateinit var context: Context

    private val testDir = File(IO_TEST_BASEDIR, this::class.java.simpleName)
    private val privateFiles = File(testDir, "files")
    private val storageDir = File(privateFiles, "appconfig_storage")
    private val configPath = File(storageDir, "appconfig")
    private val testByteArray = "The Cake Is A Lie".toByteArray()

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        every { context.filesDir } returns privateFiles
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
        testDir.deleteRecursively()
    }

    private fun createStorage() = AppConfigStorage(context)

    @Test
    fun `simple read and write config`() {
        configPath.exists() shouldBe false
        val storage = createStorage()
        configPath.exists() shouldBe false

        storage.appConfigRaw = testByteArray

        configPath.exists() shouldBe true
        configPath.readBytes() shouldBe testByteArray

        storage.appConfigRaw shouldBe testByteArray
    }

    @Test
    fun `nulling and overwriting`() {
        val storage = createStorage()
        configPath.exists() shouldBe false

        storage.appConfigRaw shouldBe null
        storage.appConfigRaw = null
        configPath.exists() shouldBe false

        storage.appConfigRaw shouldBe null
        storage.appConfigRaw = testByteArray
        storage.appConfigRaw shouldBe testByteArray
        configPath.exists() shouldBe true
        configPath.readBytes() shouldBe testByteArray

        storage.appConfigRaw = null
        storage.appConfigRaw shouldBe null
        configPath.exists() shouldBe false
    }
}
