package de.rki.coronawarnapp.eventregistration.events.server.qrcodepostertemplate

import android.content.Context
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseIOTest
import java.io.ByteArrayInputStream
import java.io.File

class QrCodePosterTemplateCacheTest : BaseIOTest() {

    @MockK lateinit var context: Context
    private val testDir = File(IO_TEST_BASEDIR, this::class.simpleName!!)

    @BeforeEach
    fun setup() {
        testDir.mkdirs()

        MockKAnnotations.init(this)
        every { context.filesDir } returns testDir
        every { context.assets.open(any()) } returns ByteArrayInputStream("ASSET".toByteArray())
    }

    @AfterEach
    fun teardown() {
        testDir.deleteRecursively()
    }

    private fun createInstance() = QrCodePosterTemplateCache(context)

    @Test
    fun `should return latest cached template if available`() {
        val cacheFolder = testDir.resolve("events")
        cacheFolder.mkdirs()
        cacheFolder.resolve("template.pdf").writeBytes("CACHE".toByteArray())

        createInstance().getTemplate() shouldBe "CACHE".toByteArray()
    }

    @Test
    fun `should return default template if no cache is available`() {
        createInstance().getTemplate() shouldBe "ASSET".toByteArray()
    }

    @Test
    fun `should create cache template file`() {
        createInstance().saveTemplate("TEST".toByteArray())
        testDir.resolve("events").resolve("template.pdf").readBytes() shouldBe "TEST".toByteArray()
    }
}
