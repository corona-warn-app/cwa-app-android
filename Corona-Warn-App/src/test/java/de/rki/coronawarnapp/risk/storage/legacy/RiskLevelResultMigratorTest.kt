package de.rki.coronawarnapp.risk.storage.legacy

import android.content.Context
import androidx.core.content.edit
import de.rki.coronawarnapp.risk.RiskState
import de.rki.coronawarnapp.util.TimeStamper
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.runBlocking
import org.joda.time.Instant
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.preferences.MockSharedPreferences

class RiskLevelResultMigratorTest : BaseTest() {

    @MockK lateinit var timeStamper: TimeStamper
    @MockK lateinit var context: Context
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
        encryptedPreferences = { mockPreferences },
        context = context
    )

    @Test
    fun `normal case with full values`() = runBlocking {
        mockPreferences.edit {
            putInt("preference_risk_level_score", MigrationRiskLevelConstants.INCREASED_RISK)
            putInt("preference_risk_level_score_successful", MigrationRiskLevelConstants.LOW_LEVEL_RISK)
            putLong("preference_timestamp_risk_level_calculation", 1234567890L)
        }
        createInstance().apply {
            val legacyResults = getLegacyResults()
            legacyResults[0].apply {
                riskState shouldBe RiskState.INCREASED_RISK
                calculatedAt shouldBe Instant.ofEpochMilli(1234567890L)
            }
            legacyResults[1].apply {
                riskState shouldBe RiskState.LOW_RISK
                calculatedAt shouldBe Instant.EPOCH.plus(1337)
            }
        }
    }

    @Test
    fun `empty list if no previous data was available`() = runBlocking {
        mockPreferences.dataMapPeek.isEmpty() shouldBe true
        createInstance().getLegacyResults() shouldBe emptyList()
    }

    @Test
    fun `if no timestamp is available we use the current time`() = runBlocking {
        mockPreferences.edit {
            putInt("preference_risk_level_score", MigrationRiskLevelConstants.INCREASED_RISK)
            putInt("preference_risk_level_score_successful", MigrationRiskLevelConstants.LOW_LEVEL_RISK)
        }
        createInstance().apply {
            val legacyResults = getLegacyResults()
            legacyResults[0].apply {
                riskState shouldBe RiskState.INCREASED_RISK
                calculatedAt shouldBe Instant.EPOCH.plus(1337)
            }
            legacyResults[1].apply {
                riskState shouldBe RiskState.LOW_RISK
                calculatedAt shouldBe Instant.EPOCH.plus(1337)
            }
        }
    }

    @Test
    fun `last successful is null`() = runBlocking {
        mockPreferences.edit {
            putInt("preference_risk_level_score_successful", MigrationRiskLevelConstants.INCREASED_RISK)
        }
        createInstance().apply {
            val legacyResults = getLegacyResults()
            legacyResults.size shouldBe 1
            legacyResults.first().apply {
                riskState shouldBe RiskState.INCREASED_RISK
                calculatedAt shouldBe Instant.EPOCH.plus(1337)
            }
        }
    }

    @Test
    fun `last successfully calculated is null`() = runBlocking {
        mockPreferences.edit {
            putInt("preference_risk_level_score", MigrationRiskLevelConstants.INCREASED_RISK)
            putLong("preference_timestamp_risk_level_calculation", 1234567890L)
        }
        createInstance().apply {
            val legacyResults = getLegacyResults()
            legacyResults.size shouldBe 1
            legacyResults.first().apply {
                riskState shouldBe RiskState.INCREASED_RISK
                calculatedAt shouldBe Instant.ofEpochMilli(1234567890L)
            }
        }
    }

    @Test
    fun `exceptions are handled gracefully`() = runBlocking {
        mockPreferences.edit {
            putInt("preference_risk_level_score", MigrationRiskLevelConstants.INCREASED_RISK)
        }
        every { timeStamper.nowUTC } throws Exception("Surprise!")
        createInstance().getLegacyResults() shouldBe emptyList()
    }

    @Test
    fun `legacy risk level mapping`() {
        RiskLevelResultMigrator.mapRiskLevelConstant(
            MigrationRiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF
        ) shouldBe RiskState.CALCULATION_FAILED

        RiskLevelResultMigrator.mapRiskLevelConstant(
            MigrationRiskLevelConstants.LOW_LEVEL_RISK
        ) shouldBe RiskState.LOW_RISK

        RiskLevelResultMigrator.mapRiskLevelConstant(
            MigrationRiskLevelConstants.INCREASED_RISK
        ) shouldBe RiskState.INCREASED_RISK

        RiskLevelResultMigrator.mapRiskLevelConstant(
            MigrationRiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS
        ) shouldBe RiskState.CALCULATION_FAILED

        RiskLevelResultMigrator.mapRiskLevelConstant(
            MigrationRiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS_MANUAL
        ) shouldBe RiskState.CALCULATION_FAILED

        RiskLevelResultMigrator.mapRiskLevelConstant(
            MigrationRiskLevelConstants.UNDETERMINED
        ) shouldBe RiskState.CALCULATION_FAILED
    }
}
