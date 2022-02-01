package de.rki.coronawarnapp.ccl.configuration.storage

import android.content.Context
import android.content.res.AssetManager
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import okio.FileNotFoundException
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseIOTest
import java.io.File

class DefaultCCLConfigurationProviderTest : BaseIOTest() {

    @MockK lateinit var context: Context
    @MockK lateinit var assets: AssetManager

    private val testDir = File(IO_TEST_BASEDIR, DefaultCCLConfigurationProviderTest::class.java.simpleName)
    private val defaultCCLConfigFile = File(testDir, ASSET_DEFAULT_CCL_CONFIGURATION)

    private val instance: DefaultCCLConfigurationProvider
        get() = DefaultCCLConfigurationProvider(context = context)

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        defaultCCLConfigFile.parentFile?.mkdirs()

        every { context.assets } returns assets
        every { assets.open(ASSET_DEFAULT_CCL_CONFIGURATION) } answers { defaultCCLConfigFile.inputStream() }
    }

    @AfterEach
    fun cleanup() {
        testDir.deleteRecursively()
    }

    @Test
    fun `check asset file name`() {
        ASSET_DEFAULT_CCL_CONFIGURATION shouldBe "ccl/ccl-configuration.bin"
    }

    @Test
    fun `loads default ccl configs raw data from assets`() {
        val data = "ccl config".toByteArray()
        defaultCCLConfigFile.writeBytes(data)

        instance.loadDefaultCCLConfigurationsRawData() shouldBe data

        verify { assets.open(ASSET_DEFAULT_CCL_CONFIGURATION) }
    }

    @Test
    fun `forwards exceptions`() {
        val error = FileNotFoundException("Test error")
        every { assets.open(any()) } throws error

        shouldThrow<FileNotFoundException> { instance.loadDefaultCCLConfigurationsRawData() } shouldBe error
    }
}
