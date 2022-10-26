package de.rki.coronawarnapp.coronatest.type.rapidantigen

import de.rki.coronawarnapp.appconfig.CoronaTestConfig
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.exception.http.BadRequestException
import de.rki.coronawarnapp.util.TimeStamper
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.instanceOf
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import java.net.SocketException
import java.time.Instant

class RapidAntigenCoronaTestExtensionsTest : BaseTest() {
    @MockK lateinit var coronaTestConfig: CoronaTestConfig
    @MockK lateinit var timeStamper: TimeStamper

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { timeStamper.nowUTC } returns Instant.ofEpochMilli(1010010101)
        every { coronaTestConfig.ratParameters.hoursToDeemTestOutdated } returns
            java.time.Duration.ofHours(48)
    }

    @Test
    fun `state determination, unregistered test`() = runTest {
        val test: RACoronaTest? = null
        test.toSubmissionState(timeStamper.nowUTC, coronaTestConfig) shouldBe SubmissionStateRAT.NoTest
    }

    @Test
    fun `submission done mapping`() = runTest {
        val test = RACoronaTest(
            identifier = "identifier",
            registeredAt = Instant.ofEpochMilli(123),
            registrationToken = "regtoken",
            testResult = CoronaTestResult.RAT_POSITIVE,
            testedAt = Instant.EPOCH,
            isSubmitted = true, // <---
            isViewed = true,
            dateOfBirth = null,
            firstName = null,
            lastName = null,
            lastUpdatedAt = Instant.EPOCH,
        )
        test.toSubmissionState(timeStamper.nowUTC, coronaTestConfig) shouldBe SubmissionStateRAT.SubmissionDone(
            testRegisteredAt = Instant.EPOCH
        )
    }

    // EXPOSUREAPP-6784 / https://github.com/corona-warn-app/cwa-app-android/issues/2953
    @Test
    fun `errors that are not http 400 do not affect result state`() = runTest {
        val test = RACoronaTest(
            identifier = "identifier",
            registeredAt = Instant.ofEpochMilli(123),
            registrationToken = "regtoken",
            testResult = CoronaTestResult.RAT_POSITIVE,
            testedAt = Instant.EPOCH,
            dateOfBirth = null,
            firstName = null,
            lastName = null,
            lastUpdatedAt = Instant.EPOCH,
            lastError = SocketException("Connection reset")
        )
        test.toSubmissionState(
            timeStamper.nowUTC,
            coronaTestConfig
        ) shouldBe instanceOf(SubmissionStateRAT.TestResultReady::class)
    }

    @Test
    fun `client http 400 errors result in invalid test state`() = runTest {
        val test = RACoronaTest(
            identifier = "identifier",
            registeredAt = Instant.ofEpochMilli(123),
            registrationToken = "regtoken",
            testResult = CoronaTestResult.RAT_POSITIVE,
            testedAt = Instant.EPOCH,
            dateOfBirth = null,
            firstName = null,
            lastName = null,
            lastUpdatedAt = Instant.EPOCH,
            lastError = BadRequestException("")
        )
        test.toSubmissionState(
            timeStamper.nowUTC,
            coronaTestConfig
        ) shouldBe instanceOf(SubmissionStateRAT.TestInvalid::class)
    }

    @Test
    fun `recycled test returns no test`() {
        val test = RACoronaTest(
            identifier = "identifier",
            registeredAt = Instant.ofEpochMilli(123),
            registrationToken = "regtoken",
            testResult = CoronaTestResult.RAT_POSITIVE,
            testedAt = Instant.EPOCH,
            dateOfBirth = null,
            firstName = null,
            lastName = null,
            lastUpdatedAt = Instant.EPOCH,
            recycledAt = timeStamper.nowUTC
        )
        test.toSubmissionState(
            timeStamper.nowUTC,
            coronaTestConfig
        ) shouldBe SubmissionStateRAT.NoTest
    }
}
