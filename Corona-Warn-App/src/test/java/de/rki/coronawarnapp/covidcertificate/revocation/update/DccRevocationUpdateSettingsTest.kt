package de.rki.coronawarnapp.covidcertificate.revocation.update

import de.rki.coronawarnapp.util.TimeAndDateExtensions.seconds
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runBlockingTest
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach

import org.junit.jupiter.api.Test

import testhelpers.BaseTest
import testhelpers.preferences.FakeDataStore

internal class DccRevocationUpdateSettingsTest : BaseTest() {

    private val fakeDataStore = FakeDataStore()
    private lateinit var settingsDcc: DccRevocationUpdateSettings

    @BeforeEach
    fun setup() {
        settingsDcc = DccRevocationUpdateSettings(fakeDataStore)
    }

    @Test
    fun `test RevocationUpdateSettings - set last update value`() =
        runBlockingTest {
            settingsDcc.getLastUpdateTime() shouldBe null

            val now = Instant.parse("2022-04-14T00:00:00.000Z")
            settingsDcc.setUpdateTimeToNow(now)

            fakeDataStore[DccRevocationUpdateSettings.LAST_UPDATE_TIME_KEY] shouldBe now.seconds
            settingsDcc.getLastUpdateTime() shouldBe now
        }
}
