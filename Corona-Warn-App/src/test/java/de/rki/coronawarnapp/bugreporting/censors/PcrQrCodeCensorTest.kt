package de.rki.coronawarnapp.bugreporting.censors

import de.rki.coronawarnapp.bugreporting.censors.submission.PcrQrCodeCensor
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class PcrQrCodeCensorTest : BaseTest() {

    private val testGUID = "63b4d3ff-e0de-4bd4-90c1-17c2bb683a2f"

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    @AfterEach
    fun teardown() {
        PcrQrCodeCensor.lastGUID = null
    }

    private fun createInstance() = PcrQrCodeCensor()

    @Test
    fun `censoring replaces the logline message`() = runTest {
        PcrQrCodeCensor.lastGUID = testGUID
        val instance = createInstance()
        val censored = "I'm a shy qrcode: $testGUID"
        instance.checkLog(censored)!!
            .compile()!!.censored shouldBe "I'm a shy qrcode: ########-####-####-####-########3a2f"
    }

    @Test
    fun `censoring returns null if there is no match`() = runTest {
        PcrQrCodeCensor.lastGUID = testGUID.replace("f", "a")
        val instance = createInstance()
        val notCensored = "I'm a shy qrcode: $testGUID"
        instance.checkLog(notCensored) shouldBe null
    }

    @Test
    fun `censoring aborts if no qrcode was set`() = runTest {
        PcrQrCodeCensor.lastGUID = null
        val instance = createInstance()
        val notCensored = "I'm a shy qrcode: $testGUID"
        instance.checkLog(notCensored) shouldBe null
    }
}
