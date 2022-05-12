package de.rki.coronawarnapp.bugreporting.censors

import de.rki.coronawarnapp.bugreporting.censors.submission.PcrTeleTanCensor
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class PcrTeleTanCensorTest : BaseTest() {

    private val testTans = listOf(
        "WF894R5XX5",
        "XJYNJU3MTB",
        "2MU6N6JRE5",
        "ZX3EWW4JX7",
        "5ARBA4W2NC",
        "FQEKD78DVC",
        "WBNNPG3HGF",
        "E856RHPKY9",
    )

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    @AfterEach
    fun teardown() {
        runBlocking { PcrTeleTanCensor.clearTans() }
    }

    private fun createInstance() = PcrTeleTanCensor()

    @Test
    fun `censoring replaces the logline message`() = runTest {
        val instance = createInstance()

        testTans.forEach {
            PcrTeleTanCensor.addTan(it)
            val toCensor = "I'm a shy teletan: $it"
            instance.checkLog(toCensor)!!
                .compile()!!.censored shouldBe "I'm a shy teletan: #######${it.takeLast(3)}"
        }
    }

    @Test
    fun `censoring replaces the logline message - multiple instances`() = runTest {
        testTans.forEach {
            PcrTeleTanCensor.addTan(it)
            val toCensor = "I'm a shy teletan: $it"
            createInstance().checkLog(toCensor)!!
                .compile()!!.censored shouldBe "I'm a shy teletan: #######${it.takeLast(3)}"
        }
    }

    @Test
    fun `censoring returns null if there is no match`() = runTest {
        val instance = createInstance()

        testTans.map { it.substring(2) }.forEach {
            PcrTeleTanCensor.addTan(it)
            val toCensor = "I'm a shy teletan: $it"
            instance.checkLog(toCensor)!!
                .compile()!!.censored shouldBe "I'm a shy teletan: #######${it.takeLast(3)}"
        }
    }

    @Test
    fun `censoring aborts if no teletan was set`() = runTest {
        val instance = createInstance()

        testTans.forEach {
            val toCensor = "I'm a shy teletan: $it"
            instance.checkLog(toCensor) shouldBe null
        }
    }
}
