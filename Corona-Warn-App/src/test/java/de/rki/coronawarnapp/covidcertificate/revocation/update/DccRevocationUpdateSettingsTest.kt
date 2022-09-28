package de.rki.coronawarnapp.covidcertificate.revocation.update

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach

import org.junit.jupiter.api.Test

import testhelpers.BaseTest
import testhelpers.preferences.FakeDataStore
import java.time.Instant

internal class DccRevocationUpdateSettingsTest : BaseTest() {

    private val fakeDataStore = FakeDataStore()
    private lateinit var settings: DccRevocationUpdateSettings

    @BeforeEach
    fun setup() {
        settings = DccRevocationUpdateSettings(fakeDataStore)
    }

    @Test
    fun `test RevocationUpdateSettings - set last update value`() =
        runTest {
            settings.getLastUpdateTime() shouldBe null

            val now = Instant.parse("2022-04-14T00:00:00.000Z")
            settings.setUpdateTimeToNow(now)

            fakeDataStore[DccRevocationUpdateSettings.LAST_UPDATE_TIME_KEY] shouldBe now.epochSecond
            settings.getLastUpdateTime() shouldBe now
        }
}
