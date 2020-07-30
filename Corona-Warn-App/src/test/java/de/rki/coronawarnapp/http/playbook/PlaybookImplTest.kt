package de.rki.coronawarnapp.http.playbook

import de.rki.coronawarnapp.service.submission.KeyType
import de.rki.coronawarnapp.util.newWebRequestBuilder
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.junit.Test

class PlaybookImplTest {

    @Test
    fun hasRequestPattern_initialRegistration(): Unit = runBlocking {
        val server = MockWebServer()
        server.start()

        server.enqueue(MockResponse().setBody("{}"))
        server.enqueue(MockResponse().setBody("{}"))
        server.enqueue(MockResponse().setBody("{}"))

        PlaybookImpl(server.newWebRequestBuilder())
            .initialRegistration("9A3B578UMG", KeyType.TELETAN)

        // ensure request order is 2x verification and 1x submission
        assertThat(server.takeRequest().path, Matchers.startsWith("/verification/"))
        assertThat(server.takeRequest().path, Matchers.startsWith("/verification/"))
        assertThat(server.takeRequest().path, Matchers.startsWith("/submission/"))
    }

    @Test
    fun hasRequestPattern_submission(): Unit = runBlocking {
        val server = MockWebServer()
        server.start()

        server.enqueue(MockResponse().setBody("{}"))
        server.enqueue(MockResponse().setBody("{}"))
        server.enqueue(MockResponse().setBody("{}"))

        PlaybookImpl(server.newWebRequestBuilder())
            .submission("token", listOf())

        // ensure request order is 2x verification and 1x submission
        assertThat(server.takeRequest().path, Matchers.startsWith("/verification/"))
        assertThat(server.takeRequest().path, Matchers.startsWith("/verification/"))
        assertThat(server.takeRequest().path, Matchers.startsWith("/submission/"))
    }

    @Test
    fun hasRequestPattern_testResult(): Unit = runBlocking {
        val server = MockWebServer()
        server.start()

        server.enqueue(MockResponse().setBody("{}"))
        server.enqueue(MockResponse().setBody("{}"))
        server.enqueue(MockResponse().setBody("{}"))

        PlaybookImpl(server.newWebRequestBuilder())
            .testResult("token")

        // ensure request order is 2x verification and 1x submission
        assertThat(server.takeRequest().path, Matchers.startsWith("/verification/"))
        assertThat(server.takeRequest().path, Matchers.startsWith("/verification/"))
        assertThat(server.takeRequest().path, Matchers.startsWith("/submission/"))
    }

    @Test
    fun hasRequestPattern_dummy(): Unit = runBlocking {
        val server = MockWebServer()
        server.start()

        server.enqueue(MockResponse().setBody("{}"))
        server.enqueue(MockResponse().setBody("{}"))
        server.enqueue(MockResponse().setBody("{}"))

        PlaybookImpl(server.newWebRequestBuilder())
            .dummy()

        // ensure request order is 2x verification and 1x submission
        assertThat(server.takeRequest().path, Matchers.startsWith("/verification/"))
        assertThat(server.takeRequest().path, Matchers.startsWith("/verification/"))
        assertThat(server.takeRequest().path, Matchers.startsWith("/submission/"))
    }

}