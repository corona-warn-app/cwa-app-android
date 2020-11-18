package de.rki.coronawarnapp.appconfig.download

import android.content.Context
import android.content.res.AssetManager
import de.rki.coronawarnapp.util.HashExtensions.toSHA256
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

class DefaultAppConfigSourceTest : BaseIOTest() {
    @MockK private lateinit var context: Context
    @MockK private lateinit var assetManager: AssetManager

    private val testDir = File(IO_TEST_BASEDIR, this::class.java.simpleName)
    private val configFile = File(testDir, "default_app_config_android.bin")
    private val checksumFile = File(testDir, "default_app_config_android.sha256")

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { context.assets } returns assetManager

        every { assetManager.open("default_app_config_android.bin") } answers { configFile.inputStream() }
        every { assetManager.open("default_app_config_android.sha256") } answers { checksumFile.inputStream() }

        testDir.mkdirs()
        testDir.exists() shouldBe true
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
        testDir.deleteRecursively()
    }

    private fun createInstance() = DefaultAppConfigSource(context = context)

    @Test
    fun `config loaded from asset`() {
        val testData = "The Cake Is A Lie"
        configFile.writeText(testData)
        checksumFile.writeText(testData.toSHA256())

        val instance = createInstance()
        instance.getRawDefaultConfig() shouldBe testData.toByteArray()
    }
}
