package de.rki.coronawarnapp.risk

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.preferences.FakeDataStore
import kotlinx.coroutines.test.runTest
import java.time.Instant

class RiskLevelSettingsTest : BaseTest() {

    fun createInstance() = RiskLevelSettings(dataStore)
    private val dataStore = FakeDataStore()

    private val instant = Instant.ofEpochMilli(2134412323)

    @Test
    fun `update last used config identifier`() = runTest {
        with(createInstance()) {
            lastUsedConfigIdentifier.first() shouldBe null
            updateLastUsedConfigIdentifier("Banana")
            lastUsedConfigIdentifier.first() shouldBe "Banana"
        }
    }

    @Test
    fun `update ew last change checked riskLevel timestamp`() = runTest {
        with(createInstance()) {
            ewLastChangeCheckedRiskLevelTimestamp.first() shouldBe null
            dataStore[RiskLevelSettings.PKEY_LAST_CHANGE_CHECKED_RISKLEVEL_TIMESTAMP_EW] shouldBe null
            updateEwLastChangeCheckedRiskLevelTimestamp(instant)
            ewLastChangeCheckedRiskLevelTimestamp.first() shouldBe instant
            dataStore[RiskLevelSettings.PKEY_LAST_CHANGE_CHECKED_RISKLEVEL_TIMESTAMP_EW] shouldBe
                instant.toEpochMilli()
            updateEwLastChangeCheckedRiskLevelTimestamp(null)
            dataStore[RiskLevelSettings.PKEY_LAST_CHANGE_CHECKED_RISKLEVEL_TIMESTAMP_EW] shouldBe 0L
        }
    }

    @Test
    fun `update last change checked riskLevel combined timestamp`() = runTest {
        with(createInstance()) {
            lastChangeCheckedRiskLevelCombinedTimestamp.first() shouldBe null
            dataStore[RiskLevelSettings.PKEY_LAST_CHANGE_CHECKED_RISKLEVEL_TIMESTAMP_COMBINED] shouldBe null
            updateLastChangeCheckedRiskLevelCombinedTimestamp(instant)
            lastChangeCheckedRiskLevelCombinedTimestamp.first() shouldBe instant
            dataStore[RiskLevelSettings.PKEY_LAST_CHANGE_CHECKED_RISKLEVEL_TIMESTAMP_COMBINED] shouldBe
                instant.toEpochMilli()
            updateLastChangeCheckedRiskLevelCombinedTimestamp(null)
            dataStore[RiskLevelSettings.PKEY_LAST_CHANGE_CHECKED_RISKLEVEL_TIMESTAMP_COMBINED] shouldBe 0L
        }
    }
}
