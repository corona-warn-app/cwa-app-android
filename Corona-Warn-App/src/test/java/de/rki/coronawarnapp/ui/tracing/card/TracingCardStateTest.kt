package de.rki.coronawarnapp.ui.tracing.card

import android.content.Context
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.risk.RiskLevelConstants
import de.rki.coronawarnapp.risk.RiskLevelConstants.INCREASED_RISK
import de.rki.coronawarnapp.risk.RiskLevelConstants.LOW_LEVEL_RISK
import de.rki.coronawarnapp.risk.RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF
import de.rki.coronawarnapp.risk.RiskLevelConstants.UNKNOWN_RISK_INITIAL
import de.rki.coronawarnapp.risk.RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS
import de.rki.coronawarnapp.tracing.GeneralTracingStatus
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
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
        isRefreshing: Boolean = false,
        riskLevelLastSuccessfulCalculation: Int = 0,
        matchedKeyCount: Int = 0,
        daysSinceLastExposure: Int = 0,
        activeTracingDaysInRetentionPeriod: Long = 0,
        lastTimeDiagnosisKeysFetched: Date? = mockk(),
        isBackgroundJobEnabled: Boolean = false,
        isManualKeyRetrievalEnabled: Boolean = false,
        manualKeyRetrievalTime: Long = 0L
    ) = TracingCardState(
        tracingStatus = tracingStatus,
        riskLevelScore = riskLevel,
        isRefreshing = isRefreshing,
        lastRiskLevelScoreCalculated = riskLevelLastSuccessfulCalculation,
        matchedKeyCount = matchedKeyCount,
        daysSinceLastExposure = daysSinceLastExposure,
        activeTracingDaysInRetentionPeriod = activeTracingDaysInRetentionPeriod,
        lastTimeDiagnosisKeysFetched = lastTimeDiagnosisKeysFetched,
        isBackgroundJobEnabled = isBackgroundJobEnabled,
        isManualKeyRetrievalEnabled = isManualKeyRetrievalEnabled,
        manualKeyRetrievalTime = manualKeyRetrievalTime
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

        createInstance(riskLevel = UNKNOWN_RISK_INITIAL).apply {
            getStableIconColor(context)
            verify { context.getColor(R.color.colorStableLight) }
        }
    }

    @Test
    fun `risklevel affects riskcolors`() {
        createInstance(riskLevel = INCREASED_RISK).apply {
            getRiskColorStateList(context)
            verify { context.getColorStateList(R.color.card_increased) }
        }

        createInstance(riskLevel = UNKNOWN_RISK_OUTDATED_RESULTS).apply {
            getRiskColorStateList(context)
            verify { context.getColorStateList(R.color.card_outdated) }
        }

        createInstance(riskLevel = NO_CALCULATION_POSSIBLE_TRACING_OFF).apply {
            getRiskColorStateList(context)
            verify { context.getColorStateList(R.color.card_no_calculation) }
        }

        createInstance(riskLevel = LOW_LEVEL_RISK).apply {
            getRiskColorStateList(context)
            verify { context.getColorStateList(R.color.card_low) }
        }

        createInstance(riskLevel = UNKNOWN_RISK_INITIAL).apply {
            getRiskColorStateList(context)
            verify { context.getColorStateList(R.color.card_unknown) }
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

        createInstance(riskLevel = UNKNOWN_RISK_INITIAL).apply {
            getRiskBody(context)
            verify { context.getString(R.string.risk_card_unknown_risk_body) }
        }

        createInstance(riskLevel = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS_MANUAL).apply {
            getRiskBody(context)
            verify { context.getString(R.string.risk_card_outdated_manual_risk_body) }
        }

        createInstance(riskLevel = UNKNOWN_RISK_INITIAL).apply {
            getRiskBody(context) shouldBe ""
        }
    }

    @Test
    fun `saved risk body is affected by risklevel`() {
        createInstance(
            riskLevel = INCREASED_RISK,
            riskLevelLastSuccessfulCalculation = 0).apply {
            getSavedRiskBody(context) shouldBe ""
        }

        createInstance(
            riskLevel = UNKNOWN_RISK_OUTDATED_RESULTS,
            riskLevelLastSuccessfulCalculation = 0).apply {
            getSavedRiskBody(context) shouldBe ""
        }

        createInstance(
            riskLevel = NO_CALCULATION_POSSIBLE_TRACING_OFF,
            riskLevelLastSuccessfulCalculation = 0).apply {
            getSavedRiskBody(context) shouldBe ""
        }

        createInstance(
            riskLevel = LOW_LEVEL_RISK,
            riskLevelLastSuccessfulCalculation = 0).apply {
            getSavedRiskBody(context) shouldBe ""
        }

        createInstance(
            riskLevel = UNKNOWN_RISK_INITIAL,
            riskLevelLastSuccessfulCalculation = 0).apply {
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
            riskLevel = INCREASED_RISK,
            riskLevelLastSuccessfulCalculation = UNKNOWN_RISK_INITIAL
        ).apply {
            getSavedRiskBody(context) shouldBe ""
        }

        createInstance(
            riskLevel = NO_CALCULATION_POSSIBLE_TRACING_OFF,
            riskLevelLastSuccessfulCalculation = LOW_LEVEL_RISK
        ).apply {
            getSavedRiskBody(context)
            verify { context
                .getString(R.string.risk_card_no_calculation_possible_body_saved_risk)
                .format(context.getString(R.string.risk_card_low_risk_headline)) }
        }

        createInstance(
            riskLevel = NO_CALCULATION_POSSIBLE_TRACING_OFF,
            riskLevelLastSuccessfulCalculation = INCREASED_RISK
        ).apply {
            getSavedRiskBody(context)
            verify { context
                .getString(R.string.risk_card_no_calculation_possible_body_saved_risk)
                .format(context.getString(R.string.risk_card_increased_risk_headline)) }
        }

        createInstance(
            riskLevel = NO_CALCULATION_POSSIBLE_TRACING_OFF,
            riskLevelLastSuccessfulCalculation = UNKNOWN_RISK_INITIAL
        ).apply {
            getSavedRiskBody(context)
            verify { context
                .getString(R.string.risk_card_no_calculation_possible_body_saved_risk)
                .format(context.getString(R.string.risk_card_unknown_risk_headline)) }
        }

        createInstance(
            riskLevel = UNKNOWN_RISK_OUTDATED_RESULTS,
            riskLevelLastSuccessfulCalculation = LOW_LEVEL_RISK
        ).apply {
            getSavedRiskBody(context)
            verify { context
                .getString(R.string.risk_card_no_calculation_possible_body_saved_risk)
                .format(context.getString(R.string.risk_card_low_risk_headline)) }
        }

        createInstance(
            riskLevel = UNKNOWN_RISK_OUTDATED_RESULTS,
            riskLevelLastSuccessfulCalculation = INCREASED_RISK
        ).apply {
            getSavedRiskBody(context)
            verify { context
                .getString(R.string.risk_card_no_calculation_possible_body_saved_risk)
                .format(context.getString(R.string.risk_card_increased_risk_headline)) }
        }

        createInstance(
            riskLevel = UNKNOWN_RISK_OUTDATED_RESULTS,
            riskLevelLastSuccessfulCalculation = UNKNOWN_RISK_INITIAL
        ).apply {
            getSavedRiskBody(context)
            verify { context
                .getString(R.string.risk_card_no_calculation_possible_body_saved_risk)
                .format(context.getString(R.string.risk_card_unknown_risk_headline)) }
        }
    }

    @Test
    fun `risk contact body is affected by risklevel`() {
        createInstance(
            riskLevel = INCREASED_RISK,
            matchedKeyCount = 0).apply {
            getRiskContactBody(context)
            verify { context.getString(R.string.risk_card_body_contact) }
        }

        createInstance(
            riskLevel = INCREASED_RISK,
            matchedKeyCount = 2).apply {
            getRiskContactBody(context)
            verify { context.resources.getQuantityString(
                R.plurals.risk_card_body_contact_value_high_risk,
                2,
                2)
            }
        }

        createInstance(
            riskLevel = LOW_LEVEL_RISK,
            matchedKeyCount = 0).apply {
            getRiskContactBody(context)
            verify { context.getString(R.string.risk_card_body_contact) }
        }

        createInstance(
            riskLevel = LOW_LEVEL_RISK,
            matchedKeyCount = 2).apply {
            getRiskContactBody(context)
            verify { context.resources.getQuantityString(
                R.plurals.risk_card_body_contact_value,
                2,
                2)
            }
        }

        createInstance(
            riskLevel = UNKNOWN_RISK_OUTDATED_RESULTS,
            matchedKeyCount = 0).apply {
            getRiskContactBody(context) shouldBe ""
        }

        createInstance(
            riskLevel = NO_CALCULATION_POSSIBLE_TRACING_OFF,
            matchedKeyCount = 0).apply {
            getRiskContactBody(context) shouldBe ""
        }

        createInstance(
            riskLevel = UNKNOWN_RISK_INITIAL,
            matchedKeyCount = 0).apply {
            getRiskContactBody(context) shouldBe ""
        }

        createInstance(
            riskLevel = UNKNOWN_RISK_OUTDATED_RESULTS,
            matchedKeyCount = 2).apply {
            getRiskContactBody(context) shouldBe ""
        }

        createInstance(
            riskLevel = NO_CALCULATION_POSSIBLE_TRACING_OFF,
            matchedKeyCount = 2).apply {
            getRiskContactBody(context) shouldBe ""
        }

        createInstance(
            riskLevel = UNKNOWN_RISK_INITIAL,
            matchedKeyCount = 2).apply {
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

        createInstance(riskLevel = UNKNOWN_RISK_INITIAL).apply {
            getRiskContactIcon(context)
            verify { context.getDrawable(R.drawable.ic_risk_card_contact) }
        }
    }

    @Test
    fun `last risk contact text formatting`() {
        createInstance(
            riskLevel = INCREASED_RISK,
            daysSinceLastExposure = 2).apply {
            getRiskContactLast(context)
            verify { context.resources.getQuantityString(
                R.plurals.risk_card_increased_risk_body_contact_last,
                2,
                2)
            }
        }

        createInstance(
            riskLevel = INCREASED_RISK,
            daysSinceLastExposure = 0).apply {
            getRiskContactLast(context)
            verify { context.resources.getQuantityString(
                R.plurals.risk_card_increased_risk_body_contact_last,
                0,
                0)
            }
        }

        createInstance(
            riskLevel = UNKNOWN_RISK_OUTDATED_RESULTS,
            daysSinceLastExposure = 2).apply {
            getRiskContactLast(context) shouldBe ""
        }

        createInstance(
            riskLevel = NO_CALCULATION_POSSIBLE_TRACING_OFF,
            daysSinceLastExposure = 2).apply {
            getRiskContactLast(context) shouldBe ""
        }

        createInstance(
            riskLevel = LOW_LEVEL_RISK,
            daysSinceLastExposure = 2).apply {
            getRiskContactLast(context) shouldBe ""
        }

        createInstance(
            riskLevel = UNKNOWN_RISK_INITIAL,
            daysSinceLastExposure = 2).apply {
            getRiskContactLast(context) shouldBe ""
        }
    }

    @Test
    fun `text for active risktracing in retention period`() {
        createInstance(
            riskLevel = INCREASED_RISK,
            activeTracingDaysInRetentionPeriod = 1).apply {
            getRiskActiveTracingDaysInRetentionPeriod(context) shouldBe ""
        }

        createInstance(
            riskLevel = UNKNOWN_RISK_OUTDATED_RESULTS,
            activeTracingDaysInRetentionPeriod = 1).apply {
            getRiskActiveTracingDaysInRetentionPeriod(context) shouldBe ""
        }

        createInstance(
            riskLevel = NO_CALCULATION_POSSIBLE_TRACING_OFF,
            activeTracingDaysInRetentionPeriod = 1).apply {
            getRiskActiveTracingDaysInRetentionPeriod(context) shouldBe ""
        }

        createInstance(
            riskLevel = LOW_LEVEL_RISK,
            activeTracingDaysInRetentionPeriod = 1).apply {
            getRiskActiveTracingDaysInRetentionPeriod(context)
            verify { context.getString(R.string.risk_card_body_saved_days).format(1) }
        }

        createInstance(
            riskLevel = LOW_LEVEL_RISK,
            activeTracingDaysInRetentionPeriod = 2).apply {
            getRiskActiveTracingDaysInRetentionPeriod(context)
            verify { context.getString(R.string.risk_card_body_saved_days).format(2) }
        }

        createInstance(
            riskLevel = UNKNOWN_RISK_INITIAL,
            activeTracingDaysInRetentionPeriod = 1).apply {
            getRiskActiveTracingDaysInRetentionPeriod(context) shouldBe ""
        }
    }

    @Test
    fun `text for last time diagnosis keys were fetched`() {
        val date = Date()
        createInstance(
            riskLevel = INCREASED_RISK,
            riskLevelLastSuccessfulCalculation = 2,
            lastTimeDiagnosisKeysFetched = date).apply {
            getTimeFetched(context)
            verify { context.getString(eq(R.string.risk_card_body_time_fetched), any()) }
        }

        createInstance(
            riskLevel = UNKNOWN_RISK_OUTDATED_RESULTS,
            riskLevelLastSuccessfulCalculation = 2,
            lastTimeDiagnosisKeysFetched = date).apply {
            getTimeFetched(context)
            verify { context.getString(eq(R.string.risk_card_body_time_fetched), any()) }
        }

        createInstance(
            riskLevel = NO_CALCULATION_POSSIBLE_TRACING_OFF,
            riskLevelLastSuccessfulCalculation = 2,
            lastTimeDiagnosisKeysFetched = date).apply {
            getTimeFetched(context)
            verify { context.getString(eq(R.string.risk_card_body_time_fetched), any()) }
        }

        createInstance(
            riskLevel = LOW_LEVEL_RISK,
            riskLevelLastSuccessfulCalculation = 2,
            lastTimeDiagnosisKeysFetched = date).apply {
            getTimeFetched(context)
            verify { context.getString(eq(R.string.risk_card_body_time_fetched), any()) }
        }

        createInstance(
            riskLevel = UNKNOWN_RISK_INITIAL,
            riskLevelLastSuccessfulCalculation = 2,
            lastTimeDiagnosisKeysFetched = date).apply {
            getTimeFetched(context) shouldBe ""
        }

        createInstance(
            riskLevel = INCREASED_RISK,
            lastTimeDiagnosisKeysFetched = date).apply {
            getTimeFetched(context)
            verify { context.getString(eq(R.string.risk_card_body_time_fetched), any()) }
        }

        createInstance(
            riskLevel = UNKNOWN_RISK_OUTDATED_RESULTS,
            lastTimeDiagnosisKeysFetched = date).apply {
            getTimeFetched(context) shouldBe ""
        }

        createInstance(
            riskLevel = NO_CALCULATION_POSSIBLE_TRACING_OFF,
            lastTimeDiagnosisKeysFetched = date).apply {
            getTimeFetched(context) shouldBe ""
        }

        createInstance(
            riskLevel = LOW_LEVEL_RISK,
            lastTimeDiagnosisKeysFetched = date).apply {
            getTimeFetched(context)
            verify { context.getString(eq(R.string.risk_card_body_time_fetched), any()) }
        }

        createInstance(
            riskLevel = UNKNOWN_RISK_INITIAL,
            lastTimeDiagnosisKeysFetched = date).apply {
            getTimeFetched(context) shouldBe ""
        }

        createInstance(
            riskLevel = INCREASED_RISK,
            lastTimeDiagnosisKeysFetched = null).apply {
            getTimeFetched(context)
            verify { context.getString(R.string.risk_card_body_not_yet_fetched) }
        }

        createInstance(
            riskLevel = UNKNOWN_RISK_OUTDATED_RESULTS,
            lastTimeDiagnosisKeysFetched = null).apply {
            getTimeFetched(context) shouldBe ""
        }

        createInstance(
            riskLevel = NO_CALCULATION_POSSIBLE_TRACING_OFF,
            lastTimeDiagnosisKeysFetched = null).apply {
            getTimeFetched(context) shouldBe ""
        }

        createInstance(
            riskLevel = LOW_LEVEL_RISK,
            lastTimeDiagnosisKeysFetched = null).apply {
            getTimeFetched(context)
            verify { context.getString(R.string.risk_card_body_not_yet_fetched) }
        }

        createInstance(
            riskLevel = UNKNOWN_RISK_INITIAL,
            lastTimeDiagnosisKeysFetched = null).apply {
            getTimeFetched(context) shouldBe ""
        }
    }

    @Test
    fun `text for next update time`() {
        createInstance(
            riskLevel = INCREASED_RISK,
            isBackgroundJobEnabled = false).apply {
            getNextUpdate(context) shouldBe ""
        }

        createInstance(
            riskLevel = UNKNOWN_RISK_OUTDATED_RESULTS,
            isBackgroundJobEnabled = false).apply {
            getNextUpdate(context) shouldBe ""
        }

        createInstance(
            riskLevel = NO_CALCULATION_POSSIBLE_TRACING_OFF,
            isBackgroundJobEnabled = false).apply {
            getNextUpdate(context) shouldBe ""
        }

        createInstance(
            riskLevel = LOW_LEVEL_RISK,
            isBackgroundJobEnabled = false).apply {
            getNextUpdate(context) shouldBe ""
        }

        createInstance(
            riskLevel = UNKNOWN_RISK_INITIAL,
            isBackgroundJobEnabled = false).apply {
            getNextUpdate(context) shouldBe ""
        }

        createInstance(
            riskLevel = INCREASED_RISK,
            isBackgroundJobEnabled = true).apply {
            getNextUpdate(context)
            verify { context.getString(R.string.risk_card_body_next_update) }
        }

        createInstance(
            riskLevel = UNKNOWN_RISK_OUTDATED_RESULTS,
            isBackgroundJobEnabled = true).apply {
            getNextUpdate(context) shouldBe ""
        }

        createInstance(
            riskLevel = NO_CALCULATION_POSSIBLE_TRACING_OFF,
            isBackgroundJobEnabled = true).apply {
            getNextUpdate(context) shouldBe ""
        }

        createInstance(
            riskLevel = LOW_LEVEL_RISK,
            isBackgroundJobEnabled = true).apply {
            getNextUpdate(context)
            verify { context.getString(R.string.risk_card_body_next_update) }
        }

        createInstance(
            riskLevel = UNKNOWN_RISK_INITIAL,
            isBackgroundJobEnabled = true).apply {
            getNextUpdate(context)
            verify { context.getString(R.string.risk_card_body_next_update) }
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

        createInstance(riskLevel = UNKNOWN_RISK_INITIAL).apply {
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

        createInstance(riskLevel = UNKNOWN_RISK_INITIAL).apply {
            showTracingButton() shouldBe false
        }
    }

    @Test
    fun `update button visibility`() {
        createInstance(
            riskLevel = INCREASED_RISK,
            isBackgroundJobEnabled = false).apply {
            showUpdateButton() shouldBe true
        }

        createInstance(
            riskLevel = INCREASED_RISK,
            isBackgroundJobEnabled = true).apply {
            showUpdateButton() shouldBe false
        }

        createInstance(
            riskLevel = UNKNOWN_RISK_OUTDATED_RESULTS,
            isBackgroundJobEnabled = false).apply {
            showUpdateButton() shouldBe false
        }

        createInstance(
            riskLevel = UNKNOWN_RISK_OUTDATED_RESULTS,
            isBackgroundJobEnabled = true).apply {
            showUpdateButton() shouldBe false
        }

        createInstance(
            riskLevel = NO_CALCULATION_POSSIBLE_TRACING_OFF,
            isBackgroundJobEnabled = false).apply {
            showUpdateButton() shouldBe false
        }

        createInstance(
            riskLevel = NO_CALCULATION_POSSIBLE_TRACING_OFF,
            isBackgroundJobEnabled = true).apply {
            showUpdateButton() shouldBe false
        }

        createInstance(
            riskLevel = LOW_LEVEL_RISK,
            isBackgroundJobEnabled = false).apply {
            showUpdateButton() shouldBe true
        }

        createInstance(
            riskLevel = LOW_LEVEL_RISK,
            isBackgroundJobEnabled = true).apply {
            showUpdateButton() shouldBe false
        }

        createInstance(
            riskLevel = UNKNOWN_RISK_INITIAL,
            isBackgroundJobEnabled = false).apply {
            showUpdateButton() shouldBe true
        }

        createInstance(
            riskLevel = UNKNOWN_RISK_INITIAL,
            isBackgroundJobEnabled = true).apply {
            showUpdateButton() shouldBe false
        }
    }

    @Test
    fun `risklevel headline is affected by score and refreshingstate`() {
        createInstance(
            riskLevel = INCREASED_RISK,
            isRefreshing = false).apply {
            getRiskLevelHeadline(context)
            verify { context.getString(R.string.risk_card_increased_risk_headline) }
        }

        createInstance(
            riskLevel = UNKNOWN_RISK_OUTDATED_RESULTS,
            isRefreshing = false).apply {
            getRiskLevelHeadline(context)
            verify { context.getString(R.string.risk_card_outdated_risk_headline) }
        }

        createInstance(
            riskLevel = NO_CALCULATION_POSSIBLE_TRACING_OFF,
            isRefreshing = false).apply {
            getRiskLevelHeadline(context)
            verify { context.getString(R.string.risk_card_no_calculation_possible_headline) }
        }

        createInstance(
            riskLevel = LOW_LEVEL_RISK,
            isRefreshing = false).apply {
            getRiskLevelHeadline(context)
            verify { context.getString(R.string.risk_card_low_risk_headline) }
        }

        createInstance(
            riskLevel = UNKNOWN_RISK_INITIAL,
            isRefreshing = false).apply {
            getRiskLevelHeadline(context)
            verify { context.getString(R.string.risk_card_unknown_risk_headline) }
        }

        createInstance(
            riskLevel = INCREASED_RISK,
            isRefreshing = true).apply {
            getRiskLevelHeadline(context)
            verify { context.getString(R.string.risk_card_loading_headline) }
        }

        createInstance(
            riskLevel = UNKNOWN_RISK_OUTDATED_RESULTS,
            isRefreshing = true).apply {
            getRiskLevelHeadline(context)
            verify { context.getString(R.string.risk_card_loading_headline) }
        }

        createInstance(
            riskLevel = NO_CALCULATION_POSSIBLE_TRACING_OFF,
            isRefreshing = true).apply {
            getRiskLevelHeadline(context)
            verify { context.getString(R.string.risk_card_loading_headline) }
        }

        createInstance(
            riskLevel = LOW_LEVEL_RISK,
            isRefreshing = true).apply {
            getRiskLevelHeadline(context)
            verify { context.getString(R.string.risk_card_loading_headline) }
        }

        createInstance(
            riskLevel = UNKNOWN_RISK_INITIAL,
            isRefreshing = true).apply {
            getRiskLevelHeadline(context)
            verify { context.getString(R.string.risk_card_loading_headline) }
        }
    }
}
