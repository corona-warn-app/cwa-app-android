package de.rki.coronawarnapp.coronatest.type.pcr

import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.exception.http.BadRequestException
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.instanceOf
import kotlinx.coroutines.test.runTest
import java.time.Instant
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import java.net.SocketException

class PCRCoronaTestExtensionsTest : BaseTest() {

    @Test
    fun `state determination, unregistered test`() = runTest {
        val test: PCRCoronaTest? = null
        test.toSubmissionState() shouldBe SubmissionStatePCR.NoTest
    }

    // EXPOSUREAPP-6784 / https://github.com/corona-warn-app/cwa-app-android/issues/2953
    @Test
    fun `non http 400 errors do not affect result state`() = runTest {
        val test = PCRCoronaTest(
            identifier = "identifier",
            registeredAt = Instant.ofEpochMilli(123),
            registrationToken = "regtoken",
            testResult = CoronaTestResult.PCR_POSITIVE,
            lastUpdatedAt = Instant.EPOCH,
            lastError = SocketException("Connection reset")
        )
        test.toSubmissionState() shouldBe instanceOf(SubmissionStatePCR.TestResultReady::class)
    }

    @Test
    fun `client HTTP400 errors result in invalid test state`() = runTest {
        val test = PCRCoronaTest(
            identifier = "identifier",
            registeredAt = Instant.ofEpochMilli(123),
            registrationToken = "regtoken",
            testResult = CoronaTestResult.PCR_POSITIVE,
            lastUpdatedAt = Instant.EPOCH,
            lastError = BadRequestException("")
        )
        test.toSubmissionState() shouldBe instanceOf(SubmissionStatePCR.TestInvalid::class)
    }

    @Test
    fun `recycled test returns no test`() {
        val test = PCRCoronaTest(
            identifier = "identifier",
            registeredAt = Instant.ofEpochMilli(123),
            registrationToken = "regtoken",
            testResult = CoronaTestResult.PCR_POSITIVE,
            lastUpdatedAt = Instant.EPOCH,
            recycledAt = Instant.EPOCH
        )
        test.toSubmissionState() shouldBe SubmissionStatePCR.NoTest
    }
}
