package de.rki.coronawarnapp.covidcertificate.pdf.core

import android.content.res.AssetManager
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseIOTest
import java.io.File

internal class PdfTemplateRepositoryTest : BaseIOTest() {

    private val testDir = File(IO_TEST_BASEDIR, this::class.java.simpleName)
    @MockK lateinit var assetManager: AssetManager
    @MockK lateinit var cwaCertificate: TestCertificate

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        every { assetManager.open(any()) } answers {
            this.javaClass.classLoader!!.getResourceAsStream(arg<String>(0))
        }
    }

    @AfterEach
    fun teardown() {
        testDir.deleteRecursively()
    }

    @Test
    fun `get template from assets when cache is empty`() {
        val repository = getInstance()
        repository.getTemplate(cwaCertificate)
        verify { assetManager.open(any()) }
    }

    @Test
    fun `get template from cache when there is one`() {
        val root = File(testDir, "template").apply { mkdirs() }
        File(root, "test_certificate_template.pdf").createNewFile()
        val repository = getInstance()
        repository.getTemplate(cwaCertificate)
        verify(exactly = 0) { assetManager.open(any()) }
    }

    private fun getInstance() = PdfTemplateRepository(testDir, assetManager)
}
