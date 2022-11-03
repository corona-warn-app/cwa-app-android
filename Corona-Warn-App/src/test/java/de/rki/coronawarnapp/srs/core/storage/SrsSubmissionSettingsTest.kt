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

internal class SrsSubmissionSettingsTest : BaseTest() {

    private var fakeStore = FakeDataStore()
    private val time = Instant.parse("2022-11-02T14:01:22Z")
    private val otp = SrsOtp(
        otp = "73a373fd-3a7b-49b9-b71c-2ae7a2824760",
        expiresAt = time
    )

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
            setOtp(otp)
            fakeStore[SrsSubmissionSettings.SRS_OTP_KEY] shouldBe """
                {"otp":"73a373fd-3a7b-49b9-b71c-2ae7a2824760","expiresAt":1667397682.000000000}
            """.trimIndent()
            getOtp() shouldBe otp
        }
    }

    @Test
    fun setOtp() = runTest {
        instance().apply {
            setOtp(otp)
            fakeStore[SrsSubmissionSettings.SRS_OTP_KEY] shouldBe """
                {"otp":"73a373fd-3a7b-49b9-b71c-2ae7a2824760","expiresAt":1667397682.000000000}
            """.trimIndent()
            getOtp() shouldBe otp
        }
    }

    private fun instance() = SrsSubmissionSettings(
        dataStore = fakeStore,
        mapper = SerializationModule.jacksonBaseMapper
    )
}
