package de.rki.coronawarnapp.bugreporting.censors

import de.rki.coronawarnapp.bugreporting.censors.submission.OtpCensor
import de.rki.coronawarnapp.bugreporting.censors.submission.PcrQrCodeCensor
import de.rki.coronawarnapp.srs.core.model.SrsOtp
import de.rki.coronawarnapp.srs.core.storage.SrsSubmissionSettings
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import java.time.Instant
import java.util.UUID

class OtpCensorTest : BaseTest() {

    @MockK lateinit var srsSubmissionSettings: SrsSubmissionSettings

    private val srsOtp = SrsOtp(
        uuid = UUID.fromString("73a373fd-3a7b-49b9-b71c-2ae7a2824760"),
        expiresAt = Instant.parse("2023-11-07T12:10:10Z")
    )

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        coEvery { srsSubmissionSettings.getOtp() } returns srsOtp
    }

    @AfterEach
    fun teardown() {
        PcrQrCodeCensor.lastGUID = null
    }

    private fun createInstance() = OtpCensor(
        srsSubmissionSettings
    )

    @Test
    fun `censoring replaces the otp uuid`() = runTest {
        val instance = createInstance()
        val censored = "This is the very secret otp: ${srsOtp.uuid}"
        instance.checkLog(censored)!!
            .compile()!!.censored shouldBe "This is the very secret otp: ########-####-####-####-########"
    }

    @Test
    fun `censoring replaces the otp expiration date`() = runTest {
        val instance = createInstance()
        val censored = "This is the expiration date of the secret otp: ${srsOtp.expiresAt}"
        instance.checkLog(censored)!!
            .compile()!!.censored shouldBe "This is the expiration date of the secret otp: SrsOtp/expiresAt"
    }
}
