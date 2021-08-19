package de.rki.coronawarnapp.playbook

import de.rki.coronawarnapp.coronatest.server.RegistrationRequest
import de.rki.coronawarnapp.coronatest.server.VerificationApiV1
import de.rki.coronawarnapp.coronatest.server.VerificationKeyType
import de.rki.coronawarnapp.coronatest.server.VerificationServer
import de.rki.coronawarnapp.presencetracing.checkins.CheckInsReport
import de.rki.coronawarnapp.presencetracing.organizer.submission.server.OrganizerSubmissionServer
import de.rki.coronawarnapp.server.protocols.internal.pt.CheckInOuterClass
import de.rki.coronawarnapp.submission.server.SubmissionServer
import io.kotest.assertions.throwables.shouldThrow
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerifySequence
import io.mockk.impl.annotations.MockK
import io.mockk.just
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import retrofit2.Response
import testhelpers.BaseTest
import testhelpers.TestDispatcherProvider

internal class OrganizerPlaybookTest : BaseTest() {

    @MockK lateinit var verificationServer: VerificationServer
    @MockK lateinit var organizerSubmissionServer: OrganizerSubmissionServer
    @MockK lateinit var submissionServer: SubmissionServer
    private lateinit var organizerPlaybook: OrganizerPlaybook

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        with(verificationServer) {
            coEvery { retrieveRegistrationToken(any()) } returns "registrationToken"
            coEvery { retrieveTan(any()) } returns "uploadTan"
            coEvery { retrieveTanFake() } returns VerificationApiV1.TanResponse("tan")
        }

        coEvery { submissionServer.submitFakePayload() } just Runs
        coEvery { organizerSubmissionServer.submit(any(), any()) } returns Response.success("{}".toResponseBody())

        organizerPlaybook = OrganizerPlaybook(
            appScope = TestCoroutineScope(),
            verificationServer = verificationServer,
            organizerSubmissionServer = organizerSubmissionServer,
            submissionServer = submissionServer,
            dispatcherProvider = TestDispatcherProvider()
        )
    }

    @Test
    fun `submit Upload TAN pass`() = runBlockingTest {
        val checkInsReport = CheckInsReport(
            unencryptedCheckIns = listOf(CheckInOuterClass.CheckIn.getDefaultInstance()),
            encryptedCheckIns = listOf(CheckInOuterClass.CheckInProtectedReport.getDefaultInstance())
        )
        organizerPlaybook.submit("tan", checkInsReport)

        coVerifySequence {
            // Obtain TAN
            verificationServer.retrieveRegistrationToken(any())
            verificationServer.retrieveTan(any())
            submissionServer.submitFakePayload()

            // Submission
            verificationServer.retrieveTanFake()
            organizerSubmissionServer.submit(any(), any())
        }
    }

    @Test
    fun `submit Upload TAN fail`() = runBlockingTest {
        with(verificationServer) {
            coEvery { retrieveRegistrationToken(any()) } throws Exception()
            coEvery { retrieveTan(any()) } returns "uploadTan"
            coEvery { retrieveTanFake() } returns VerificationApiV1.TanResponse("tan")
        }

        val checkInsReport = CheckInsReport(
            unencryptedCheckIns = listOf(CheckInOuterClass.CheckIn.getDefaultInstance()),
            encryptedCheckIns = listOf(CheckInOuterClass.CheckInProtectedReport.getDefaultInstance())
        )

        shouldThrow<Exception> {
            organizerPlaybook.submit("tan", checkInsReport)
        }

        coVerifySequence {
            // Obtain TAN
            verificationServer.retrieveRegistrationToken(any())
            verificationServer.retrieveTanFake()
            submissionServer.submitFakePayload()

            // Submission
            verificationServer.retrieveTanFake()
            submissionServer.submitFakePayload()
        }
    }

    @Test
    fun `obtainUploadTan - Registration token pass`() = runBlockingTest {
        organizerPlaybook.obtainUploadTan(RegistrationRequest(key = "key", type = VerificationKeyType.TELETAN))

        coVerifySequence {
            verificationServer.retrieveRegistrationToken(any())
            verificationServer.retrieveTan(any())
            submissionServer.submitFakePayload()
        }
    }

    @Test
    fun `obtainUploadTan - Registration token fail`() = runBlockingTest {
        with(verificationServer) {
            coEvery { retrieveRegistrationToken(any()) } throws Exception()
            coEvery { retrieveTan(any()) } returns "uploadTan"
            coEvery { retrieveTanFake() } returns VerificationApiV1.TanResponse("tan")
        }

        shouldThrow<Exception> {
            organizerPlaybook.obtainUploadTan(RegistrationRequest(key = "key", type = VerificationKeyType.TELETAN))
        }

        coVerifySequence {
            verificationServer.retrieveRegistrationToken(any())
            verificationServer.retrieveTanFake()
            submissionServer.submitFakePayload()
        }
    }
}
