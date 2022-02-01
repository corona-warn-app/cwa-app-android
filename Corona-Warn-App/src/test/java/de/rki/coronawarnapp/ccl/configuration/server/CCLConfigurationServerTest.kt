package de.rki.coronawarnapp.ccl.configuration.server

import de.rki.coronawarnapp.util.ZipHelper.readIntoMap
import de.rki.coronawarnapp.util.ZipHelper.unzip
import de.rki.coronawarnapp.util.security.SignatureValidation
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runBlockingTest
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.ByteString.Companion.decodeBase64
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import retrofit2.Response
import testhelpers.BaseTest
import testhelpers.TestDispatcherProvider

@Suppress("MaxLineLength")
class CCLConfigurationServerTest : BaseTest() {

    @MockK lateinit var cclConfigurationApiV1: CCLConfigurationApiV1
    @MockK lateinit var signatureValidation: SignatureValidation

    private val cclConfigDataBase64 =
        "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"

    private val instance: CCLConfigurationServer
        get() = CCLConfigurationServer(
            cclConfigurationApiLazy = { cclConfigurationApiV1 },
            dispatcherProvider = TestDispatcherProvider(),
            signatureValidation = signatureValidation
        )

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { signatureValidation.hasValidSignature(any(), any()) } returns true
    }

    @Test
    fun `happy download`() = runBlockingTest {
        val data = cclConfigDataBase64.decodeBase64()
        val response = Response.success(data?.toResponseBody())
        val fileMap = data!!.toByteArray().inputStream().unzip().readIntoMap()
        val exportBinary = fileMap[CCLConfigurationServer.EXPORT_BINARY_FILE_NAME]!!

        coEvery { cclConfigurationApiV1.getCCLConfiguration() } returns response

        instance.getCCLConfiguration() shouldBe exportBinary

        coVerify {
            signatureValidation.hasValidSignature(any(), any())
        }
    }

    @Test
    fun `returns null on error response`() = runBlockingTest {
        val response = Response.error<ResponseBody>(404, "".toResponseBody())

        coEvery { cclConfigurationApiV1.getCCLConfiguration() } returns response

        instance.getCCLConfiguration() shouldBe null
    }

    @Test
    fun `returns null on faulty data`() = runBlockingTest {
        val response = Response.success("faulty data".toResponseBody())

        coEvery { cclConfigurationApiV1.getCCLConfiguration() } returns response

        instance.getCCLConfiguration() shouldBe null
    }

    @Test
    fun `returns null on cached response`() = runBlockingTest {
        val data = cclConfigDataBase64.decodeBase64()
        val response: okhttp3.Response = mockk {
            every { body } returns data?.toResponseBody()
        }
        val cachedResponse: okhttp3.Response = mockk {
            every { isSuccessful } returns true
            every { cacheResponse } returns response
            every { networkResponse } returns null
        }

        coEvery { cclConfigurationApiV1.getCCLConfiguration() } returns Response.success(
            data?.toResponseBody(),
            cachedResponse
        )

        instance.getCCLConfiguration() shouldBe null
    }

    @Test
    fun `returns null on not modified response`() = runBlockingTest {
        val data = cclConfigDataBase64.decodeBase64()
        val response: okhttp3.Response = mockk {
            every { code } returns 304
            every { body } returns data?.toResponseBody()
        }
        val cachedResponse: okhttp3.Response = mockk {
            every { isSuccessful } returns true
            every { networkResponse } returns response
        }

        coEvery { cclConfigurationApiV1.getCCLConfiguration() } returns Response.success(
            data?.toResponseBody(),
            cachedResponse
        )

        instance.getCCLConfiguration() shouldBe null
    }
}
