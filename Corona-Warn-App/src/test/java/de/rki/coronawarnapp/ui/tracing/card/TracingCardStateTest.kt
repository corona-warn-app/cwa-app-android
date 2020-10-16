package de.rki.coronawarnapp.ui.tracing.card

import android.content.Context
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.risk.RiskLevelConstants
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
        riskLevelLastSuccessfulCalculation = riskLevelLastSuccessfulCalculation,
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
        createInstance(riskLevel = RiskLevelConstants.INCREASED_RISK).apply {
            getStableIconColor(context)
            verify { context.getColor(R.color.colorStableLight) }
        }

        createInstance(riskLevel = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS).apply {
            getStableIconColor(context)
            verify { context.getColor(R.color.colorTextSemanticNeutral) }
        }


        createInstance(riskLevel = RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF).apply {
            getStableIconColor(context)
            verify { context.getColor(R.color.colorTextSemanticNeutral) }
        }

        createInstance(riskLevel = RiskLevelConstants.LOW_LEVEL_RISK).apply {
            getStableIconColor(context)
            verify { context.getColor(R.color.colorStableLight) }
        }

        createInstance(riskLevel = RiskLevelConstants.UNKNOWN_RISK_INITIAL).apply {
            getStableIconColor(context)
            verify { context.getColor(R.color.colorStableLight) }
        }
    }

    @Test
    fun `risklevel affects riskcolors`() {
        createInstance(riskLevel = RiskLevelConstants.INCREASED_RISK).apply {
            getRiskColorStateList(context)
            verify { context.getColorStateList(R.color.card_increased) }
        }

        createInstance(riskLevel = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS).apply {
            getRiskColorStateList(context)
            verify { context.getColorStateList(R.color.card_outdated) }
        }

        createInstance(riskLevel = RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF).apply {
            getRiskColorStateList(context)
            verify { context.getColorStateList(R.color.card_no_calculation) }
        }

        createInstance(riskLevel = RiskLevelConstants.LOW_LEVEL_RISK).apply {
            getRiskColorStateList(context)
            verify { context.getColorStateList(R.color.card_low) }
        }

        createInstance(riskLevel = RiskLevelConstants.UNKNOWN_RISK_INITIAL).apply {
            getRiskColorStateList(context)
            verify { context.getColorStateList(R.color.card_unknown) }
        }
    }

    @Test
    fun `risklevel affects risk body text`() {
        createInstance(riskLevel = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS).apply {
            getRiskBody(context)
            verify { context.getString(R.string.risk_card_outdated_risk_body) }
        }

        createInstance(riskLevel = RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF).apply {
            getRiskBody(context)
            verify { context.getString(R.string.risk_card_body_tracing_off) }
        }

        createInstance(riskLevel = RiskLevelConstants.UNKNOWN_RISK_INITIAL).apply {
            getRiskBody(context)
            verify { context.getString(R.string.risk_card_unknown_risk_body) }
        }

        createInstance(riskLevel = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS_MANUAL).apply {
            getRiskBody(context)
            verify { context.getString(R.string.risk_card_outdated_manual_risk_body) }
        }

        createInstance(riskLevel = RiskLevelConstants.UNKNOWN_RISK_INITIAL).apply {
            getRiskBody(context) shouldBe ""
        }
    }

    @Test
    fun `saved risk body is affected by risklevel`() {
        createInstance().apply {
//           getSavedRiskBody
            TODO()
//            private fun formatRiskSavedRiskBase(
//        iRiskLevelScore: Int?,
//        iRiskLevelScoreLastSuccessfulCalculated: Int?,
//        sValue: String
//    ) {
//        every { context.getString(R.string.risk_card_no_calculation_possible_body_saved_risk) } returns R.string.risk_card_no_calculation_possible_body_saved_risk.toString()
//
//        val result = formatRiskSavedRisk(
//            riskLevelScore = iRiskLevelScore,
//            riskLevelScoreLastSuccessfulCalculated = iRiskLevelScoreLastSuccessfulCalculated
//        )
//        assertThat(
//            result, `is`(sValue)
//        )
//    }
//    @Test
//    fun formatRiskSavedRisk() {
//        formatRiskSavedRiskBase(iRiskLevelScore = null, iRiskLevelScoreLastSuccessfulCalculated = null, sValue = "")
//        formatRiskSavedRiskBase(
//            iRiskLevelScore = RiskLevelConstants.INCREASED_RISK,
//            iRiskLevelScoreLastSuccessfulCalculated = null,
//            sValue = ""
//        )
//        formatRiskSavedRiskBase(
//            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS,
//            iRiskLevelScoreLastSuccessfulCalculated = null,
//            sValue = ""
//        )
//        formatRiskSavedRiskBase(
//            iRiskLevelScore = RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF,
//            iRiskLevelScoreLastSuccessfulCalculated = null,
//            sValue = ""
//        )
//        formatRiskSavedRiskBase(
//            iRiskLevelScore = RiskLevelConstants.LOW_LEVEL_RISK,
//            iRiskLevelScoreLastSuccessfulCalculated = null,
//            sValue = ""
//        )
//        formatRiskSavedRiskBase(
//            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_INITIAL,
//            iRiskLevelScoreLastSuccessfulCalculated = null,
//            sValue = ""
//        )
//
//        formatRiskSavedRiskBase(
//            iRiskLevelScore = RiskLevelConstants.INCREASED_RISK,
//            iRiskLevelScoreLastSuccessfulCalculated = RiskLevelConstants.INCREASED_RISK,
//            sValue = ""
//        )
//        formatRiskSavedRiskBase(
//            iRiskLevelScore = RiskLevelConstants.INCREASED_RISK,
//            iRiskLevelScoreLastSuccessfulCalculated = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS,
//            sValue = ""
//        )
//        formatRiskSavedRiskBase(
//            iRiskLevelScore = RiskLevelConstants.INCREASED_RISK,
//            iRiskLevelScoreLastSuccessfulCalculated = RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF,
//            sValue = ""
//        )
//        formatRiskSavedRiskBase(
//            iRiskLevelScore = RiskLevelConstants.INCREASED_RISK,
//            iRiskLevelScoreLastSuccessfulCalculated = RiskLevelConstants.LOW_LEVEL_RISK,
//            sValue = ""
//        )
//        formatRiskSavedRiskBase(
//            iRiskLevelScore = RiskLevelConstants.INCREASED_RISK,
//            iRiskLevelScoreLastSuccessfulCalculated = RiskLevelConstants.UNKNOWN_RISK_INITIAL,
//            sValue = ""
//        )
//
//        formatRiskSavedRiskBase(
//            iRiskLevelScore = RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF,
//            iRiskLevelScoreLastSuccessfulCalculated = RiskLevelConstants.LOW_LEVEL_RISK,
//            sValue = context.getString(R.string.risk_card_no_calculation_possible_body_saved_risk)
//                .format(
//                    formatRiskLevelHeadline(
//                        riskLevelScore = RiskLevelConstants.LOW_LEVEL_RISK,
//                        isRefreshing = false
//                    )
//                )
//        )
//        formatRiskSavedRiskBase(
//            iRiskLevelScore = RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF,
//            iRiskLevelScoreLastSuccessfulCalculated = RiskLevelConstants.INCREASED_RISK,
//            sValue = context.getString(R.string.risk_card_no_calculation_possible_body_saved_risk)
//                .format(
//                    formatRiskLevelHeadline(
//                        riskLevelScore = RiskLevelConstants.INCREASED_RISK,
//                        isRefreshing = false
//                    )
//                )
//        )
//        formatRiskSavedRiskBase(
//            iRiskLevelScore = RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF,
//            iRiskLevelScoreLastSuccessfulCalculated = RiskLevelConstants.UNKNOWN_RISK_INITIAL,
//            sValue = context.getString(R.string.risk_card_no_calculation_possible_body_saved_risk)
//                .format(
//                    formatRiskLevelHeadline(
//                        riskLevelScore = RiskLevelConstants.UNKNOWN_RISK_INITIAL,
//                        isRefreshing = false
//                    )
//                )
//        )
//
//        formatRiskSavedRiskBase(
//            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS,
//            iRiskLevelScoreLastSuccessfulCalculated = RiskLevelConstants.LOW_LEVEL_RISK,
//            sValue = context.getString(R.string.risk_card_no_calculation_possible_body_saved_risk)
//                .format(
//                    formatRiskLevelHeadline(
//                        riskLevelScore = RiskLevelConstants.LOW_LEVEL_RISK,
//                        isRefreshing = false
//                    )
//                )
//        )
//        formatRiskSavedRiskBase(
//            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS,
//            iRiskLevelScoreLastSuccessfulCalculated = RiskLevelConstants.INCREASED_RISK,
//            sValue = context.getString(R.string.risk_card_no_calculation_possible_body_saved_risk)
//                .format(
//                    formatRiskLevelHeadline(
//                        riskLevelScore = RiskLevelConstants.INCREASED_RISK,
//                        isRefreshing = false
//                    )
//                )
//        )
//        formatRiskSavedRiskBase(
//            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS,
//            iRiskLevelScoreLastSuccessfulCalculated = RiskLevelConstants.UNKNOWN_RISK_INITIAL,
//            sValue = context.getString(R.string.risk_card_no_calculation_possible_body_saved_risk)
//                .format(
//                    formatRiskLevelHeadline(
//                        riskLevelScore = RiskLevelConstants.UNKNOWN_RISK_INITIAL,
//                        isRefreshing = false
//                    )
//                )
//        )
//    }
        }
    }

    @Test
    fun `risk contact body is affected by risklevel`() {
        createInstance().apply {
            TODO("getRiskContactBody")
//            private fun formatRiskContactBase(iRiskLevelScore: Int?, iMatchedKeysCount: Int?, sValue: String) {
//        every { context.getString(R.string.risk_card_body_contact) } returns R.string.risk_card_body_contact.toString()
//
//        val result = formatRiskContact(riskLevelScore = iRiskLevelScore, matchedKeysCount = iMatchedKeysCount)
//        assertThat(
//            result, `is`(sValue)
//        )
//    }
//
//    @Test
//    fun formatRiskContact() {
//        formatRiskContactBase(iRiskLevelScore = null, iMatchedKeysCount = null, sValue = "")
//        formatRiskContactBase(iRiskLevelScore = null, iMatchedKeysCount = 0, sValue = "")
//
//        formatRiskContactBase(
//            iRiskLevelScore = RiskLevelConstants.INCREASED_RISK,
//            iMatchedKeysCount = 0,
//            sValue = context.getString(R.string.risk_card_body_contact)
//        )
//        formatRiskContactBase(
//            iRiskLevelScore = RiskLevelConstants.INCREASED_RISK,
//            iMatchedKeysCount = 2,
//            sValue = context.resources.getQuantityString(
//                R.plurals.risk_card_body_contact_value,
//                2,
//                2
//            )
//        )
//
//        formatRiskContactBase(
//            iRiskLevelScore = RiskLevelConstants.LOW_LEVEL_RISK,
//            iMatchedKeysCount = 0,
//            sValue = context.getString(R.string.risk_card_body_contact)
//        )
//        formatRiskContactBase(
//            iRiskLevelScore = RiskLevelConstants.LOW_LEVEL_RISK,
//            iMatchedKeysCount = 2,
//            sValue = context.resources.getQuantityString(
//                R.plurals.risk_card_body_contact_value,
//                2,
//                2
//            )
//        )
//
//        formatRiskContactBase(
//            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS,
//            iMatchedKeysCount = 0,
//            sValue = ""
//        )
//        formatRiskContactBase(
//            iRiskLevelScore = RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF,
//            iMatchedKeysCount = 0,
//            sValue = ""
//        )
//        formatRiskContactBase(
//            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_INITIAL,
//            iMatchedKeysCount = 0,
//            sValue = ""
//        )
//
//        formatRiskContactBase(
//            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS,
//            iMatchedKeysCount = 2,
//            sValue = ""
//        )
//        formatRiskContactBase(
//            iRiskLevelScore = RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF,
//            iMatchedKeysCount = 2,
//            sValue = ""
//        )
//        formatRiskContactBase(
//            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_INITIAL,
//            iMatchedKeysCount = 2,
//            sValue = ""
//        )
//    }
        }
    }

    @Test
    fun `risk icon formatting`() {
        TODO("getRiskContactIcon")
//            private fun formatRiskContactIconBase(iRiskLevelScore: Int?) {
//        every { context.getDrawable(any()) } returns drawable
//
//        val result = formatRiskContactIcon(riskLevelScore = iRiskLevelScore)
//        assertThat(
//            result, `is`(drawable)
//        )
//    }
//    @Test
//    fun formatRiskContactIcon() {
//        formatRiskContactIconBase(iRiskLevelScore = null)
//        formatRiskContactIconBase(iRiskLevelScore = RiskLevelConstants.INCREASED_RISK)
//        formatRiskContactIconBase(iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS)
//        formatRiskContactIconBase(iRiskLevelScore = RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF)
//        formatRiskContactIconBase(iRiskLevelScore = RiskLevelConstants.LOW_LEVEL_RISK)
//        formatRiskContactIconBase(iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_INITIAL)
//    }
    }

    @Test
    fun `last risk contact text formatting`() {
        TODO("getRiskContactLast")
//            private fun formatRiskContactLastBase(iRiskLevelScore: Int?, iDaysSinceLastExposure: Int?, sValue: String) {
//        every { context.getString(R.string.risk_card_body_contact) } returns R.string.risk_card_body_contact.toString()
//
//        val result =
//            formatRiskContactLast(riskLevelScore = iRiskLevelScore, daysSinceLastExposure = iDaysSinceLastExposure)
//        assertThat(
//            result, `is`(sValue)
//        )
//    }
//
//    @Test
//    fun formatRiskContactLast() {
//        formatRiskContactLastBase(iRiskLevelScore = null, iDaysSinceLastExposure = 2, sValue = "")
//        formatRiskContactLastBase(
//            iRiskLevelScore = RiskLevelConstants.INCREASED_RISK,
//            iDaysSinceLastExposure = 2,
//            sValue = context.resources.getQuantityString(
//                R.plurals.risk_card_increased_risk_body_contact_last,
//                2,
//                2
//            )
//        )
//        formatRiskContactLastBase(
//            iRiskLevelScore = RiskLevelConstants.INCREASED_RISK,
//            iDaysSinceLastExposure = null,
//            sValue = context.resources.getQuantityString(
//                R.plurals.risk_card_increased_risk_body_contact_last,
//                0,
//                0
//            )
//        )
//        formatRiskContactLastBase(
//            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS,
//            iDaysSinceLastExposure = 2,
//            sValue = ""
//        )
//        formatRiskContactLastBase(
//            iRiskLevelScore = RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF,
//            iDaysSinceLastExposure = 2,
//            sValue = ""
//        )
//        formatRiskContactLastBase(
//            iRiskLevelScore = RiskLevelConstants.LOW_LEVEL_RISK,
//            iDaysSinceLastExposure = 2,
//            sValue = ""
//        )
//        formatRiskContactLastBase(
//            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_INITIAL,
//            iDaysSinceLastExposure = 2,
//            sValue = ""
//        )
//    }
    }

    @Test
    fun `text for active risktracing in retention period`() {
        TODO("getRiskActiveTracingDaysInRetentionPeriod")
//        private fun formatRiskActiveTracingDaysInRetentionPeriodBase(
//        iRiskLevelScore: Int?,
//        bShowDetails: Boolean,
//        lActiveTracingDaysInRetentionPeriod: Long,
//        sValue: String
//    ) {
//        every { context.getString(R.string.risk_card_body_saved_days) } returns R.string.risk_card_body_saved_days.toString()
//
//        val result = formatRiskActiveTracingDaysInRetentionPeriod(
//            riskLevelScore = iRiskLevelScore,
//            showDetails = bShowDetails,
//            activeTracingDaysInRetentionPeriod = lActiveTracingDaysInRetentionPeriod
//        )
//        assertThat(
//            result, `is`(sValue)
//        )
//    }
//
//    @Test
//    fun formatRiskActiveTracingDaysInRetentionPeriod() {
//        formatRiskActiveTracingDaysInRetentionPeriodBase(
//            iRiskLevelScore = null,
//            bShowDetails = false,
//            lActiveTracingDaysInRetentionPeriod = 1,
//            sValue = ""
//        )
//        formatRiskActiveTracingDaysInRetentionPeriodBase(
//            iRiskLevelScore = RiskLevelConstants.INCREASED_RISK,
//            bShowDetails = false,
//            lActiveTracingDaysInRetentionPeriod = 1,
//            sValue = ""
//        )
//        formatRiskActiveTracingDaysInRetentionPeriodBase(
//            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS,
//            bShowDetails = false,
//            lActiveTracingDaysInRetentionPeriod = 1,
//            sValue = ""
//        )
//        formatRiskActiveTracingDaysInRetentionPeriodBase(
//            iRiskLevelScore = RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF,
//            bShowDetails = false,
//            lActiveTracingDaysInRetentionPeriod = 1,
//            sValue = ""
//        )
//        formatRiskActiveTracingDaysInRetentionPeriodBase(
//            iRiskLevelScore = RiskLevelConstants.LOW_LEVEL_RISK,
//            bShowDetails = false,
//            lActiveTracingDaysInRetentionPeriod = 1,
//            sValue = context.getString(
//                R.string.risk_card_body_saved_days
//            )
//                .format(1)
//        )
//        formatRiskActiveTracingDaysInRetentionPeriodBase(
//            iRiskLevelScore = RiskLevelConstants.LOW_LEVEL_RISK,
//            bShowDetails = false,
//            lActiveTracingDaysInRetentionPeriod = 2,
//            sValue = context.getString(
//                R.string.risk_card_body_saved_days
//            )
//                .format(2)
//        )
//        formatRiskActiveTracingDaysInRetentionPeriodBase(
//            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_INITIAL,
//            bShowDetails = false,
//            lActiveTracingDaysInRetentionPeriod = 1,
//            sValue = ""
//        )
//
//        formatRiskActiveTracingDaysInRetentionPeriodBase(
//            iRiskLevelScore = null,
//            bShowDetails = true,
//            lActiveTracingDaysInRetentionPeriod = 1,
//            sValue = ""
//        )
//        formatRiskActiveTracingDaysInRetentionPeriodBase(
//            iRiskLevelScore = RiskLevelConstants.INCREASED_RISK,
//            bShowDetails = true,
//            lActiveTracingDaysInRetentionPeriod = 1,
//            sValue = context.getString(
//                R.string.risk_card_body_saved_days
//            )
//                .format(1)
//        )
//        formatRiskActiveTracingDaysInRetentionPeriodBase(
//            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS,
//            bShowDetails = true,
//            lActiveTracingDaysInRetentionPeriod = 1,
//            sValue = ""
//        )
//        formatRiskActiveTracingDaysInRetentionPeriodBase(
//            iRiskLevelScore = RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF,
//            bShowDetails = true,
//            lActiveTracingDaysInRetentionPeriod = 1,
//            sValue = ""
//        )
//        formatRiskActiveTracingDaysInRetentionPeriodBase(
//            iRiskLevelScore = RiskLevelConstants.LOW_LEVEL_RISK,
//            bShowDetails = true,
//            lActiveTracingDaysInRetentionPeriod = 1,
//            sValue = context.getString(
//                R.string.risk_card_body_saved_days
//            )
//                .format(1)
//        )
//        formatRiskActiveTracingDaysInRetentionPeriodBase(
//            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_INITIAL,
//            bShowDetails = true,
//            lActiveTracingDaysInRetentionPeriod = 1,
//            sValue = ""
//        )
//    }
    }

    @Test
    fun `text for last time diagnosis keys were fetched`() {
        TODO("getTimeFetched")
//         private fun formatTimeFetchedBase(
//        iRiskLevelScore: Int?,
//        iRiskLevelScoreLastSuccessfulCalculated: Int?,
//        dLastTimeDiagnosisKeysFetched: Date?,
//        sValue: String
//    ) {
//        every {
//            context.getString(
//                R.string.risk_card_body_time_fetched,
//                formatRelativeDateTimeString(context, Date())
//            )
//        } returns R.string.risk_card_body_time_fetched.toString()
//        every { context.getString(R.string.risk_card_body_not_yet_fetched) } returns R.string.risk_card_body_not_yet_fetched.toString()
//
//        val result = formatTimeFetched(
//            riskLevelScore = iRiskLevelScore,
//            riskLevelScoreLastSuccessfulCalculated = iRiskLevelScoreLastSuccessfulCalculated,
//            lastTimeDiagnosisKeysFetched = dLastTimeDiagnosisKeysFetched
//        )
//        assertThat(
//            result, `is`(sValue)
//        )
//    }
//
//    @Test
//    fun formatTimeFetched() {
//        formatTimeFetchedBase(
//            iRiskLevelScore = null,
//            iRiskLevelScoreLastSuccessfulCalculated = 2,
//            dLastTimeDiagnosisKeysFetched = Date(),
//            sValue = ""
//        )
//        formatTimeFetchedBase(
//            iRiskLevelScore = RiskLevelConstants.INCREASED_RISK,
//            iRiskLevelScoreLastSuccessfulCalculated = 2,
//            dLastTimeDiagnosisKeysFetched = Date(),
//            sValue = context.getString(
//                R.string.risk_card_body_time_fetched,
//                formatRelativeDateTimeString(context, Date())
//            )
//        )
//        formatTimeFetchedBase(
//            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS,
//            iRiskLevelScoreLastSuccessfulCalculated = 2,
//            dLastTimeDiagnosisKeysFetched = Date(),
//            sValue = context.getString(
//                R.string.risk_card_body_time_fetched,
//                formatRelativeDateTimeString(context, Date())
//            )
//        )
//        formatTimeFetchedBase(
//            iRiskLevelScore = RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF,
//            iRiskLevelScoreLastSuccessfulCalculated = 2,
//            dLastTimeDiagnosisKeysFetched = Date(),
//            sValue = context.getString(
//                R.string.risk_card_body_time_fetched,
//                formatRelativeDateTimeString(context, Date())
//            )
//        )
//        formatTimeFetchedBase(
//            iRiskLevelScore = RiskLevelConstants.LOW_LEVEL_RISK,
//            iRiskLevelScoreLastSuccessfulCalculated = 2,
//            dLastTimeDiagnosisKeysFetched = Date(),
//            sValue = context.getString(
//                R.string.risk_card_body_time_fetched,
//                formatRelativeDateTimeString(context, Date())
//            )
//        )
//        formatTimeFetchedBase(
//            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_INITIAL,
//            iRiskLevelScoreLastSuccessfulCalculated = 2,
//            dLastTimeDiagnosisKeysFetched = Date(),
//            sValue = ""
//        )
//
//        formatTimeFetchedBase(
//            iRiskLevelScore = null,
//            iRiskLevelScoreLastSuccessfulCalculated = null,
//            dLastTimeDiagnosisKeysFetched = Date(),
//            sValue = ""
//        )
//        formatTimeFetchedBase(
//            iRiskLevelScore = RiskLevelConstants.INCREASED_RISK,
//            iRiskLevelScoreLastSuccessfulCalculated = null,
//            dLastTimeDiagnosisKeysFetched = Date(),
//            sValue = context.getString(
//                R.string.risk_card_body_time_fetched,
//                formatRelativeDateTimeString(context, Date())
//            )
//        )
//        formatTimeFetchedBase(
//            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS,
//            iRiskLevelScoreLastSuccessfulCalculated = null,
//            dLastTimeDiagnosisKeysFetched = Date(),
//            sValue = ""
//        )
//        formatTimeFetchedBase(
//            iRiskLevelScore = RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF,
//            iRiskLevelScoreLastSuccessfulCalculated = null,
//            dLastTimeDiagnosisKeysFetched = Date(),
//            sValue = ""
//        )
//        formatTimeFetchedBase(
//            iRiskLevelScore = RiskLevelConstants.LOW_LEVEL_RISK,
//            iRiskLevelScoreLastSuccessfulCalculated = null,
//            dLastTimeDiagnosisKeysFetched = Date(),
//            sValue = context.getString(
//                R.string.risk_card_body_time_fetched,
//                formatRelativeDateTimeString(context, Date())
//            )
//        )
//        formatTimeFetchedBase(
//            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_INITIAL,
//            iRiskLevelScoreLastSuccessfulCalculated = null,
//            dLastTimeDiagnosisKeysFetched = Date(),
//            sValue = ""
//        )
//
//        formatTimeFetchedBase(
//            iRiskLevelScore = null,
//            iRiskLevelScoreLastSuccessfulCalculated = null,
//            dLastTimeDiagnosisKeysFetched = null,
//            sValue = ""
//        )
//        formatTimeFetchedBase(
//            iRiskLevelScore = RiskLevelConstants.INCREASED_RISK,
//            iRiskLevelScoreLastSuccessfulCalculated = null,
//            dLastTimeDiagnosisKeysFetched = null,
//            sValue = context.getString(R.string.risk_card_body_not_yet_fetched)
//        )
//        formatTimeFetchedBase(
//            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS,
//            iRiskLevelScoreLastSuccessfulCalculated = null,
//            dLastTimeDiagnosisKeysFetched = null,
//            sValue = ""
//        )
//        formatTimeFetchedBase(
//            iRiskLevelScore = RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF,
//            iRiskLevelScoreLastSuccessfulCalculated = null,
//            dLastTimeDiagnosisKeysFetched = null,
//            sValue = ""
//        )
//        formatTimeFetchedBase(
//            iRiskLevelScore = RiskLevelConstants.LOW_LEVEL_RISK,
//            iRiskLevelScoreLastSuccessfulCalculated = null,
//            dLastTimeDiagnosisKeysFetched = null,
//            sValue = context.getString(R.string.risk_card_body_not_yet_fetched)
//        )
//        formatTimeFetchedBase(
//            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_INITIAL,
//            iRiskLevelScoreLastSuccessfulCalculated = null,
//            dLastTimeDiagnosisKeysFetched = null,
//            sValue = ""
//        )
//    }
    }

    @Test
    fun `text for next update time`() {
        TODO("getNextUpdate")
//         private fun formatNextUpdateBase(
//        iRiskLevelScore: Int?,
//        bIsBackgroundJobEnabled: Boolean?,
//        sValue: String
//    ) {
//        every { context.getString(R.string.risk_card_body_next_update) } returns R.string.risk_card_body_next_update.toString()
//
//        val result =
//            formatNextUpdate(
//                riskLevelScore = iRiskLevelScore,
//                isBackgroundJobEnabled = bIsBackgroundJobEnabled
//            )
//        assertThat(
//            result, `is`(sValue)
//        )
//    }
//
//    @Test
//    fun formatNextUpdate() {
//        formatNextUpdateBase(iRiskLevelScore = null, bIsBackgroundJobEnabled = null, sValue = "")
//        formatNextUpdateBase(
//            iRiskLevelScore = RiskLevelConstants.INCREASED_RISK,
//            bIsBackgroundJobEnabled = null,
//            sValue = ""
//        )
//        formatNextUpdateBase(
//            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS,
//            bIsBackgroundJobEnabled = null,
//            sValue = ""
//        )
//        formatNextUpdateBase(
//            iRiskLevelScore = RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF,
//            bIsBackgroundJobEnabled = null,
//            sValue = ""
//        )
//        formatNextUpdateBase(
//            iRiskLevelScore = RiskLevelConstants.LOW_LEVEL_RISK,
//            bIsBackgroundJobEnabled = null,
//            sValue = ""
//        )
//        formatNextUpdateBase(
//            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_INITIAL,
//            bIsBackgroundJobEnabled = null,
//            sValue = ""
//        )
//
//        formatNextUpdateBase(iRiskLevelScore = null, bIsBackgroundJobEnabled = true, sValue = "")
//        formatNextUpdateBase(
//            iRiskLevelScore = RiskLevelConstants.INCREASED_RISK,
//            bIsBackgroundJobEnabled = true,
//            sValue = context.getString(
//                R.string.risk_card_body_next_update
//            )
//        )
//        formatNextUpdateBase(
//            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS,
//            bIsBackgroundJobEnabled = true,
//            sValue = ""
//        )
//        formatNextUpdateBase(
//            iRiskLevelScore = RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF,
//            bIsBackgroundJobEnabled = true,
//            sValue = ""
//        )
//        formatNextUpdateBase(
//            iRiskLevelScore = RiskLevelConstants.LOW_LEVEL_RISK,
//            bIsBackgroundJobEnabled = true,
//            sValue = context.getString(
//                R.string.risk_card_body_next_update
//            )
//        )
//        formatNextUpdateBase(
//            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_INITIAL,
//            bIsBackgroundJobEnabled = true,
//            sValue = context.getString(
//                R.string.risk_card_body_next_update
//            )
//        )
//    }
    }

    @Test
    fun `task divider is formatted according to riskLevel`() {
        TODO("getStableDividerColor")
//
//    private fun formatStableDividerColorBase(iRiskLevelScore: Int?) {
//        every { context.getColor(any()) } returns R.color.colorStableLight
//
//        val result = formatStableDividerColor(riskLevelScore = iRiskLevelScore)
//        assertThat(
//            result, `is`(R.color.colorStableLight)
//        )
//    }
//    @Test
//    fun formatStableDividerColor() {
//        formatStableDividerColorBase(iRiskLevelScore = null)
//        formatStableDividerColorBase(iRiskLevelScore = RiskLevelConstants.INCREASED_RISK)
//        formatStableDividerColorBase(iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS)
//        formatStableDividerColorBase(iRiskLevelScore = RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF)
//        formatStableDividerColorBase(iRiskLevelScore = RiskLevelConstants.LOW_LEVEL_RISK)
//        formatStableDividerColorBase(iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_INITIAL)
//    }
    }

    @Test
    fun `tracing button visibility depends on risklevel`() {
        TODO("showTracingButton")
//         private fun formatButtonEnableTracingVisibilityBase(
//        iRiskLevelScore: Int?,
//        bShowDetails: Boolean?,
//        iValue: Int
//    ) {
//        val result = formatButtonEnableTracingVisibility(
//            riskLevelScore = iRiskLevelScore,
//            showDetails = bShowDetails
//        )
//        assertThat(
//            result, `is`(iValue)
//        )
//    }
//    @Test
//    fun formatButtonEnableTracingVisibility() {
//        formatButtonEnableTracingVisibilityBase(
//            iRiskLevelScore = null,
//            bShowDetails = true,
//            iValue = View.GONE
//        )
//        formatButtonEnableTracingVisibilityBase(
//            iRiskLevelScore = RiskLevelConstants.INCREASED_RISK,
//            bShowDetails = true,
//            iValue = View.GONE
//        )
//        formatButtonEnableTracingVisibilityBase(
//            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS,
//            bShowDetails = true,
//            iValue = View.GONE
//        )
//        formatButtonEnableTracingVisibilityBase(
//            iRiskLevelScore = RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF,
//            bShowDetails = true,
//            iValue = View.GONE
//        )
//        formatButtonEnableTracingVisibilityBase(
//            iRiskLevelScore = RiskLevelConstants.LOW_LEVEL_RISK,
//            bShowDetails = true,
//            iValue = View.GONE
//        )
//        formatButtonEnableTracingVisibilityBase(
//            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_INITIAL,
//            bShowDetails = true,
//            iValue = View.GONE
//        )
//
//        formatButtonEnableTracingVisibilityBase(
//            iRiskLevelScore = RiskLevelConstants.INCREASED_RISK,
//            bShowDetails = false,
//            iValue = View.GONE
//        )
//        formatButtonEnableTracingVisibilityBase(
//            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS,
//            bShowDetails = false,
//            iValue = View.VISIBLE
//        )
//        formatButtonEnableTracingVisibilityBase(
//            iRiskLevelScore = RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF,
//            bShowDetails = false,
//            iValue = View.VISIBLE
//        )
//        formatButtonEnableTracingVisibilityBase(
//            iRiskLevelScore = RiskLevelConstants.LOW_LEVEL_RISK,
//            bShowDetails = false,
//            iValue = View.GONE
//        )
//        formatButtonEnableTracingVisibilityBase(
//            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_INITIAL,
//            bShowDetails = false,
//            iValue = View.GONE
//        )
//    }
    }

    @Test
    fun `update button visibility`() {
        TODO("showUpdateButton")
//        private fun formatButtonUpdateVisibilityBase(
//        iRiskLevelScore: Int?,
//        bIsBackgroundJobEnabled: Boolean?,
//        bShowDetails: Boolean?,
//        iValue: Int
//    ) {
//        val result = formatButtonUpdateVisibility(
//            riskLevelScore = iRiskLevelScore,
//            isBackgroundJobEnabled = bIsBackgroundJobEnabled,
//            showDetails = bShowDetails
//        )
//        assertThat(
//            result, `is`(iValue)
//        )
//    }
//
//    @Test
//    fun formatButtonUpdateVisibility() {
//        formatButtonUpdateVisibilityBase(
//            iRiskLevelScore = null,
//            bIsBackgroundJobEnabled = true,
//            bShowDetails = true,
//            iValue = View.GONE
//        )
//        formatButtonUpdateVisibilityBase(
//            iRiskLevelScore = RiskLevelConstants.INCREASED_RISK,
//            bIsBackgroundJobEnabled = true,
//            bShowDetails = true,
//            iValue = View.GONE
//        )
//        formatButtonUpdateVisibilityBase(
//            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS,
//            bIsBackgroundJobEnabled = true,
//            bShowDetails = true,
//            iValue = View.GONE
//        )
//        formatButtonUpdateVisibilityBase(
//            iRiskLevelScore = RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF,
//            bIsBackgroundJobEnabled = true,
//            bShowDetails = true,
//            iValue = View.GONE
//        )
//        formatButtonUpdateVisibilityBase(
//            iRiskLevelScore = RiskLevelConstants.LOW_LEVEL_RISK,
//            bIsBackgroundJobEnabled = true,
//            bShowDetails = true,
//            iValue = View.GONE
//        )
//        formatButtonUpdateVisibilityBase(
//            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_INITIAL,
//            bIsBackgroundJobEnabled = true,
//            bShowDetails = true,
//            iValue = View.GONE
//        )
//
//        formatButtonUpdateVisibilityBase(
//            iRiskLevelScore = RiskLevelConstants.INCREASED_RISK,
//            bIsBackgroundJobEnabled = false,
//            bShowDetails = true,
//            iValue = View.GONE
//        )
//        formatButtonUpdateVisibilityBase(
//            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS,
//            bIsBackgroundJobEnabled = false,
//            bShowDetails = true,
//            iValue = View.GONE
//        )
//        formatButtonUpdateVisibilityBase(
//            iRiskLevelScore = RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF,
//            bIsBackgroundJobEnabled = false,
//            bShowDetails = true,
//            iValue = View.GONE
//        )
//        formatButtonUpdateVisibilityBase(
//            iRiskLevelScore = RiskLevelConstants.LOW_LEVEL_RISK,
//            bIsBackgroundJobEnabled = false,
//            bShowDetails = true,
//            iValue = View.GONE
//        )
//        formatButtonUpdateVisibilityBase(
//            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_INITIAL,
//            bIsBackgroundJobEnabled = false,
//            bShowDetails = true,
//            iValue = View.GONE
//        )
//
//        formatButtonUpdateVisibilityBase(
//            iRiskLevelScore = RiskLevelConstants.INCREASED_RISK,
//            bIsBackgroundJobEnabled = false,
//            bShowDetails = false,
//            iValue = View.VISIBLE
//        )
//        formatButtonUpdateVisibilityBase(
//            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS,
//            bIsBackgroundJobEnabled = false,
//            bShowDetails = false,
//            iValue = View.GONE
//        )
//        formatButtonUpdateVisibilityBase(
//            iRiskLevelScore = RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF,
//            bIsBackgroundJobEnabled = false,
//            bShowDetails = false,
//            iValue = View.GONE
//        )
//        formatButtonUpdateVisibilityBase(
//            iRiskLevelScore = RiskLevelConstants.LOW_LEVEL_RISK,
//            bIsBackgroundJobEnabled = false,
//            bShowDetails = false,
//            iValue = View.VISIBLE
//        )
//        formatButtonUpdateVisibilityBase(
//            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_INITIAL,
//            bIsBackgroundJobEnabled = false,
//            bShowDetails = false,
//            iValue = View.VISIBLE
//        )
//
//        formatButtonUpdateVisibilityBase(
//            iRiskLevelScore = null,
//            bIsBackgroundJobEnabled = true,
//            bShowDetails = true,
//            iValue = View.GONE
//        )
//        formatButtonUpdateVisibilityBase(
//            iRiskLevelScore = null,
//            bIsBackgroundJobEnabled = false,
//            bShowDetails = true,
//            iValue = View.GONE
//        )
//    }
    }

    @Test
    fun `risklevel headline is affected by score and refreshingstate`() {
        TODO("getRiskLevelHeadline")
//         private fun formatRiskLevelHeadlineBase(
//        iRiskLevelScore: Int?,
//        bIsRefreshing: Boolean?,
//        sValue: String
//    ) {
//        val result =
//            formatRiskLevelHeadline(riskLevelScore = iRiskLevelScore, isRefreshing = bIsRefreshing)
//        assertThat(
//            result, `is`(sValue)
//        )
//    }
//    @Test
//    fun formatRiskLevelHeadline() {
//        formatRiskLevelHeadlineBase(iRiskLevelScore = null, bIsRefreshing = null, sValue = "")
//        formatRiskLevelHeadlineBase(
//            iRiskLevelScore = RiskLevelConstants.INCREASED_RISK,
//            bIsRefreshing = null,
//            sValue = R.string.risk_card_increased_risk_headline.toString()
//        )
//        formatRiskLevelHeadlineBase(
//            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS,
//            bIsRefreshing = null,
//            sValue = R.string.risk_card_outdated_risk_headline.toString()
//        )
//        formatRiskLevelHeadlineBase(
//            iRiskLevelScore = RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF,
//            bIsRefreshing = null,
//            sValue = R.string.risk_card_no_calculation_possible_headline.toString()
//        )
//        formatRiskLevelHeadlineBase(
//            iRiskLevelScore = RiskLevelConstants.LOW_LEVEL_RISK,
//            bIsRefreshing = null,
//            sValue = R.string.risk_card_low_risk_headline.toString()
//        )
//        formatRiskLevelHeadlineBase(
//            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_INITIAL,
//            bIsRefreshing = null,
//            sValue = R.string.risk_card_unknown_risk_headline.toString()
//        )
//        formatRiskLevelHeadlineBase(
//            iRiskLevelScore = RiskLevelConstants.INCREASED_RISK,
//            bIsRefreshing = true,
//            sValue = R.string.risk_card_loading_headline.toString()
//        )
//        formatRiskLevelHeadlineBase(
//            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS,
//            bIsRefreshing = true,
//            sValue = R.string.risk_card_loading_headline.toString()
//        )
//        formatRiskLevelHeadlineBase(
//            iRiskLevelScore = RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF,
//            bIsRefreshing = true,
//            sValue = R.string.risk_card_loading_headline.toString()
//        )
//        formatRiskLevelHeadlineBase(
//            iRiskLevelScore = RiskLevelConstants.LOW_LEVEL_RISK,
//            bIsRefreshing = true,
//            sValue = R.string.risk_card_loading_headline.toString()
//        )
//        formatRiskLevelHeadlineBase(
//            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_INITIAL,
//            bIsRefreshing = true,
//            sValue = R.string.risk_card_loading_headline.toString()
//        )
//    }
    }
}
