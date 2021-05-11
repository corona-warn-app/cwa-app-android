package de.rki.coronawarnapp.bugreporting.censors

import de.rki.coronawarnapp.bugreporting.censors.submission.PcrQrCodeCensor
import de.rki.coronawarnapp.bugreporting.debuglog.LogLine
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import kotlinx.coroutines.test.runBlockingTest
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
    fun `censoring replaces the logline message`() = runBlockingTest {
        PcrQrCodeCensor.lastGUID = testGUID
        val instance = createInstance()
        val censored = LogLine(
            timestamp = 1,
            priority = 3,
            message = "I'm a shy qrcode: $testGUID",
            tag = "I'm a tag",
            throwable = null
        )
        instance.checkLog(censored) shouldBe censored.copy(
            message = "I'm a shy qrcode: ########-####-####-####-########3a2f"
        )
    }

    @Test
    fun `censoring returns null if there is no match`() = runBlockingTest {
        PcrQrCodeCensor.lastGUID = testGUID.replace("f", "a")
        val instance = createInstance()
        val notCensored = LogLine(
            timestamp = 1,
            priority = 3,
            message = "I'm a shy qrcode: $testGUID",
            tag = "I'm a tag",
            throwable = null
        )
        instance.checkLog(notCensored) shouldBe null
    }

    @Test
    fun `censoring aborts if no qrcode was set`() = runBlockingTest {
        PcrQrCodeCensor.lastGUID = null
        val instance = createInstance()
        val notCensored = LogLine(
            timestamp = 1,
            priority = 3,
            message = "I'm a shy qrcode: $testGUID",
            tag = "I'm a tag",
            throwable = null
        )
        instance.checkLog(notCensored) shouldBe null
    }
}
