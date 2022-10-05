package de.rki.coronawarnapp.covidcertificate.validation.core.settings

import de.rki.coronawarnapp.util.TimeStamper
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.preferences.FakeDataStore
import java.time.Instant

internal class DccValidationSettingsTest : BaseTest() {

    @MockK lateinit var timeStamper: TimeStamper

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        every { timeStamper.nowUTC } returns Instant.ofEpochMilli(2000L)
    }

    @Test
    fun getSettings() = runTest {
        instance().settings.first() shouldBe ("DE" to 2000L)
    }

    @Test
    fun updateDccValidationCountry() = runTest {
        instance().apply {
            settings.first() shouldBe ("DE" to 2000L)
            updateDccValidationCountry("AT")
            settings.first() shouldBe ("AT" to 2000L)
        }
    }

    @Test
    fun updateDccValidationTime() = runTest {
        instance().apply {
            settings.first() shouldBe ("DE" to 2000L)
            updateDccValidationTime(3000L)
            settings.first() shouldBe ("DE" to 3000L)
        }
    }

    @Test
    fun reset() = runTest {
        instance().apply {
            updateDccValidationTime(0L)
            settings.first() shouldBe ("DE" to 0L)
            reset()
            settings.first() shouldBe ("DE" to 2000L)
        }
    }

    fun instance() = DccValidationSettings(
        FakeDataStore(),
        timeStamper
    )
}
