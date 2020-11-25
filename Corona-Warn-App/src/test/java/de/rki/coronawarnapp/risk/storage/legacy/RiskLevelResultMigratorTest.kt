package de.rki.coronawarnapp.risk.storage.legacy

import androidx.core.content.edit
import de.rki.coronawarnapp.risk.RiskLevel
import de.rki.coronawarnapp.util.TimeStamper
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.joda.time.Instant
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.preferences.MockSharedPreferences

class RiskLevelResultMigratorTest : BaseTest() {

    @MockK lateinit var timeStamper: TimeStamper
    private val mockPreferences = MockSharedPreferences()

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { timeStamper.nowUTC } returns Instant.EPOCH.plus(1337)
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    fun createInstance() = RiskLevelResultMigrator(
        timeStamper = timeStamper,
        encryptedPreferences = { mockPreferences }
    )

    @Test
    fun `normal case with full values`() {
        mockPreferences.edit {
            putInt("preference_risk_level_score", RiskLevel.INCREASED_RISK.raw)
            putInt("preference_risk_level_score_successful", RiskLevel.LOW_LEVEL_RISK.raw)
            putLong("preference_timestamp_risk_level_calculation", 1234567890L)
        }
        createInstance().apply {
            val legacyResults = getLegacyResults()
            legacyResults[0].apply {
                riskLevel shouldBe RiskLevel.INCREASED_RISK
                calculatedAt shouldBe Instant.ofEpochMilli(1234567890L)
            }
            legacyResults[1].apply {
                riskLevel shouldBe RiskLevel.LOW_LEVEL_RISK
                calculatedAt shouldBe Instant.EPOCH.plus(1337)
            }
        }
    }

    @Test
    fun `empty list if no previous data was available`() {
        mockPreferences.dataMapPeek.isEmpty() shouldBe true
        createInstance().getLegacyResults() shouldBe emptyList()
    }

    @Test
    fun `if no timestamp is available we use the current time`() {
        mockPreferences.edit {
            putInt("preference_risk_level_score", RiskLevel.INCREASED_RISK.raw)
            putInt("preference_risk_level_score_successful", RiskLevel.LOW_LEVEL_RISK.raw)
        }
        createInstance().apply {
            val legacyResults = getLegacyResults()
            legacyResults[0].apply {
                riskLevel shouldBe RiskLevel.INCREASED_RISK
                calculatedAt shouldBe Instant.EPOCH.plus(1337)
            }
            legacyResults[1].apply {
                riskLevel shouldBe RiskLevel.LOW_LEVEL_RISK
                calculatedAt shouldBe Instant.EPOCH.plus(1337)
            }
        }
    }

    @Test
    fun `last successful is null`() {
        mockPreferences.edit {
            putInt("preference_risk_level_score_successful", RiskLevel.INCREASED_RISK.raw)
        }
        createInstance().apply {
            val legacyResults = getLegacyResults()
            legacyResults.size shouldBe 1
            legacyResults.first().apply {
                riskLevel shouldBe RiskLevel.INCREASED_RISK
                calculatedAt shouldBe Instant.EPOCH.plus(1337)
            }
        }
    }

    @Test
    fun `last successfully calculated is null`() {
        mockPreferences.edit {
            putInt("preference_risk_level_score", RiskLevel.INCREASED_RISK.raw)
            putLong("preference_timestamp_risk_level_calculation", 1234567890L)
        }
        createInstance().apply {
            val legacyResults = getLegacyResults()
            legacyResults.size shouldBe 1
            legacyResults.first().apply {
                riskLevel shouldBe RiskLevel.INCREASED_RISK
                calculatedAt shouldBe Instant.ofEpochMilli(1234567890L)
            }
        }
    }

    @Test
    fun `exceptions are handled gracefully`() {
        mockPreferences.edit {
            putInt("preference_risk_level_score", RiskLevel.INCREASED_RISK.raw)
        }
        every { timeStamper.nowUTC } throws Exception("Surprise!")
        createInstance().getLegacyResults() shouldBe emptyList()
    }
}
