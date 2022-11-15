package de.rki.coronawarnapp.srs.core

import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.appconfig.SelfReportSubmissionConfigContainer
import de.rki.coronawarnapp.main.CWASettings
import de.rki.coronawarnapp.srs.core.error.SrsSubmissionException
import de.rki.coronawarnapp.srs.core.storage.SrsDevSettings
import de.rki.coronawarnapp.srs.core.storage.SrsSubmissionSettings
import de.rki.coronawarnapp.util.TimeStamper
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import testhelpers.BaseTest
import java.time.Instant

internal class SrsLocalCheckerTest : BaseTest() {

    @MockK lateinit var srsSubmissionSettings: SrsSubmissionSettings
    @MockK lateinit var appConfigProvider: AppConfigProvider
    @MockK lateinit var cwaSettings: CWASettings
    @MockK lateinit var timeStamper: TimeStamper
    @MockK lateinit var srsDevSettings: SrsDevSettings

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        every { timeStamper.nowUTC } returns Instant.parse("2022-11-02T14:01:22Z")
        every { cwaSettings.firstReliableDeviceTime } returns flowOf(Instant.parse("2022-10-02T14:01:22Z"))
        coEvery { srsSubmissionSettings.getMostRecentSubmissionTime() } returns
            Instant.parse("2022-08-02T14:01:22Z")
        coEvery { srsDevSettings.checkLocalPrerequisites() } returns true
        coEvery { appConfigProvider.getAppConfig() } returns config()
    }

    @Test
    fun `check pass`() = runTest {
        shouldNotThrow<SrsSubmissionException> {
            instance().check()
        }
    }

    @Test
    fun `device time is incorrect`() = runTest {
        coEvery { appConfigProvider.getAppConfig() } returns config(ConfigData.DeviceTimeState.INCORRECT)
        shouldThrow<SrsSubmissionException> {
            instance().check()
        }.errorCode shouldBe SrsSubmissionException.ErrorCode.DEVICE_TIME_INCORRECT
    }

    @Test
    fun `device time is assumed correct`() = runTest {
        coEvery { appConfigProvider.getAppConfig() } returns config(ConfigData.DeviceTimeState.ASSUMED_CORRECT)
        shouldThrow<SrsSubmissionException> {
            instance().check()
        }.errorCode shouldBe SrsSubmissionException.ErrorCode.DEVICE_TIME_UNVERIFIED
    }

    @Test
    fun `Time since onboarding is unverified`() = runTest {
        every { cwaSettings.firstReliableDeviceTime } returns flowOf(Instant.parse("2022-11-02T10:01:22Z"))
        shouldThrow<SrsSubmissionException> {
            instance().check()
        }.errorCode shouldBe SrsSubmissionException.ErrorCode.MIN_TIME_SINCE_ONBOARDING
    }

    @Test
    fun `Time since last submission is too early`() = runTest {
        coEvery { srsSubmissionSettings.getMostRecentSubmissionTime() } returns Instant.parse("2022-11-02T10:01:22Z")
        shouldThrow<SrsSubmissionException> {
            instance().check()
        }.errorCode shouldBe SrsSubmissionException.ErrorCode.SUBMISSION_TOO_EARLY
    }

    private fun instance() = SrsLocalChecker(
        srsSubmissionSettings = srsSubmissionSettings,
        appConfigProvider = appConfigProvider,
        cwaSettings = cwaSettings,
        timeStamper = timeStamper,
        srsDevSettings = srsDevSettings,
    )

    private fun config(
        state: ConfigData.DeviceTimeState = ConfigData.DeviceTimeState.CORRECT
    ) = mockk<ConfigData>().apply {
        every { selfReportSubmission } returns SelfReportSubmissionConfigContainer.DEFAULT
        every { deviceTimeState } returns state
    }
}
