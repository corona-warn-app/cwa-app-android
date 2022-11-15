package de.rki.coronawarnapp.bugreporting.censors

import de.rki.coronawarnapp.bugreporting.censors.submission.OtpCensor
import de.rki.coronawarnapp.srs.core.model.SrsOtp
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import java.time.Instant
import java.util.UUID

class OtpCensorTest : BaseTest() {

    private val srsOtp = SrsOtp(
        uuid = UUID.fromString("73a373fd-3a7b-49b9-b71c-2ae7a2824760"),
        expiresAt = Instant.parse("2023-11-07T12:10:10Z")
    )

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    @AfterEach
    fun teardown() {
        OtpCensor.otp = null
    }

    private fun createInstance() = OtpCensor()

    @Test
    fun `censoring replaces the otp uuid`() = runTest {
        OtpCensor.otp = srsOtp
        val instance = createInstance()
        val censored = "This is the very secret otp: ${srsOtp.uuid}"
        instance.checkLog(censored)!!
            .compile()!!.censored shouldBe "This is the very secret otp: ${OtpCensor.OTP_MASK}"
    }

    @Test
    fun `censoring replaces the otp expiration date`() = runTest {
        OtpCensor.otp = srsOtp
        val instance = createInstance()
        val censored = "This is the expiration date of the secret otp: ${srsOtp.expiresAt}"
        instance.checkLog(censored)!!
            .compile()!!.censored shouldBe "This is the expiration date of the secret otp: ${OtpCensor.DATE_MASK}"
    }
}
