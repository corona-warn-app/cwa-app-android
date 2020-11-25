package de.rki.coronawarnapp.ui.tracing.card

import android.content.Context
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.risk.RiskLevelConstants
import de.rki.coronawarnapp.risk.RiskLevelConstants.INCREASED_RISK
import de.rki.coronawarnapp.risk.RiskLevelConstants.LOW_LEVEL_RISK
import de.rki.coronawarnapp.risk.RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF
import de.rki.coronawarnapp.risk.RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS
import de.rki.coronawarnapp.tracing.GeneralTracingStatus
import de.rki.coronawarnapp.tracing.TracingProgress
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifySequence
import org.joda.time.Instant
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import java.util.Date

class TracingCardStateTest : BaseTest() {

    @MockK(relaxed = true) lateinit var context: Context

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    private fun createInstance(
        tracingStatus: GeneralTracingStatus.Status = mockk(),
        riskLevel: Int = 0,
        tracingProgress: TracingProgress = TracingProgress.Idle,
        riskLevelLastSuccessfulCalculation: Int = 0,
        daysWithEncounters: Int = 0,
        lastEncounterAt: Instant? = null,
        activeTracingDaysInRetentionPeriod: Long = 0,
        lastTimeDiagnosisKeysFetched: Date? = mockk(),
        isBackgroundJobEnabled: Boolean = false
    ) = TracingCardState(
        tracingStatus = tracingStatus,
        riskLevelScore = riskLevel,
        tracingProgress = tracingProgress,
        lastRiskLevelScoreCalculated = riskLevelLastSuccessfulCalculation,
        daysWithEncounters = daysWithEncounters,
        lastEncounterAt = lastEncounterAt,
        activeTracingDaysInRetentionPeriod = activeTracingDaysInRetentionPeriod,
        lastTimeDiagnosisKeysFetched = lastTimeDiagnosisKeysFetched,
        isManualKeyRetrievalEnabled = !isBackgroundJobEnabled
    )

    @Test
    fun `risklevel affects icon color`() {
        createInstance(riskLevel = INCREASED_RISK).apply {
            getStableIconColor(context)
            verify { context.getColor(R.color.colorStableLight) }
        }

        createInstance(riskLevel = UNKNOWN_RISK_OUTDATED_RESULTS).apply {
            getStableIconColor(context)
            verify { context.getColor(R.color.colorTextSemanticNeutral) }
        }

        createInstance(riskLevel = NO_CALCULATION_POSSIBLE_TRACING_OFF).apply {
            getStableIconColor(context)
            verify { context.getColor(R.color.colorTextSemanticNeutral) }
        }

        createInstance(riskLevel = LOW_LEVEL_RISK).apply {
            getStableIconColor(context)
            verify { context.getColor(R.color.colorStableLight) }
        }
    }

    @Test
    fun `risklevel affects riskcolors`() {
        createInstance(riskLevel = INCREASED_RISK).apply {
            getRiskInfoContainerBackgroundTint(context)
            verify { context.getColorStateList(R.color.card_increased) }
        }

        createInstance(riskLevel = UNKNOWN_RISK_OUTDATED_RESULTS).apply {
            getRiskInfoContainerBackgroundTint(context)
            verify { context.getColorStateList(R.color.card_outdated) }
        }

        createInstance(riskLevel = NO_CALCULATION_POSSIBLE_TRACING_OFF).apply {
            getRiskInfoContainerBackgroundTint(context)
            verify { context.getColorStateList(R.color.card_no_calculation) }
        }

        createInstance(riskLevel = LOW_LEVEL_RISK).apply {
            getRiskInfoContainerBackgroundTint(context)
            verify { context.getColorStateList(R.color.card_low) }
        }
    }

    @Test
    fun `risklevel affects risk body text`() {
        createInstance(riskLevel = UNKNOWN_RISK_OUTDATED_RESULTS).apply {
            getRiskBody(context)
            verify { context.getString(R.string.risk_card_outdated_risk_body) }
        }

        createInstance(riskLevel = NO_CALCULATION_POSSIBLE_TRACING_OFF).apply {
            getRiskBody(context)
            verify { context.getString(R.string.risk_card_body_tracing_off) }
        }

        createInstance(riskLevel = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS_MANUAL).apply {
            getRiskBody(context)
            verify { context.getString(R.string.risk_card_outdated_manual_risk_body) }
        }
    }

    @Test
    fun `risklevel affected by tracing status`() {
        createInstance(
            riskLevel = UNKNOWN_RISK_OUTDATED_RESULTS,
            tracingStatus = GeneralTracingStatus.Status.TRACING_INACTIVE
        ).apply {
            getRiskBody(context)
            verify { context.getString(R.string.risk_card_body_tracing_off) }
        }

        createInstance(
            riskLevel = NO_CALCULATION_POSSIBLE_TRACING_OFF,
            tracingStatus = GeneralTracingStatus.Status.TRACING_INACTIVE
        ).apply {
            getRiskBody(context)
            verify { context.getString(R.string.risk_card_body_tracing_off) }
        }

        createInstance(
            riskLevel = LOW_LEVEL_RISK,
            tracingStatus = GeneralTracingStatus.Status.TRACING_INACTIVE
        ).apply {
            getRiskBody(context)
            verify { context.getString(R.string.risk_card_body_tracing_off) }
        }

        createInstance(
            riskLevel = INCREASED_RISK,
            tracingStatus = GeneralTracingStatus.Status.TRACING_INACTIVE
        ).apply {
            getRiskBody(context)
            verify { context.getString(R.string.risk_card_body_tracing_off) }
        }
    }

    @Test
    fun `saved risk body is affected by risklevel`() {
        createInstance(
            riskLevel = INCREASED_RISK,
            riskLevelLastSuccessfulCalculation = 0
        ).apply {
            getSavedRiskBody(context) shouldBe ""
        }

        createInstance(
            riskLevel = UNKNOWN_RISK_OUTDATED_RESULTS,
            riskLevelLastSuccessfulCalculation = 0
        ).apply {
            getSavedRiskBody(context) shouldBe ""
        }

        createInstance(
            riskLevel = NO_CALCULATION_POSSIBLE_TRACING_OFF,
            riskLevelLastSuccessfulCalculation = 0
        ).apply {
            getSavedRiskBody(context) shouldBe ""
        }

        createInstance(
            riskLevel = LOW_LEVEL_RISK,
            riskLevelLastSuccessfulCalculation = 0
        ).apply {
            getSavedRiskBody(context) shouldBe ""
        }

        createInstance(
            riskLevel = INCREASED_RISK,
            riskLevelLastSuccessfulCalculation = INCREASED_RISK
        ).apply {
            getSavedRiskBody(context) shouldBe ""
        }

        createInstance(
            riskLevel = INCREASED_RISK,
            riskLevelLastSuccessfulCalculation = UNKNOWN_RISK_OUTDATED_RESULTS
        ).apply {
            getSavedRiskBody(context) shouldBe ""
        }

        createInstance(
            riskLevel = INCREASED_RISK,
            riskLevelLastSuccessfulCalculation = NO_CALCULATION_POSSIBLE_TRACING_OFF
        ).apply {
            getSavedRiskBody(context) shouldBe ""
        }

        createInstance(
            riskLevel = INCREASED_RISK,
            riskLevelLastSuccessfulCalculation = LOW_LEVEL_RISK
        ).apply {
            getSavedRiskBody(context) shouldBe ""
        }

        createInstance(
            riskLevel = NO_CALCULATION_POSSIBLE_TRACING_OFF,
            riskLevelLastSuccessfulCalculation = LOW_LEVEL_RISK
        ).apply {
            getSavedRiskBody(context)
            verify {
                context
                    .getString(R.string.risk_card_no_calculation_possible_body_saved_risk)
                    .format(context.getString(R.string.risk_card_low_risk_headline))
            }
        }

        createInstance(
            riskLevel = NO_CALCULATION_POSSIBLE_TRACING_OFF,
            riskLevelLastSuccessfulCalculation = INCREASED_RISK
        ).apply {
            getSavedRiskBody(context)
            verify {
                context
                    .getString(R.string.risk_card_no_calculation_possible_body_saved_risk)
                    .format(context.getString(R.string.risk_card_increased_risk_headline))
            }
        }

        createInstance(
            riskLevel = UNKNOWN_RISK_OUTDATED_RESULTS,
            riskLevelLastSuccessfulCalculation = LOW_LEVEL_RISK
        ).apply {
            getSavedRiskBody(context)
            verify {
                context
                    .getString(R.string.risk_card_no_calculation_possible_body_saved_risk)
                    .format(context.getString(R.string.risk_card_low_risk_headline))
            }
        }

        createInstance(
            riskLevel = UNKNOWN_RISK_OUTDATED_RESULTS,
            riskLevelLastSuccessfulCalculation = INCREASED_RISK
        ).apply {
            getSavedRiskBody(context)
            verify {
                context
                    .getString(R.string.risk_card_no_calculation_possible_body_saved_risk)
                    .format(context.getString(R.string.risk_card_increased_risk_headline))
            }
        }
    }

    @Test
    fun `risk contact body is affected by risklevel`() {
        createInstance(
            riskLevel = UNKNOWN_RISK_OUTDATED_RESULTS,
            daysWithEncounters = 0
        ).apply {
            getRiskContactBody(context) shouldBe ""
        }

        createInstance(
            riskLevel = NO_CALCULATION_POSSIBLE_TRACING_OFF,
            daysWithEncounters = 0
        ).apply {
            getRiskContactBody(context) shouldBe ""
        }

        createInstance(
            riskLevel = UNKNOWN_RISK_OUTDATED_RESULTS,
            daysWithEncounters = 2
        ).apply {
            getRiskContactBody(context) shouldBe ""
        }

        createInstance(
            riskLevel = NO_CALCULATION_POSSIBLE_TRACING_OFF,
            daysWithEncounters = 2
        ).apply {
            getRiskContactBody(context) shouldBe ""
        }
    }

    @Test
    fun `risk icon formatting`() {
        createInstance(riskLevel = INCREASED_RISK).apply {
            getRiskContactIcon(context)
            verify { context.getDrawable(R.drawable.ic_risk_card_contact_increased) }
        }

        createInstance(riskLevel = UNKNOWN_RISK_OUTDATED_RESULTS).apply {
            getRiskContactIcon(context)
            verify { context.getDrawable(R.drawable.ic_risk_card_contact) }
        }

        createInstance(riskLevel = NO_CALCULATION_POSSIBLE_TRACING_OFF).apply {
            getRiskContactIcon(context)
            verify { context.getDrawable(R.drawable.ic_risk_card_contact) }
        }

        createInstance(riskLevel = LOW_LEVEL_RISK).apply {
            getRiskContactIcon(context)
            verify { context.getDrawable(R.drawable.ic_risk_card_contact) }
        }
    }

    @Test
    fun `last risk contact text formatting`() {
        createInstance(
            riskLevel = UNKNOWN_RISK_OUTDATED_RESULTS
        ).apply {
            getRiskContactLast(context) shouldBe ""
        }

        createInstance(
            riskLevel = NO_CALCULATION_POSSIBLE_TRACING_OFF
        ).apply {
            getRiskContactLast(context) shouldBe ""
        }

        createInstance(
            riskLevel = LOW_LEVEL_RISK
        ).apply {
            getRiskContactLast(context) shouldBe ""
        }
    }

    @Test
    fun `text for active risktracing in retention period`() {
        createInstance(
            riskLevel = INCREASED_RISK,
            activeTracingDaysInRetentionPeriod = 1
        ).apply {
            getRiskActiveTracingDaysInRetentionPeriod(context) shouldBe ""
        }

        createInstance(
            riskLevel = UNKNOWN_RISK_OUTDATED_RESULTS,
            activeTracingDaysInRetentionPeriod = 1
        ).apply {
            getRiskActiveTracingDaysInRetentionPeriod(context) shouldBe ""
        }

        createInstance(
            riskLevel = NO_CALCULATION_POSSIBLE_TRACING_OFF,
            activeTracingDaysInRetentionPeriod = 1
        ).apply {
            getRiskActiveTracingDaysInRetentionPeriod(context) shouldBe ""
        }

        createInstance(
            riskLevel = LOW_LEVEL_RISK,
            activeTracingDaysInRetentionPeriod = 1
        ).apply {
            getRiskActiveTracingDaysInRetentionPeriod(context)
            verify { context.getString(R.string.risk_card_body_saved_days).format(1) }
        }

        createInstance(
            riskLevel = LOW_LEVEL_RISK,
            activeTracingDaysInRetentionPeriod = 2
        ).apply {
            getRiskActiveTracingDaysInRetentionPeriod(context)
            verify { context.getString(R.string.risk_card_body_saved_days).format(2) }
        }
    }

    @Test
    fun `text for last time diagnosis keys were fetched`() {
        val date = Date()
        createInstance(
            riskLevel = INCREASED_RISK,
            riskLevelLastSuccessfulCalculation = 2,
            lastTimeDiagnosisKeysFetched = date
        ).apply {
            getTimeFetched(context)
            verify { context.getString(eq(R.string.risk_card_body_time_fetched), any()) }
        }

        createInstance(
            riskLevel = UNKNOWN_RISK_OUTDATED_RESULTS,
            riskLevelLastSuccessfulCalculation = 2,
            lastTimeDiagnosisKeysFetched = date
        ).apply {
            getTimeFetched(context)
            verify { context.getString(eq(R.string.risk_card_body_time_fetched), any()) }
        }

        createInstance(
            riskLevel = NO_CALCULATION_POSSIBLE_TRACING_OFF,
            riskLevelLastSuccessfulCalculation = 2,
            lastTimeDiagnosisKeysFetched = date
        ).apply {
            getTimeFetched(context)
            verify { context.getString(eq(R.string.risk_card_body_time_fetched), any()) }
        }

        createInstance(
            riskLevel = LOW_LEVEL_RISK,
            riskLevelLastSuccessfulCalculation = 2,
            lastTimeDiagnosisKeysFetched = date
        ).apply {
            getTimeFetched(context)
            verify { context.getString(eq(R.string.risk_card_body_time_fetched), any()) }
        }

        createInstance(
            riskLevel = INCREASED_RISK,
            lastTimeDiagnosisKeysFetched = date
        ).apply {
            getTimeFetched(context)
            verify { context.getString(eq(R.string.risk_card_body_time_fetched), any()) }
        }

        createInstance(
            riskLevel = UNKNOWN_RISK_OUTDATED_RESULTS,
            lastTimeDiagnosisKeysFetched = date
        ).apply {
            getTimeFetched(context) shouldBe ""
        }

        createInstance(
            riskLevel = NO_CALCULATION_POSSIBLE_TRACING_OFF,
            lastTimeDiagnosisKeysFetched = date
        ).apply {
            getTimeFetched(context) shouldBe ""
        }

        createInstance(
            riskLevel = LOW_LEVEL_RISK,
            lastTimeDiagnosisKeysFetched = date
        ).apply {
            getTimeFetched(context)
            verify { context.getString(eq(R.string.risk_card_body_time_fetched), any()) }
        }

        createInstance(
            riskLevel = INCREASED_RISK,
            lastTimeDiagnosisKeysFetched = null
        ).apply {
            getTimeFetched(context)
            verify { context.getString(R.string.risk_card_body_not_yet_fetched) }
        }

        createInstance(
            riskLevel = UNKNOWN_RISK_OUTDATED_RESULTS,
            lastTimeDiagnosisKeysFetched = null
        ).apply {
            getTimeFetched(context) shouldBe ""
        }

        createInstance(
            riskLevel = NO_CALCULATION_POSSIBLE_TRACING_OFF,
            lastTimeDiagnosisKeysFetched = null
        ).apply {
            getTimeFetched(context) shouldBe ""
        }

        createInstance(
            riskLevel = LOW_LEVEL_RISK,
            lastTimeDiagnosisKeysFetched = null
        ).apply {
            getTimeFetched(context)
            verify { context.getString(R.string.risk_card_body_not_yet_fetched) }
        }
    }

    @Test
    fun `task divider is formatted according to riskLevel`() {
        createInstance(riskLevel = INCREASED_RISK).apply {
            getStableDividerColor(context)
            verify { context.getColor(R.color.colorStableHairlineLight) }
        }

        createInstance(riskLevel = UNKNOWN_RISK_OUTDATED_RESULTS).apply {
            getStableDividerColor(context)
            verify { context.getColor(R.color.colorStableHairlineDark) }
        }

        createInstance(riskLevel = NO_CALCULATION_POSSIBLE_TRACING_OFF).apply {
            getStableDividerColor(context)
            verify { context.getColor(R.color.colorStableHairlineDark) }
        }

        createInstance(riskLevel = LOW_LEVEL_RISK).apply {
            getStableDividerColor(context)
            verify { context.getColor(R.color.colorStableHairlineLight) }
        }
    }

    @Test
    fun `tracing button visibility depends on risklevel`() {
        createInstance(riskLevel = INCREASED_RISK).apply {
            showTracingButton() shouldBe false
        }

        createInstance(riskLevel = UNKNOWN_RISK_OUTDATED_RESULTS).apply {
            showTracingButton() shouldBe true
        }

        createInstance(riskLevel = NO_CALCULATION_POSSIBLE_TRACING_OFF).apply {
            showTracingButton() shouldBe true
        }

        createInstance(riskLevel = LOW_LEVEL_RISK).apply {
            showTracingButton() shouldBe false
        }
    }

    @Test
    fun `update button visibility`() {
        createInstance(
            riskLevel = INCREASED_RISK,
            isBackgroundJobEnabled = false
        ).apply {
            showUpdateButton() shouldBe true
        }

        createInstance(
            riskLevel = INCREASED_RISK,
            isBackgroundJobEnabled = true
        ).apply {
            showUpdateButton() shouldBe false
        }

        createInstance(
            riskLevel = UNKNOWN_RISK_OUTDATED_RESULTS,
            isBackgroundJobEnabled = false
        ).apply {
            showUpdateButton() shouldBe false
        }

        createInstance(
            riskLevel = UNKNOWN_RISK_OUTDATED_RESULTS,
            isBackgroundJobEnabled = true
        ).apply {
            showUpdateButton() shouldBe false
        }

        createInstance(
            riskLevel = NO_CALCULATION_POSSIBLE_TRACING_OFF,
            isBackgroundJobEnabled = false
        ).apply {
            showUpdateButton() shouldBe false
        }

        createInstance(
            riskLevel = NO_CALCULATION_POSSIBLE_TRACING_OFF,
            isBackgroundJobEnabled = true
        ).apply {
            showUpdateButton() shouldBe false
        }

        createInstance(
            riskLevel = LOW_LEVEL_RISK,
            isBackgroundJobEnabled = false
        ).apply {
            showUpdateButton() shouldBe true
        }

        createInstance(
            riskLevel = LOW_LEVEL_RISK,
            isBackgroundJobEnabled = true
        ).apply {
            showUpdateButton() shouldBe false
        }
    }

    @Test
    fun `risklevel headline is affected by score`() {
        createInstance(riskLevel = INCREASED_RISK).apply {
            getRiskLevelHeadline(context)
            verify { context.getString(R.string.risk_card_increased_risk_headline) }
        }

        createInstance(riskLevel = UNKNOWN_RISK_OUTDATED_RESULTS).apply {
            getRiskLevelHeadline(context)
            verify { context.getString(R.string.risk_card_outdated_risk_headline) }
        }

        createInstance(riskLevel = NO_CALCULATION_POSSIBLE_TRACING_OFF).apply {
            getRiskLevelHeadline(context)
            verify { context.getString(R.string.risk_card_no_calculation_possible_headline) }
        }

        createInstance(riskLevel = LOW_LEVEL_RISK).apply {
            getRiskLevelHeadline(context)
            verify { context.getString(R.string.risk_card_low_risk_headline) }
        }
    }

    @Test
    fun `tracing progress state`() {
        createInstance(tracingProgress = TracingProgress.Idle).apply {
            getProgressCardHeadline(context)
            getProgressCardBody(context)
            verify(exactly = 0) { context.getString(any()) }
        }
        createInstance(tracingProgress = TracingProgress.Downloading).apply {
            getProgressCardHeadline(context)
            getProgressCardBody(context)
        }
        createInstance(tracingProgress = TracingProgress.ENFIsCalculating).apply {
            getProgressCardHeadline(context)
            getProgressCardBody(context)
        }
        verifySequence {
            context.getString(R.string.risk_card_progress_download_headline)
            context.getString(R.string.risk_card_progress_download_body)
            context.getString(R.string.risk_card_progress_calculation_headline)
            context.getString(R.string.risk_card_progress_calculation_body)
        }
    }
}
