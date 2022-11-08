package de.rki.coronawarnapp.srs.core.playbook

import de.rki.coronawarnapp.srs.core.error.SrsSubmissionException
import de.rki.coronawarnapp.srs.core.model.SrsAuthorizationRequest
import de.rki.coronawarnapp.srs.core.model.SrsSubmissionPayload
import de.rki.coronawarnapp.srs.core.server.SrsAuthorizationServer
import de.rki.coronawarnapp.srs.core.server.SrsSubmissionServer
import io.kotest.assertions.any
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.inspectors.runTest
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import testhelpers.BaseTest
import java.time.Instant

internal class SrsPlaybookTest : BaseTest() {

    @MockK lateinit var srsSubmissionServer: SrsSubmissionServer
    @MockK lateinit var srsAuthorizationServer: SrsAuthorizationServer

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        coEvery { srsSubmissionServer.submit(any()) } just Runs
        coEvery { srsAuthorizationServer.authorize(any()) } returns Instant.now()
    }

    @Test
    fun authorize() = runTest {
        val request = mockk<SrsAuthorizationRequest>()
        instance().authorize(request)
        coVerify { srsAuthorizationServer.authorize(request) }
    }

    @Test
    fun submit() = runTest {
        val payload = mockk<SrsSubmissionPayload>()
        instance().submit(payload)
        coVerify { srsSubmissionServer.submit(payload) }
    }

    @Test
    fun `Playbook throws what auth server throws`() = runTest {
        coEvery { srsAuthorizationServer.authorize(any()) } throws
            SrsSubmissionException(SrsSubmissionException.ErrorCode.SRS_OTP_SERVER_ERROR)
        val request = mockk<SrsAuthorizationRequest>()
        shouldThrow<SrsSubmissionException> {
            instance().authorize(request)
        }.errorCode shouldBe SrsSubmissionException.ErrorCode.SRS_OTP_SERVER_ERROR
    }

    @Test
    fun `Playbook throws what submission server throws`() = runTest {
        coEvery { srsSubmissionServer.submit(any()) } throws
            SrsSubmissionException(SrsSubmissionException.ErrorCode.SRS_SUB_SERVER_ERROR)
        val payload = mockk<SrsSubmissionPayload>()
        shouldThrow<SrsSubmissionException> {
            instance().submit(payload)
        }.errorCode shouldBe SrsSubmissionException.ErrorCode.SRS_SUB_SERVER_ERROR
    }

    private fun instance() = SrsPlaybook(
        srsSubmissionServer = srsSubmissionServer,
        srsAuthorizationServer = srsAuthorizationServer
    )
}
