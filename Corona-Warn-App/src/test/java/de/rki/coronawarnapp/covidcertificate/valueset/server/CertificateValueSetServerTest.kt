package de.rki.coronawarnapp.covidcertificate.valueset.server

import dagger.Lazy
import de.rki.coronawarnapp.covidcertificate.valueset.internal.ValueSetInvalidSignatureException
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.security.SignatureValidation
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import kotlinx.coroutines.test.runBlockingTest
import okhttp3.Cache
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import java.io.File

class CertificateValueSetServerTest : BaseTest() {

    /**
     * contains both binary and signature
     */
    private val exportZip = File("src/test/resources/vaccination/valueset_default.zip")

    /**
     * binary is missing
     */
    private val invalidExportZip = File("src/test/resources/vaccination/valueset_invalid.zip")

    @MockK lateinit var cache: Cache
    @MockK lateinit var apiV1: Lazy<CertificateValueSetApiV1>
    @MockK lateinit var dispatcherProvider: DispatcherProvider
    @MockK lateinit var signatureValidation: SignatureValidation

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        exportZip.exists() shouldBe true
        invalidExportZip.exists() shouldBe true
    }

    private fun createInstance() = CertificateValueSetServer(
        cache,
        apiV1,
        dispatcherProvider,
        signatureValidation
    )

    @Test
    @Suppress("NestedBlockDepth")
    fun `valid export data`() {
        every { signatureValidation.hasValidSignature(any(), any()) } returns true
        exportZip.inputStream().use {
            createInstance().parseBody(it).apply {
                this shouldNotBe null
                ma.apply {
                    itemsCount shouldBe 1
                    getItems(0).apply {
                        key shouldBe "maKey"
                        displayText shouldBe "maDisplayText"
                    }
                }
                mp.apply {
                    itemsCount shouldBe 1
                    getItems(0).apply {
                        key shouldBe "mpKey"
                        displayText shouldBe "mpDisplayText"
                    }
                }
                vp.apply {
                    itemsCount shouldBe 1
                    getItems(0).apply {
                        key shouldBe "vpKey"
                        displayText shouldBe "vpDisplayText"
                    }
                }
            }
        }
    }

    @Test
    fun `invalid signature`() {
        every { signatureValidation.hasValidSignature(any(), any()) } returns false
        exportZip.inputStream().use {
            shouldThrow<ValueSetInvalidSignatureException> {
                createInstance().parseBody(it)
            }
        }
    }

    @Test
    fun `a file is missing`() {
        invalidExportZip.inputStream().use {
            shouldThrow<ValueSetInvalidSignatureException> {
                createInstance().parseBody(it)
            }
        }
    }

    @Test
    fun `reset invalidates cache`() = runBlockingTest {
        every { cache.evictAll() } just runs
        createInstance().reset()
        verify { cache.evictAll() }
    }
}
