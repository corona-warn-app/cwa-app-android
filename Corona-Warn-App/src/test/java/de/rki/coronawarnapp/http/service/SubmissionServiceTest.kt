package de.rki.coronawarnapp.http.service

import de.rki.coronawarnapp.util.headerSizeIgnoringContentLength
import de.rki.coronawarnapp.util.newWebRequestBuilder
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert
import org.junit.Test

class SubmissionServiceTest {

    @Test
    fun allRequestHaveSameFootprintForPlausibleDeniability(): Unit = runBlocking {

        val server = MockWebServer()
        server.start()

        val webRequestBuilder = server.newWebRequestBuilder()

        val authCodeExample = "39ec4930-7a1f-4d5d-921f-bfad3b6f1269"

        server.enqueue(MockResponse().setBody("{}"))
        webRequestBuilder.asyncSubmitKeysToServer(authCodeExample, listOf(), false, listOf())

        server.enqueue(MockResponse().setBody("{}"))
        webRequestBuilder.asyncFakeSubmission()

        val requests = listOf(
            server.takeRequest(),
            server.takeRequest()
        )

        // ensure all request have same size (header & body)
        requests.zipWithNext().forEach { (a, b) ->
            Assert.assertEquals(
                "Header size mismatch: ",
                a.headerSizeIgnoringContentLength(),
                b.headerSizeIgnoringContentLength()
            )
        }
    }
}
