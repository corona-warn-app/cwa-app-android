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

class DefaultCclConfigurationProviderTest : BaseIOTest() {

    @MockK lateinit var context: Context
    @MockK lateinit var assets: AssetManager

    private val testDir = File(IO_TEST_BASEDIR, DefaultCclConfigurationProviderTest::class.java.simpleName)
    private val defaultCclConfigFile = File(testDir, ASSET_DEFAULT_CCL_CONFIGURATION)

    private val instance: DefaultCclConfigurationProvider
        get() = DefaultCclConfigurationProvider(context = context)

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        defaultCclConfigFile.parentFile?.mkdirs()

        every { context.assets } returns assets
        every { assets.open(ASSET_DEFAULT_CCL_CONFIGURATION) } answers { defaultCclConfigFile.inputStream() }
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
        defaultCclConfigFile.writeBytes(data)

        instance.loadDefaultCclConfigurationsRawData() shouldBe data

        verify { assets.open(ASSET_DEFAULT_CCL_CONFIGURATION) }
    }

    @Test
    fun `forwards exceptions`() {
        val error = FileNotFoundException("Test error")
        every { assets.open(any()) } throws error

        shouldThrow<FileNotFoundException> { instance.loadDefaultCclConfigurationsRawData() } shouldBe error
    }
}
