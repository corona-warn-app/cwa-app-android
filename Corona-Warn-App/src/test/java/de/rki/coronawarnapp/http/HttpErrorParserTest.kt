package de.rki.coronawarnapp.http

import de.rki.coronawarnapp.exception.http.CwaWebException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class HttpErrorParserTest : BaseTest() {

    @MockK lateinit var chain: Interceptor.Chain

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { chain.request() } returns mockk()
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    private val baseResponse: Response.Builder
        get() = Response.Builder().apply {
            protocol(Protocol.HTTP_1_1)
            Request.Builder().apply {
                url("http://url.url")
            }.build().let { request(it) }
        }

    @Test
    fun `normal response`() {
        val response = baseResponse.apply {
            code(200)
            message("")
        }.build()
        every { chain.proceed(any()) } returns response
        HttpErrorParser().intercept(chain) shouldBe response
    }

    @Test
    fun `error without message`() {
        val response = baseResponse.apply {
            code(404)
            message("")
        }.build()
        every { chain.proceed(any()) } returns response
        val exception = shouldThrow<CwaWebException> { HttpErrorParser().intercept(chain) }
        exception.statusCode shouldBe 404
        exception.message shouldContain "code=404 message= body=null"
    }

    @Test
    fun `error with message`() {
        val response = baseResponse.apply {
            code(403)
            message("Forbidden")
        }.build()
        every { chain.proceed(any()) } returns response
        val exception = shouldThrow<CwaWebException> { HttpErrorParser().intercept(chain) }
        exception.statusCode shouldBe 403
        exception.message shouldContain "message=Forbidden"
        exception.message shouldContain "body=null"
    }

    @Test
    fun `error in body`() {
        val response = baseResponse.apply {
            code(500)
            message("")
            body("{\"errorCode\":\"APK_CERTIFICATE_MISMATCH\"}".toResponseBody("application/json".toMediaTypeOrNull()))
        }.build()
        every { chain.proceed(any()) } returns response
        val exception = shouldThrow<CwaWebException> { HttpErrorParser().intercept(chain) }
        exception.statusCode shouldBe 500
        exception.message shouldContain "message= "
        exception.message shouldContain "body={\"errorCode\":\"APK_CERTIFICATE_MISMATCH\"}"
    }

    @Test
    fun `error in message and body`() {
        val response = baseResponse.apply {
            code(501)
            message("Error")
            body("{\"errorCode\":\"APK_CERTIFICATE_MISMATCH\"}".toResponseBody("application/json".toMediaTypeOrNull()))
        }.build()
        every { chain.proceed(any()) } returns response
        val exception = shouldThrow<CwaWebException> { HttpErrorParser().intercept(chain) }
        exception.statusCode shouldBe 501
        exception.message shouldContain "message=Error"
        exception.message shouldContain "body={\"errorCode\":\"APK_CERTIFICATE_MISMATCH\"}"
    }

    @Test
    fun `oversized errors are handled`() {
        val response = baseResponse.apply {
            code(501)
            message("")
            body(
                (1..5000).joinToString { "1" }.toResponseBody()
            )
        }.build()
        every { chain.proceed(any()) } returns response
        val exception = shouldThrow<CwaWebException> { HttpErrorParser().intercept(chain) }
        exception.statusCode shouldBe 501
        val start = exception.message!!.indexOf("body=")
        val body = exception.message!!.substring(start + "body=".length)
        body.length shouldBe 2049
    }
}
