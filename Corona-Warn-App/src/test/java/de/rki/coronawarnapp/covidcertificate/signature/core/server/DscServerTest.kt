package de.rki.coronawarnapp.covidcertificate.signature.core.server

import de.rki.coronawarnapp.covidcertificate.signature.core.DscRawData.DSC_LIST_BASE64
import de.rki.coronawarnapp.covidcertificate.signature.core.common.exception.DscValidationException
import de.rki.coronawarnapp.util.encoding.base64
import de.rki.coronawarnapp.util.security.SignatureValidation
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.ByteString.Companion.decodeBase64
import okio.ByteString.Companion.decodeHex
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import retrofit2.Response

@Suppress("MaxLineLength")
internal class DscServerTest {

    @MockK lateinit var dscApi: DscApiV1
    @MockK lateinit var signatureValidation: SignatureValidation

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { signatureValidation.hasValidSignature(any(), any()) } returns true
        coEvery { dscApi.dscList() } returns Response.success(DSC_LIST.decodeBase64()?.toResponseBody())
    }

    private fun createInstance() = DscServer(
        signatureValidation = signatureValidation,
        dscApi = dscApi
    )

    @Test
    fun `successful download`() = runTest {

        val server = createInstance()

        val rawData = server.getDscList()

        rawData.base64() shouldBe DSC_LIST_BASE64

        verify(exactly = 1) { signatureValidation.hasValidSignature(any(), any()) }
    }

    @Test
    fun `data is faulty`() = runTest {
        coEvery { dscApi.dscList() } returns Response.success("123ABC".decodeHex().toResponseBody())

        val server = createInstance()

        shouldThrow<DscValidationException> {
            server.getDscList()
        }.errorCode shouldBe DscValidationException.ErrorCode.FILE_MISSING
    }

    @Test
    fun `verification fails`() = runTest {
        every { signatureValidation.hasValidSignature(any(), any()) } returns false

        val server = createInstance()

        shouldThrow<DscValidationException> {
            server.getDscList()
        }.errorCode shouldBe DscValidationException.ErrorCode.SIGNATURE_INVALID
    }

    companion object {
        const val DSC_LIST =
            "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"
    }
}
