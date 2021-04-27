package de.rki.coronawarnapp.coronatest.type.rapidantigen

import de.rki.coronawarnapp.appconfig.CoronaTestConfig
import de.rki.coronawarnapp.util.TimeStamper
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runBlockingTest
import org.joda.time.Duration
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class RapidAntigenCoronaTestExtensionsTest : BaseTest() {
    @MockK lateinit var coronaTestConfig: CoronaTestConfig
    @MockK lateinit var timeStamper: TimeStamper

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { timeStamper.nowUTC } returns Instant.ofEpochMilli(1010010101)
        every { coronaTestConfig.coronaRapidAntigenTestParameters.hoursToDeemTestOutdated } returns
            Duration.standardHours(48)
    }

    @Test
    fun `state determination, unregistered test`() = runBlockingTest {
        val test: RACoronaTest? = null
        test.toSubmissionState(timeStamper.nowUTC, coronaTestConfig) shouldBe SubmissionStateRAT.NoTest
    }
}
