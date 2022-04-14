package de.rki.coronawarnapp.covidcertificate.revocation.update

import de.rki.coronawarnapp.util.TimeAndDateExtensions.seconds
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runBlockingTest
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach

import org.junit.jupiter.api.Test

import testhelpers.BaseTest
import testhelpers.preferences.FakeDataStore

internal class RevocationUpdateSettingsTest : BaseTest() {

    private val fakeDataStore = FakeDataStore()
    private lateinit var settings: RevocationUpdateSettings

    @BeforeEach
    fun setup() {
        settings = RevocationUpdateSettings(fakeDataStore)
    }

    @Test
    fun `test RevocationUpdateSettings - set last update value and clear it again`() =
        runBlockingTest {
            settings.getLastUpdateTime() shouldBe null

            val now = Instant.parse("2022-04-14T00:00:00.000Z")
            settings.setUpdateTimeToNow(now)

            fakeDataStore[RevocationUpdateSettings.LAST_UPDATE_TIME_KEY] shouldBe now.seconds
            settings.getLastUpdateTime() shouldBe now

            settings.clear()
            settings.getLastUpdateTime() shouldBe null
        }
}
