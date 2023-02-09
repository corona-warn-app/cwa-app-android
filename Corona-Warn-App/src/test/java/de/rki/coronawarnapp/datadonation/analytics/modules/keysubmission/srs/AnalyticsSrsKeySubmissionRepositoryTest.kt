package de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission.srs

import de.rki.coronawarnapp.datadonation.analytics.storage.AnalyticsSettings
import de.rki.coronawarnapp.presencetracing.risk.PtRiskLevelResult
import de.rki.coronawarnapp.risk.CombinedEwPtRiskLevelResult
import de.rki.coronawarnapp.risk.EwRiskLevelResult
import de.rki.coronawarnapp.risk.LastCombinedRiskResults
import de.rki.coronawarnapp.risk.RiskState
import de.rki.coronawarnapp.risk.storage.RiskLevelStorage
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData
import de.rki.coronawarnapp.srs.core.model.SrsSubmissionType
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.toLocalDateUtc
import de.rki.coronawarnapp.util.toProtoByteString
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import okio.ByteString.Companion.decodeBase64
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import testhelpers.BaseTest
import java.time.Duration
import java.time.Instant

internal class AnalyticsSrsKeySubmissionRepositoryTest : BaseTest() {

    @MockK lateinit var timeStamper: TimeStamper
    @MockK lateinit var analyticsSettings: AnalyticsSettings
    @MockK lateinit var storage: AnalyticsSrsKeySubmissionStorage
    @MockK lateinit var riskLevelStorage: RiskLevelStorage
    @MockK lateinit var ewRiskLevelResult: EwRiskLevelResult
    @MockK lateinit var ptRiskLevelResult: PtRiskLevelResult
    private val now = Instant.now()
    private val base64 = "CAEgAUgBaAFw////////////AXgCgAEG"

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        every { timeStamper.nowUTC } returns Instant.now()
        every { analyticsSettings.analyticsEnabled } returns flowOf(true)
        coEvery { storage.reset() } just Runs
        coEvery { storage.saveSrsPpaData(any()) } just Runs
        coEvery { storage.getSrsPpaData() } returns null

        every { ewRiskLevelResult.riskState } returns RiskState.INCREASED_RISK
        every { ewRiskLevelResult.mostRecentDateAtRiskState } returns now.minus(Duration.ofDays(1))
        every { ptRiskLevelResult.mostRecentDateAtRiskState } returns
            now.minus(Duration.ofDays(1)).toLocalDateUtc()
        every { ptRiskLevelResult.riskState } returns RiskState.LOW_RISK
        every { ewRiskLevelResult.calculatedAt } returns now
        every { ptRiskLevelResult.calculatedAt } returns now
        every { ewRiskLevelResult.wasSuccessfullyCalculated } returns true
        every { ptRiskLevelResult.wasSuccessfullyCalculated } returns true

        val combinedEwPtRiskLevelResult = CombinedEwPtRiskLevelResult(ptRiskLevelResult, ewRiskLevelResult)

        coEvery {
            riskLevelStorage.latestAndLastSuccessfulCombinedEwPtRiskLevelResult
        } returns flowOf(LastCombinedRiskResults(combinedEwPtRiskLevelResult, RiskState.INCREASED_RISK))

        coEvery { riskLevelStorage.allEwRiskLevelResultsWithExposureWindows } returns flowOf(listOf(ewRiskLevelResult))
        coEvery { riskLevelStorage.allEwRiskLevelResults } returns flowOf(listOf(ewRiskLevelResult))
        coEvery { riskLevelStorage.allPtRiskLevelResults } returns flowOf(listOf(ptRiskLevelResult))
        every { ewRiskLevelResult.wasSuccessfullyCalculated } returns true
    }

    @Test
    fun `collectSrsSubmissionAnalytics - no checkins`() = runTest {
        instance().collectSrsSubmissionAnalytics(SrsSubmissionType.SRS_RAPID_PCR, false)
        coVerify {
            storage.saveSrsPpaData(base64)
        }
    }

    @Test
    fun `collectSrsSubmissionAnalytics - no analytics`() = runTest {
        every { analyticsSettings.analyticsEnabled } returns flowOf(false)
        instance().collectSrsSubmissionAnalytics(SrsSubmissionType.SRS_RAPID_PCR, false)
        coVerify(exactly = 0) {
            storage.saveSrsPpaData(base64)
        }
    }

    @Test
    fun `collectSrsSubmissionAnalytics - checkins`() = runTest {
        instance().collectSrsSubmissionAnalytics(SrsSubmissionType.SRS_RAPID_PCR, true)
        coVerify {
            storage.saveSrsPpaData("CAEgAUgBaAFw////////////AXgBgAEG")
        }
    }

    @Test
    fun `srsPpaData nothing is collected`() = runTest {
        instance().srsPpaData() shouldBe null
    }

    @Test
    fun `srsPpaData - collected`() = runTest {
        coEvery { storage.getSrsPpaData() } returns base64
        instance().srsPpaData() shouldBe PpaData.PPAKeySubmissionMetadata.parseFrom(
            base64.decodeBase64()!!.toProtoByteString()
        )
    }

    @Test
    fun reset() = runTest {
        instance().reset()
        coVerify { storage.reset() }
    }

    private fun instance() = AnalyticsSrsKeySubmissionRepository(
        timeStamper = timeStamper,
        analyticsSettings = analyticsSettings,
        storage = storage,
        riskLevelStorage = riskLevelStorage
    )
}
