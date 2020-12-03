package de.rki.coronawarnapp.ui.tracing.card

import android.content.Context
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.risk.RiskState
import de.rki.coronawarnapp.risk.RiskState.CALCULATION_FAILED
import de.rki.coronawarnapp.risk.RiskState.INCREASED_RISK
import de.rki.coronawarnapp.risk.RiskState.LOW_RISK
import de.rki.coronawarnapp.tracing.GeneralTracingStatus.Status
import de.rki.coronawarnapp.tracing.TracingProgress
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toLocalDate
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifySequence
import org.joda.time.Instant
import org.joda.time.format.DateTimeFormat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

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
        tracingStatus: Status = mockk(),
        riskState: RiskState = LOW_RISK,
        tracingProgress: TracingProgress = TracingProgress.Idle,
        lastSuccessfulRiskState: RiskState = LOW_RISK,
        daysWithEncounters: Int = 0,
        lastEncounterAt: Instant? = null,
        activeTracingDaysInRetentionPeriod: Long = 0,
        lastExposureDetectionTime: Instant? = mockk(),
        isBackgroundJobEnabled: Boolean = false
    ) = TracingCardState(
        tracingStatus = tracingStatus,
        riskState = riskState,
        tracingProgress = tracingProgress,
        lastSuccessfulRiskState = lastSuccessfulRiskState,
        daysWithEncounters = daysWithEncounters,
        lastEncounterAt = lastEncounterAt,
        activeTracingDays = activeTracingDaysInRetentionPeriod,
        lastExposureDetectionTime = lastExposureDetectionTime,
        isManualKeyRetrievalEnabled = !isBackgroundJobEnabled
    )

    @Test
    fun `risklevel affects icon color`() {
        createInstance(riskState = INCREASED_RISK).apply {
            getStableIconColor(context)
            verify { context.getColor(R.color.colorStableLight) }
        }

        createInstance(riskState = LOW_RISK).apply {
            getStableIconColor(context)
            verify { context.getColor(R.color.colorStableLight) }
        }

        createInstance(riskState = CALCULATION_FAILED).apply {
            getStableIconColor(context)
            verify { context.getColor(R.color.colorTextSemanticNeutral) }
        }
    }

    @Test
    fun `risklevel affects riskcolors`() {
        createInstance(riskState = INCREASED_RISK).apply {
            getRiskInfoContainerBackgroundTint(context)
            verify { context.getColorStateList(R.color.card_increased) }
        }

        createInstance(riskState = LOW_RISK).apply {
            getRiskInfoContainerBackgroundTint(context)
            verify { context.getColorStateList(R.color.card_low) }
        }

        createInstance(riskState = CALCULATION_FAILED).apply {
            getRiskInfoContainerBackgroundTint(context)
            verify { context.getColorStateList(R.color.card_no_calculation) }
        }
    }

    @Test
    fun `risklevel affects risk body text`() {
        createInstance(riskState = INCREASED_RISK).apply {
            getErrorStateBody(context) shouldBe ""
        }

        createInstance(riskState = LOW_RISK).apply {
            getErrorStateBody(context) shouldBe ""
        }

        createInstance(riskState = CALCULATION_FAILED).apply {
            getErrorStateBody(context)
            verify { context.getString(R.string.risk_card_check_failed_no_internet_body) }
        }

        createInstance(
            riskState = CALCULATION_FAILED,
            tracingStatus = Status.TRACING_INACTIVE
        ).apply {
            getErrorStateBody(context)
            verify { context.getString(R.string.risk_card_body_tracing_off) }
        }
    }

    @Test
    fun `saved risk body is affected by risklevel`() {
        createInstance(riskState = CALCULATION_FAILED, lastSuccessfulRiskState = CALCULATION_FAILED).apply {
            getSavedRiskBody(context) shouldBe ""
        }

        createInstance(riskState = LOW_RISK, lastSuccessfulRiskState = CALCULATION_FAILED).apply {
            getSavedRiskBody(context) shouldBe ""
        }

        createInstance(riskState = INCREASED_RISK, lastSuccessfulRiskState = INCREASED_RISK).apply {
            getSavedRiskBody(context) shouldBe ""
        }

        createInstance(riskState = INCREASED_RISK, lastSuccessfulRiskState = CALCULATION_FAILED).apply {
            getSavedRiskBody(context) shouldBe ""
        }

        createInstance(riskState = INCREASED_RISK, lastSuccessfulRiskState = LOW_RISK).apply {
            getSavedRiskBody(context) shouldBe ""
        }

        createInstance(riskState = CALCULATION_FAILED, lastSuccessfulRiskState = LOW_RISK).apply {
            getSavedRiskBody(context)
            verify {
                context
                    .getString(R.string.risk_card_no_calculation_possible_body_saved_risk)
                    .format(context.getString(R.string.risk_card_low_risk_headline))
            }
        }

        createInstance(riskState = CALCULATION_FAILED, lastSuccessfulRiskState = INCREASED_RISK).apply {
            getSavedRiskBody(context)
            verify {
                context
                    .getString(R.string.risk_card_no_calculation_possible_body_saved_risk)
                    .format(context.getString(R.string.risk_card_increased_risk_headline))
            }
        }

        createInstance(riskState = CALCULATION_FAILED, lastSuccessfulRiskState = LOW_RISK).apply {
            getSavedRiskBody(context)
            verify {
                context
                    .getString(R.string.risk_card_no_calculation_possible_body_saved_risk)
                    .format(context.getString(R.string.risk_card_low_risk_headline))
            }
        }
    }

    @Test
    fun `saved risk body when tracing is disabled`() {
        createInstance(
            riskState = CALCULATION_FAILED,
            lastSuccessfulRiskState = LOW_RISK,
            tracingStatus = Status.TRACING_INACTIVE
        ).apply {
            getSavedRiskBody(context)
            verify {
                context
                    .getString(R.string.risk_card_no_calculation_possible_body_saved_risk)
                    .format(context.getString(R.string.risk_card_low_risk_headline))
            }
        }

        createInstance(
            riskState = CALCULATION_FAILED,
            lastSuccessfulRiskState = INCREASED_RISK,
            tracingStatus = Status.TRACING_INACTIVE
        ).apply {
            getSavedRiskBody(context)
            verify {
                context
                    .getString(R.string.risk_card_no_calculation_possible_body_saved_risk)
                    .format(context.getString(R.string.risk_card_increased_risk_headline))
            }
        }

        createInstance(
            riskState = CALCULATION_FAILED,
            lastSuccessfulRiskState = LOW_RISK,
            tracingStatus = Status.TRACING_INACTIVE
        ).apply {
            getSavedRiskBody(context)
            verify {
                context
                    .getString(R.string.risk_card_no_calculation_possible_body_saved_risk)
                    .format(context.getString(R.string.risk_card_low_risk_headline))
            }
        }
    }

    @Test
    fun `risk contact body is affected by risklevel`() {
        createInstance(riskState = CALCULATION_FAILED, daysWithEncounters = 0).apply {
            getRiskContactBody(context) shouldBe ""
        }

        createInstance(riskState = CALCULATION_FAILED, daysWithEncounters = 2).apply {
            getRiskContactBody(context) shouldBe ""
        }
    }

    @Test
    fun `risk icon formatting`() {
        createInstance(riskState = INCREASED_RISK).apply {
            getRiskContactIcon(context)
            verify { context.getDrawable(R.drawable.ic_risk_card_contact_increased) }
        }

        createInstance(riskState = LOW_RISK).apply {
            getRiskContactIcon(context)
            verify { context.getDrawable(R.drawable.ic_risk_card_contact) }
        }

        createInstance(riskState = CALCULATION_FAILED).apply {
            getRiskContactIcon(context)
            verify { context.getDrawable(R.drawable.ic_risk_card_contact) }
        }
    }

    @Test
    fun `last risk contact text formatting`() {
        createInstance(riskState = INCREASED_RISK, lastEncounterAt = Instant.EPOCH).apply {
            getRiskContactLast(context)
            verify {
                context.getString(
                    R.string.risk_card_high_risk_most_recent_body,
                    Instant.EPOCH.toLocalDate().toString(DateTimeFormat.mediumDate())
                )
            }
        }

        createInstance(
            riskState = INCREASED_RISK,
            lastEncounterAt = Instant.EPOCH,
            tracingStatus = Status.TRACING_INACTIVE
        ).apply {
            getRiskContactLast(context) shouldBe ""
        }

        createInstance(riskState = LOW_RISK).apply {
            getRiskContactLast(context) shouldBe ""
        }

        createInstance(riskState = CALCULATION_FAILED).apply {
            getRiskContactLast(context) shouldBe ""
        }
    }

    @Test
    fun `text for active risktracing in retention period`() {
        createInstance(riskState = INCREASED_RISK, activeTracingDaysInRetentionPeriod = 1).apply {
            getRiskActiveTracingDaysInRetentionPeriod(context) shouldBe ""
        }

        createInstance(riskState = CALCULATION_FAILED, activeTracingDaysInRetentionPeriod = 1).apply {
            getRiskActiveTracingDaysInRetentionPeriod(context) shouldBe ""
        }

        createInstance(riskState = LOW_RISK, activeTracingDaysInRetentionPeriod = 1).apply {
            getRiskActiveTracingDaysInRetentionPeriod(context)
            verify { context.getString(R.string.risk_card_body_saved_days).format(1) }
        }

        createInstance(riskState = LOW_RISK, activeTracingDaysInRetentionPeriod = 2).apply {
            getRiskActiveTracingDaysInRetentionPeriod(context)
            verify { context.getString(R.string.risk_card_body_saved_days).format(2) }
        }
    }

    @Test
    fun `text for last time diagnosis keys were fetched`() {
        val date = Instant()
        createInstance(
            riskState = INCREASED_RISK,
            lastSuccessfulRiskState = LOW_RISK,
            lastExposureDetectionTime = date
        ).apply {
            getTimeFetched(context)
            verify { context.getString(eq(R.string.risk_card_body_time_fetched), any()) }
        }

        createInstance(
            riskState = CALCULATION_FAILED,
            lastSuccessfulRiskState = LOW_RISK,
            lastExposureDetectionTime = date
        ).apply {
            getTimeFetched(context)
            verify { context.getString(eq(R.string.risk_card_body_time_fetched), any()) }
        }

        createInstance(
            riskState = CALCULATION_FAILED,
            lastSuccessfulRiskState = LOW_RISK,
            lastExposureDetectionTime = date
        ).apply {
            getTimeFetched(context)
            verify { context.getString(eq(R.string.risk_card_body_time_fetched), any()) }
        }

        createInstance(
            riskState = LOW_RISK,
            lastSuccessfulRiskState = LOW_RISK,
            lastExposureDetectionTime = date
        ).apply {
            getTimeFetched(context)
            verify { context.getString(eq(R.string.risk_card_body_time_fetched), any()) }
        }

        createInstance(riskState = INCREASED_RISK, lastExposureDetectionTime = date).apply {
            getTimeFetched(context)
            verify { context.getString(eq(R.string.risk_card_body_time_fetched), any()) }
        }

        createInstance(riskState = CALCULATION_FAILED, lastExposureDetectionTime = date).apply {
            getTimeFetched(context) shouldBe ""
        }

        createInstance(riskState = LOW_RISK, lastExposureDetectionTime = date).apply {
            getTimeFetched(context)
            verify { context.getString(eq(R.string.risk_card_body_time_fetched), any()) }
        }

        createInstance(riskState = INCREASED_RISK, lastExposureDetectionTime = null).apply {
            getTimeFetched(context)
            verify { context.getString(R.string.risk_card_body_not_yet_fetched) }
        }

        createInstance(riskState = CALCULATION_FAILED, lastExposureDetectionTime = null).apply {
            getTimeFetched(context) shouldBe ""
        }

        createInstance(riskState = LOW_RISK, lastExposureDetectionTime = null).apply {
            getTimeFetched(context)
            verify { context.getString(R.string.risk_card_body_not_yet_fetched) }
        }
    }

    @Test
    fun `task divider is formatted according to riskLevel`() {
        createInstance(riskState = INCREASED_RISK).apply {
            getStableDividerColor(context)
            verify { context.getColor(R.color.colorStableHairlineLight) }
        }

        createInstance(riskState = INCREASED_RISK, tracingStatus = Status.TRACING_INACTIVE).apply {
            getStableDividerColor(context)
            verify { context.getColor(R.color.colorStableHairlineDark) }
        }

        createInstance(riskState = LOW_RISK).apply {
            getStableDividerColor(context)
            verify { context.getColor(R.color.colorStableHairlineLight) }
        }

        createInstance(riskState = CALCULATION_FAILED).apply {
            getStableDividerColor(context)
            verify { context.getColor(R.color.colorStableHairlineDark) }
        }
    }

    @Test
    fun `tracing button visibility depends on risklevel`() {
        createInstance(riskState = INCREASED_RISK).apply {
            showTracingButton() shouldBe false
        }

        createInstance(riskState = LOW_RISK).apply {
            showTracingButton() shouldBe false
        }

        createInstance(riskState = CALCULATION_FAILED).apply {
            showTracingButton() shouldBe false
        }

        createInstance(riskState = CALCULATION_FAILED, tracingStatus = Status.TRACING_INACTIVE).apply {
            showTracingButton() shouldBe true
        }
    }

    @Test
    fun `update button visibility`() {
        createInstance(riskState = INCREASED_RISK, isBackgroundJobEnabled = false).apply {
            showUpdateButton() shouldBe true
        }

        createInstance(riskState = INCREASED_RISK, isBackgroundJobEnabled = true).apply {
            showUpdateButton() shouldBe false
        }

        createInstance(riskState = CALCULATION_FAILED, isBackgroundJobEnabled = false).apply {
            showUpdateButton() shouldBe true
        }

        createInstance(riskState = CALCULATION_FAILED, isBackgroundJobEnabled = true).apply {
            showUpdateButton() shouldBe true
        }

        createInstance(riskState = LOW_RISK, isBackgroundJobEnabled = false).apply {
            showUpdateButton() shouldBe true
        }

        createInstance(riskState = LOW_RISK, isBackgroundJobEnabled = true).apply {
            showUpdateButton() shouldBe false
        }
    }

    @Test
    fun `risklevel headline is affected by score`() {
        createInstance(riskState = INCREASED_RISK).apply {
            getRiskLevelHeadline(context)
            verify { context.getString(R.string.risk_card_increased_risk_headline) }
        }

        createInstance(riskState = CALCULATION_FAILED).apply {
            getRiskLevelHeadline(context)
            verify { context.getString(R.string.risk_card_check_failed_no_internet_headline) }
        }

        createInstance(riskState = CALCULATION_FAILED, tracingStatus = Status.TRACING_INACTIVE).apply {
            getRiskLevelHeadline(context)
            verify { context.getString(R.string.risk_card_no_calculation_possible_headline) }
        }

        createInstance(riskState = INCREASED_RISK, tracingStatus = Status.TRACING_INACTIVE).apply {
            getRiskLevelHeadline(context)
            verify { context.getString(R.string.risk_card_no_calculation_possible_headline) }
        }

        createInstance(riskState = LOW_RISK).apply {
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
