package de.rki.coronawarnapp.srs.core.storage

import de.rki.coronawarnapp.srs.core.model.SrsOtp
import de.rki.coronawarnapp.util.serialization.SerializationModule
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import testhelpers.BaseTest
import testhelpers.preferences.FakeDataStore
import java.time.Instant
import java.util.UUID

internal class SrsSubmissionSettingsTest : BaseTest() {

    private var fakeStore = FakeDataStore()
    private val time = Instant.parse("2022-11-02T14:01:22Z")
    private val otpTest = SrsOtp(
        uuid = UUID.fromString("73a373fd-3a7b-49b9-b71c-2ae7a2824760"), expiresAt = time
    )

    private val json = """
                {"otp":"73a373fd-3a7b-49b9-b71c-2ae7a2824760","expiresAt":1667397682.000000000}
    """.trimIndent()

    @BeforeEach
    fun setUp() {
        fakeStore = FakeDataStore()
    }

    @Test
    fun getMostRecentSubmissionTime() = runTest {
        instance().apply {
            fakeStore[SrsSubmissionSettings.LAST_SUBMISSION_TIME_KEY] shouldBe null
            getMostRecentSubmissionTime() shouldBe Instant.EPOCH
            setMostRecentSubmissionTime(time)
            fakeStore[SrsSubmissionSettings.LAST_SUBMISSION_TIME_KEY] shouldBe 1667397682000L
            getMostRecentSubmissionTime() shouldBe time
        }
    }

    @Test
    fun setMostRecentSubmissionTime() = runTest {
        instance().apply {
            getMostRecentSubmissionTime() shouldBe Instant.EPOCH
            setMostRecentSubmissionTime(time)
            fakeStore[SrsSubmissionSettings.LAST_SUBMISSION_TIME_KEY] shouldBe 1667397682000L
            getMostRecentSubmissionTime() shouldBe time
        }
    }

    @Test
    fun getOtp() = runTest {
        instance().apply {
            fakeStore[SrsSubmissionSettings.SRS_OTP_KEY] shouldBe null
            getOtp() shouldBe null
            setOtp(otpTest)
            fakeStore[SrsSubmissionSettings.SRS_OTP_KEY] shouldBe json
            getOtp() shouldBe otpTest
        }
    }

    @Test
    fun setOtp() = runTest {
        instance().apply {
            setOtp(otpTest)
            fakeStore[SrsSubmissionSettings.SRS_OTP_KEY] shouldBe json
            getOtp() shouldBe otpTest
        }
    }

    @Test
    fun resetMostRecentSubmission() = runTest {
        instance().apply {
            setOtp(otpTest)
            fakeStore[SrsSubmissionSettings.SRS_OTP_KEY] shouldBe json
            setMostRecentSubmissionTime(time)
            fakeStore[SrsSubmissionSettings.LAST_SUBMISSION_TIME_KEY] shouldBe 1667397682000L

            resetMostRecentSubmission()

            fakeStore[SrsSubmissionSettings.SRS_OTP_KEY] shouldBe json
            fakeStore[SrsSubmissionSettings.LAST_SUBMISSION_TIME_KEY] shouldBe null
        }
    }

    @Test
    fun resetOtp() = runTest {
        instance().apply {
            setOtp(otpTest)
            fakeStore[SrsSubmissionSettings.SRS_OTP_KEY] shouldBe json
            setMostRecentSubmissionTime(time)
            fakeStore[SrsSubmissionSettings.LAST_SUBMISSION_TIME_KEY] shouldBe 1667397682000L

            resetOtp()

            fakeStore[SrsSubmissionSettings.SRS_OTP_KEY] shouldBe null
            fakeStore[SrsSubmissionSettings.LAST_SUBMISSION_TIME_KEY] shouldBe 1667397682000L
        }
    }

    private fun instance() = SrsSubmissionSettings(
        dataStore = fakeStore, mapper = SerializationModule.jacksonBaseMapper
    )
}
