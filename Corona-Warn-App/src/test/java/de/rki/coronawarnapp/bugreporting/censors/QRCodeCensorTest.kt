package de.rki.coronawarnapp.bugreporting.censors

import de.rki.coronawarnapp.bugreporting.debuglog.LogLine
import de.rki.coronawarnapp.util.CWADebug
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockkObject
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class QRCodeCensorTest : BaseTest() {

    private val testGUID = "63b4d3ff-e0de-4bd4-90c1-17c2bb683a2f"

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        mockkObject(CWADebug)
        every { CWADebug.isDeviceForTestersBuild } returns false
    }

    @AfterEach
    fun teardown() {
        QRCodeCensor.lastGUID = null
        clearAllMocks()
    }

    private fun createInstance() = QRCodeCensor()

    @Test
    fun `censoring replaces the logline message`() = runBlockingTest {
        QRCodeCensor.lastGUID = testGUID
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

        every { CWADebug.isDeviceForTestersBuild } returns true
        instance.checkLog(censored) shouldBe censored.copy(
            message = "I'm a shy qrcode: 63b4d3ff-e0de-4bd4-90c1-17c2bb683a2f"
        )
    }

    @Test
    fun `censoring returns null if there is no match`() = runBlockingTest {
        QRCodeCensor.lastGUID = testGUID.replace("f", "a")
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
        QRCodeCensor.lastGUID = null
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
