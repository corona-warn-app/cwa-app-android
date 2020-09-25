package de.rki.coronawarnapp.http.playbook

import de.rki.coronawarnapp.exception.http.InternalServerErrorException
import de.rki.coronawarnapp.http.WebRequestBuilder
import de.rki.coronawarnapp.playbook.DefaultPlaybook
import de.rki.coronawarnapp.service.submission.KeyType
import de.rki.coronawarnapp.submission.server.SubmissionServer
import de.rki.coronawarnapp.util.formatter.TestResult
import de.rki.coronawarnapp.util.newWebRequestBuilder
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.hamcrest.Matchers.equalTo
import org.junit.Assert.fail
import org.junit.Test
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import testhelpers.BaseTest

class PlaybookImplTest : BaseTest() {

    @MockK lateinit var submissionServer: SubmissionServer
    @MockK lateinit var webRequestBuilder: WebRequestBuilder

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    private fun createPlaybook() = DefaultPlaybook(
        webRequestBuilder = webRequestBuilder,
        submissionServer = submissionServer
    )

    @Test
    fun hasRequestPattern_initialRegistration(): Unit = runBlocking {
        val server = MockWebServer()
        server.start()

        server.enqueue(MockResponse().setBody("""{"registrationToken":"response"}"""))
        server.enqueue(MockResponse().setBody("{}"))
        server.enqueue(MockResponse().setBody("{}"))

        DefaultPlaybook(server.newWebRequestBuilder())
            .initialRegistration("9A3B578UMG", KeyType.TELETAN)

        // ensure request order is 2x verification and 1x submission
        assertRequestPattern(server)
    }

    @Test
    fun hasRequestPattern_submission(): Unit = runBlocking {
        val server = MockWebServer()
        server.start()

        server.enqueue(MockResponse().setBody("""{"tan":"response"}"""))
        server.enqueue(MockResponse().setBody("{}"))
        server.enqueue(MockResponse().setBody("{}"))

        DefaultPlaybook(server.newWebRequestBuilder())
            .submission("token", listOf())

        // ensure request order is 2x verification and 1x submission
        assertRequestPattern(server)
    }

    @Test
    fun hasRequestPattern_testResult(): Unit = runBlocking {
        val server = MockWebServer()
        server.start()

        server.enqueue(MockResponse().setBody("""{"testResult":0}"""))
        server.enqueue(MockResponse().setBody("{}"))
        server.enqueue(MockResponse().setBody("{}"))

        DefaultPlaybook(server.newWebRequestBuilder())
            .testResult("token")

        // ensure request order is 2x verification and 1x submission
        assertRequestPattern(server)
    }

    @Test
    fun hasRequestPattern_dummy(): Unit = runBlocking {
        val server = MockWebServer()
        server.start()

        server.enqueue(MockResponse().setBody("{}"))
        server.enqueue(MockResponse().setBody("{}"))
        server.enqueue(MockResponse().setBody("{}"))

        DefaultPlaybook(server.newWebRequestBuilder())
            .dummy()

        // ensure request order is 2x verification and 1x submission
        assertRequestPattern(server)
    }

    @Test
    fun shouldIgnoreFailuresForDummyRequests(): Unit = runBlocking {
        val server = MockWebServer()
        server.start()

        val expectedRegistrationToken = "token"
        val expectedTestResult = TestResult.PENDING
        server.enqueue(MockResponse().setBody("""{"registrationToken":"$expectedRegistrationToken"}"""))
        server.enqueue(MockResponse().setBody("""{"testResult":${expectedTestResult.value}}"""))
        server.enqueue(MockResponse().setResponseCode(500))

        val (registrationToken, testResult) = DefaultPlaybook(server.newWebRequestBuilder())
            .initialRegistration("key", KeyType.GUID)

        assertThat(registrationToken, equalTo(expectedRegistrationToken))
        assertThat(testResult, equalTo(expectedTestResult))
    }

    @Test
    fun hasRequestPatternWhenRealRequestFails_initialRegistrationFirst(): Unit = runBlocking {
        val server = MockWebServer()
        server.start()

        server.enqueue(MockResponse().setResponseCode(500))
        server.enqueue(MockResponse().setBody("{}"))
        server.enqueue(MockResponse().setBody("{}"))

        try {

            DefaultPlaybook(server.newWebRequestBuilder())
                .initialRegistration("9A3B578UMG", KeyType.TELETAN)
            fail("exception propagation expected")
        } catch (e: InternalServerErrorException) {
        }

        // ensure request order is 2x verification and 1x submission
        assertRequestPattern(server)
    }

    @Test
    fun hasRequestPatternWhenRealRequestFails_initialRegistrationSecond(): Unit = runBlocking {
        val server = MockWebServer()
        server.start()

        server.enqueue(MockResponse().setBody("""{"registrationToken":"response"}"""))
        server.enqueue(MockResponse().setResponseCode(500))
        server.enqueue(MockResponse().setBody("{}"))

        try {
            DefaultPlaybook(server.newWebRequestBuilder())
                .initialRegistration("9A3B578UMG", KeyType.TELETAN)
            fail("exception propagation expected")
        } catch (e: InternalServerErrorException) {
        }

        // ensure request order is 2x verification and 1x submission
        assertRequestPattern(server)
    }

    @Test
    fun hasRequestPatternWhenRealRequestFails_testResult(): Unit = runBlocking {
        val server = MockWebServer()
        server.start()

        server.enqueue(MockResponse().setResponseCode(500))
        server.enqueue(MockResponse().setBody("{}"))
        server.enqueue(MockResponse().setBody("{}"))

        try {

            DefaultPlaybook(server.newWebRequestBuilder())
                .testResult("token")
            fail("exception propagation expected")
        } catch (e: InternalServerErrorException) {
        }

        // ensure request order is 2x verification and 1x submission
        assertRequestPattern(server)
    }

    @Test
    fun hasRequestPatternWhenRealRequestFails_submission(): Unit = runBlocking {
        val server = MockWebServer()
        server.start()

        server.enqueue(MockResponse().setResponseCode(500))
        server.enqueue(MockResponse().setBody("{}"))
        server.enqueue(MockResponse().setBody("{}"))

        try {
            DefaultPlaybook(server.newWebRequestBuilder())
                .submission("token", listOf())
            fail("exception propagation expected")
        } catch (e: InternalServerErrorException) {
        }

        // ensure request order is 2x verification and 1x submission
        assertRequestPattern(server)
    }

    private fun assertRequestPattern(server: MockWebServer) {
        assertThat(server.takeRequest().path, Matchers.startsWith("/verification/"))
        assertThat(server.takeRequest().path, Matchers.startsWith("/verification/"))
        assertThat(server.takeRequest().path, Matchers.startsWith("/submission/"))
    }
}
