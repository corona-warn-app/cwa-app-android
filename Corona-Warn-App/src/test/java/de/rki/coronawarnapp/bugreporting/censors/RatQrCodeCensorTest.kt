package de.rki.coronawarnapp.bugreporting.censors

import de.rki.coronawarnapp.bugreporting.censors.submission.RatQrCodeCensor
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import kotlinx.coroutines.test.runBlockingTest
import org.joda.time.LocalDate
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class RatQrCodeCensorTest {

    private val testHash = "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad"
    private val testRawString = "testRawString"

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @AfterEach
    fun teardown() {
        RatQrCodeCensor.dataToCensor = null
    }

    private fun createInstance() = RatQrCodeCensor()

    @Test
    fun `checkLog() should return censored LogLine`() = runBlockingTest {
        RatQrCodeCensor.dataToCensor = RatQrCodeCensor.CensorData(
            rawString = testRawString,
            hash = testHash,
            firstName = "Milhouse",
            lastName = "Van Houten",
            dateOfBirth = LocalDate.parse("1980-07-01")
        )

        val censor = createInstance()

        val logLineToCensor =
            "Here comes the hash: $testHash of the rat test of Milhouse Van Houten. He was born on 1980-07-01"

        censor.checkLog(logLineToCensor)!!
            .compile()!!.censored shouldBe "Here comes the hash: SHA256HASH-ENDING-WITH-15ad of the rat test of RATest/FirstName RATest/LastName. He was born on RATest/DateOfBirth"
    }

    @Test
    fun `checkLog() should return null if no data to censor was set`() = runBlockingTest {
        val censor = createInstance()

        val logLineNotToCensor = "Here comes the hash: $testHash"

        censor.checkLog(logLineNotToCensor) shouldBe null
    }

    @Test
    fun `checkLog() should return null if nothing should be censored`() = runBlockingTest {
        RatQrCodeCensor.dataToCensor = RatQrCodeCensor.CensorData(
            rawString = testRawString,
            hash = testHash.replace("8", "9"),
            firstName = null,
            lastName = null,
            dateOfBirth = null
        )

        val censor = createInstance()

        val logLineNotToCensor = "Here comes the hash: $testHash"

        censor.checkLog(logLineNotToCensor) shouldBe null
    }
}
