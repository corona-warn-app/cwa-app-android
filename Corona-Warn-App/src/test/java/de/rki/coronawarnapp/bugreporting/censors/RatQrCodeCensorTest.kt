package de.rki.coronawarnapp.bugreporting.censors

import de.rki.coronawarnapp.bugreporting.debuglog.LogLine
import de.rki.coronawarnapp.util.CWADebug
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.mockkObject
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class RatQrCodeCensorTest {

    private val testHash = "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad"
    private val testRawString = "testRawString"

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        mockkObject(CWADebug)
        every { CWADebug.isDeviceForTestersBuild } returns false
    }

    @AfterEach
    fun teardown() {
        RatQrCodeCensor.clearDataToCensor()
    }

    private fun createInstance() = RatQrCodeCensor()

    @Test
    fun `checkLog() should return censored LogLine`() = runBlockingTest {
        RatQrCodeCensor.setDataToCensor(testRawString, testHash)

        val censor = createInstance()

        val logLineToCensor = LogLine(
            timestamp = 1,
            priority = 3,
            message = "Here comes the hash: $testHash",
            tag = "I am tag",
            throwable = null
        )

        censor.checkLog(logLineToCensor) shouldBe logLineToCensor.copy(
            message = "Here comes the hash: SHA256HASH-ENDING-WITH-15ad"
        )

        every { CWADebug.isDeviceForTestersBuild } returns true
        censor.checkLog(logLineToCensor) shouldBe logLineToCensor.copy(
            message = "Here comes the hash: SHA256HASH-ENDING-WITH-61a396177a9cb410ff61f20015ad"
        )
    }

    @Test
    fun `checkLog() should return null if no data to censor was set`() = runBlockingTest {
        val censor = createInstance()

        val logLineNotToCensor = LogLine(
            timestamp = 1,
            priority = 3,
            message = "Here comes the hash: $testHash",
            tag = "I am tag",
            throwable = null
        )

        censor.checkLog(logLineNotToCensor) shouldBe null
    }

    @Test
    fun `checkLog() should return null if nothing should be censored`() = runBlockingTest {
        RatQrCodeCensor.setDataToCensor(testRawString, testHash.replace("8", "9"))

        val censor = createInstance()

        val logLineNotToCensor = LogLine(
            timestamp = 1,
            priority = 3,
            message = "Here comes the hash: $testHash",
            tag = "I am tag",
            throwable = null
        )

        censor.checkLog(logLineNotToCensor) shouldBe null
    }
}
