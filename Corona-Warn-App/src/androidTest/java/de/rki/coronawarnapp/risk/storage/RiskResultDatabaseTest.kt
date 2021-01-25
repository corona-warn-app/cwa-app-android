package de.rki.coronawarnapp.risk.storage

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.rki.coronawarnapp.risk.RiskLevelResult
import de.rki.coronawarnapp.risk.storage.internal.RiskResultDatabase
import de.rki.coronawarnapp.risk.storage.internal.riskresults.PersistedAggregatedRiskPerDateResult
import de.rki.coronawarnapp.risk.storage.internal.riskresults.PersistedRiskLevelResultDao
import de.rki.coronawarnapp.server.protocols.internal.v2.RiskCalculationParametersOuterClass
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.joda.time.Instant
import org.joda.time.LocalDate
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class RiskResultDatabaseTest {
    private val database = RiskResultDatabase.Factory(
        ApplicationProvider.getApplicationContext()
    ).create()
    private val riskResultDao = database.riskResults()

    private val oldestSuccessfulEntry = PersistedRiskLevelResultDao(
        monotonicId = 1,
        id = UUID.randomUUID().toString(),
        calculatedAt = Instant.now().minus(9000),
        aggregatedRiskResult = PersistedRiskLevelResultDao.PersistedAggregatedRiskResult(
            totalRiskLevel = RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping.RiskLevel.HIGH,
            totalMinimumDistinctEncountersWithLowRisk = 2,
            totalMinimumDistinctEncountersWithHighRisk = 23,
            mostRecentDateWithLowRisk = Instant.now().plus(123),
            mostRecentDateWithHighRisk = Instant.now().plus(456),
            numberOfDaysWithLowRisk = 4,
            numberOfDaysWithHighRisk = 5
        ),
        failureReason = null
    )

    private val olderEntryFailedEntry = PersistedRiskLevelResultDao(
        monotonicId = 2,
        id = UUID.randomUUID().toString(),
        calculatedAt = Instant.now().minus(4500),
        aggregatedRiskResult = PersistedRiskLevelResultDao.PersistedAggregatedRiskResult(
            totalRiskLevel = RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping.RiskLevel.HIGH,
            totalMinimumDistinctEncountersWithLowRisk = 2,
            totalMinimumDistinctEncountersWithHighRisk = 23,
            mostRecentDateWithLowRisk = Instant.now().plus(123),
            mostRecentDateWithHighRisk = Instant.now().plus(456),
            numberOfDaysWithLowRisk = 4,
            numberOfDaysWithHighRisk = 5
        ),
        failureReason = RiskLevelResult.FailureReason.TRACING_OFF
    )

    private val newestEntryFailed = PersistedRiskLevelResultDao(
        monotonicId = 3,
        id = UUID.randomUUID().toString(),
        calculatedAt = Instant.now(),
        aggregatedRiskResult = PersistedRiskLevelResultDao.PersistedAggregatedRiskResult(
            totalRiskLevel = RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping.RiskLevel.HIGH,
            totalMinimumDistinctEncountersWithLowRisk = 2,
            totalMinimumDistinctEncountersWithHighRisk = 23,
            mostRecentDateWithLowRisk = Instant.now().plus(123),
            mostRecentDateWithHighRisk = Instant.now().plus(456),
            numberOfDaysWithLowRisk = 4,
            numberOfDaysWithHighRisk = 5
        ),
        failureReason = RiskLevelResult.FailureReason.TRACING_OFF
    )

    @Test
    fun riskResult_CRUD(): Unit = runBlocking {
        database.clearAllTables()

        riskResultDao.apply {
            insertEntry(oldestSuccessfulEntry)
            insertEntry(olderEntryFailedEntry)
            insertEntry(newestEntryFailed)

            allEntries().first() shouldBe listOf(newestEntryFailed, olderEntryFailedEntry, oldestSuccessfulEntry)

            deleteOldest(2)

            allEntries().first() shouldBe listOf(newestEntryFailed, olderEntryFailedEntry)
        }
    }

    @Test
    fun deleteOldestKeepAll(): Unit = runBlocking {
        database.clearAllTables()

        riskResultDao.apply {
            insertEntry(oldestSuccessfulEntry)
            insertEntry(olderEntryFailedEntry)
            insertEntry(newestEntryFailed)

            allEntries().first() shouldBe listOf(newestEntryFailed, olderEntryFailedEntry, oldestSuccessfulEntry)

            deleteOldest(100)

            allEntries().first() shouldBe listOf(newestEntryFailed, olderEntryFailedEntry, oldestSuccessfulEntry)
        }
    }

    @Test
    fun deleteOldestKeepZero(): Unit = runBlocking {
        database.clearAllTables()

        riskResultDao.apply {
            insertEntry(oldestSuccessfulEntry)
            insertEntry(olderEntryFailedEntry)
            insertEntry(newestEntryFailed)

            allEntries().first() shouldBe listOf(newestEntryFailed, olderEntryFailedEntry, oldestSuccessfulEntry)

            deleteOldest(0)

            allEntries().first() shouldBe emptyList()
        }
    }

    @Test
    fun riskResult_latestEntries(): Unit = runBlocking {
        database.clearAllTables()

        riskResultDao.apply {
            insertEntry(olderEntryFailedEntry)
            insertEntry(newestEntryFailed)

            latestEntries(2).first() shouldBe listOf(newestEntryFailed, olderEntryFailedEntry)
        }
    }

    @Test
    fun riskResult_latestAndLastSuccessful(): Unit = runBlocking {
        database.clearAllTables()

        riskResultDao.apply {
            insertEntry(oldestSuccessfulEntry)
            insertEntry(newestEntryFailed)

            latestAndLastSuccessful().first() shouldBe listOf(newestEntryFailed, oldestSuccessfulEntry)
        }
    }

    @Test
    fun aggregatedRiskPerDate_CRUD(): Unit = runBlocking {
        database.clearAllTables()

        val date = LocalDate.parse("2021-01-21")

        val firstResult = PersistedAggregatedRiskPerDateResult(
            dateMillisSinceEpoch = date.toDateTimeAtStartOfDay().millis,
            riskLevel = RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping.RiskLevel.LOW,
            minimumDistinctEncountersWithLowRisk = 10,
            minimumDistinctEncountersWithHighRisk = 0
        )

        val secondResult = PersistedAggregatedRiskPerDateResult(
            dateMillisSinceEpoch = date.minusDays(5).toDateTimeAtStartOfDay().millis,
            riskLevel = RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping.RiskLevel.LOW,
            minimumDistinctEncountersWithLowRisk = 10,
            minimumDistinctEncountersWithHighRisk = 0
        )

        val replaceSecondResult = secondResult.copy(
            riskLevel = RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping.RiskLevel.HIGH,
            minimumDistinctEncountersWithHighRisk = 20
        )

        database.aggregatedRiskPerDate().apply {
            // Proof we are not cheating
            allEntries().first() shouldBe emptyList()

            // Check insert
            insertRisk(listOf(firstResult, secondResult))
            allEntries().first() shouldBe listOf(firstResult, secondResult)

            // Check replace
            insertRisk(listOf(replaceSecondResult))
            val entries = allEntries().first()
            entries shouldNotBe listOf(firstResult, secondResult)
            entries shouldBe listOf(firstResult, replaceSecondResult)

            // Check delete
            delete(listOf(replaceSecondResult))
            allEntries().first() shouldBe listOf(firstResult)
        }
    }
}
