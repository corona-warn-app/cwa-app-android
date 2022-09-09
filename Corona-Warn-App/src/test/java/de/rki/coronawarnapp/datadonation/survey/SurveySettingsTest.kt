package de.rki.coronawarnapp.datadonation.survey

import de.rki.coronawarnapp.datadonation.OTPAuthorizationResult
import de.rki.coronawarnapp.datadonation.OneTimePassword
import de.rki.coronawarnapp.datadonation.survey.SurveySettings.Companion.KEY_OTP
import de.rki.coronawarnapp.util.serialization.SerializationModule
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.flow.first
import org.junit.jupiter.api.AfterEach
import java.time.Instant
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.preferences.FakeDataStore
import java.util.UUID

class SurveySettingsTest : BaseTest() {

    private val objectMapper = SerializationModule().jacksonObjectMapper()
    private val dataStore = FakeDataStore()

    @AfterEach
    fun cleanup() {
        dataStore.reset()
    }

    private fun buildInstance(): SurveySettings = SurveySettings(
        dataStore = dataStore,
        objectMapper = objectMapper
    )

    @Test
    fun `load and deserialize otp json`() = runTest {
        dataStore[KEY_OTP] shouldBe null

        with(buildInstance()) {
            oneTimePassword.first() shouldBe null
            updateOneTimePassword(
                OneTimePassword(
                    uuid = UUID.fromString("e103c755-0975-4588-a639-d0cd1ba421a1"),
                    time = Instant.ofEpochMilli(1612381217442)
                )
            )
            val value = oneTimePassword.first()
            value shouldNotBe null
            value!!.uuid.toString() shouldBe "e103c755-0975-4588-a639-d0cd1ba421a1"
            value.time.toEpochMilli() shouldBe 1612381217442
        }
    }

    @Test
    fun `save and serialize otp json`() = runTest {
        val otp = OneTimePassword(
            UUID.fromString("e103c755-0975-4588-a639-d0cd1ba421a0"),
            Instant.ofEpochMilli(1612381567242)
        )

        with(buildInstance()) {
            updateOneTimePassword(otp)
            val value = oneTimePassword.first()
            value shouldBe otp
        }
    }

    @Test
    fun `load and deserialize auth result json`() = runTest {
        with(buildInstance()) {
            otpAuthorizationResult.first() shouldBe null

            updateOtpAuthorizationResult(
                OTPAuthorizationResult(
                    uuid = UUID.fromString("e103c755-0975-4588-a639-d0cd1ba421a1"),
                    authorized = true,
                    redeemedAt = Instant.ofEpochMilli(1612381217443),
                    invalidated = true
                )
            )
            val value = otpAuthorizationResult.first()
            value shouldNotBe null
            value!!.uuid.toString() shouldBe "e103c755-0975-4588-a639-d0cd1ba421a1"
            value.authorized shouldBe true
            value.redeemedAt.toEpochMilli() shouldBe 1612381217443
            value.invalidated shouldBe true
        }
    }

    @Test
    fun `save auth result`() = runTest {

        val otpAuthorizationResult = OTPAuthorizationResult(
            uuid = UUID.fromString("e103c755-0975-4588-a639-d0cd1ba421a0"),
            authorized = false,
            redeemedAt = Instant.ofEpochMilli(1612381217445),
            invalidated = false
        )

        with(buildInstance()) {
            updateOtpAuthorizationResult(otpAuthorizationResult)
            this.otpAuthorizationResult.first() shouldBe otpAuthorizationResult
        }
    }
}
