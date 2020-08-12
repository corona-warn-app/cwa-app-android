package de.rki.coronawarnapp.util.formatter

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.view.View
import de.rki.coronawarnapp.CoronaWarnApplication
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.risk.RiskLevelConstants
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkObject
import io.mockk.unmockkAll
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.Date

class FormatterRiskHelperTest {

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var resources: Resources

    @MockK
    private lateinit var colorStateList: ColorStateList

    @MockK
    private lateinit var drawable: Drawable

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        mockkObject(CoronaWarnApplication.Companion)

        every { CoronaWarnApplication.getAppContext() } returns context
        every { context.resources } returns resources
        every { context.getString(R.string.risk_card_increased_risk_headline) } returns R.string.risk_card_increased_risk_headline.toString()
        every { context.getString(R.string.risk_card_loading_headline) } returns R.string.risk_card_loading_headline.toString()
        every { context.getString(R.string.risk_card_outdated_risk_headline) } returns R.string.risk_card_outdated_risk_headline.toString()
        every { context.getString(R.string.risk_card_no_calculation_possible_headline) } returns R.string.risk_card_no_calculation_possible_headline.toString()
        every { context.getString(R.string.risk_card_low_risk_headline) } returns R.string.risk_card_low_risk_headline.toString()
        every { context.getString(R.string.risk_card_unknown_risk_headline) } returns R.string.risk_card_unknown_risk_headline.toString()
        every { resources.getQuantityString(any(), any(), any()) } returns "plural"
        every { context.getColor(R.color.colorAccentTintIcon) } returns R.color.colorAccentTintIcon
        every { context.getColor(R.color.colorStableLight) } returns R.color.colorStableLight
        every { context.getColor(R.color.colorSemanticHighRisk) } returns R.color.colorSemanticHighRisk
        every { context.getColor(R.color.colorSemanticLowRisk) } returns R.color.colorSemanticLowRisk
        every { context.getColor(R.color.colorSemanticNeutralRisk) } returns R.color.colorSemanticNeutralRisk
        every { context.getColor(R.color.colorSurface2) } returns R.color.colorSurface2
        every { context.getString(R.string.risk_card_button_update) } returns R.string.risk_card_button_update.toString()
        every { context.getString(R.string.risk_card_button_cooldown) } returns R.string.risk_card_button_cooldown.toString()
        every { context.getColor(R.color.colorTextSemanticNeutral) } returns R.color.colorTextSemanticNeutral
    }

    private fun formatRiskLevelHeadlineBase(iRiskLevelScore: Int?, bIsRefreshing: Boolean?, sValue: String) {
        val result = formatRiskLevelHeadline(riskLevelScore = iRiskLevelScore, isRefreshing = bIsRefreshing)
        assertThat(
            result, `is`(sValue)
        )
    }

    private fun formatRiskBodyBase(iRiskLevelScore: Int?, sValue: String) {
        every { context.getString(R.string.risk_card_outdated_risk_body) } returns R.string.risk_card_outdated_risk_body.toString()
        every { context.getString(R.string.risk_card_body_tracing_off) } returns R.string.risk_card_body_tracing_off.toString()
        every { context.getString(R.string.risk_card_unknown_risk_body) } returns R.string.risk_card_unknown_risk_body.toString()

        val result = formatRiskBody(riskLevelScore = iRiskLevelScore)
        assertThat(
            result, `is`(sValue)
        )
    }

    private fun formatRiskSavedRiskBase(
        iRiskLevelScore: Int?,
        iRiskLevelScoreLastSuccessfulCalculated: Int?,
        sValue: String
    ) {
        every { context.getString(R.string.risk_card_no_calculation_possible_body_saved_risk) } returns R.string.risk_card_no_calculation_possible_body_saved_risk.toString()

        val result = formatRiskSavedRisk(
            riskLevelScore = iRiskLevelScore,
            riskLevelScoreLastSuccessfulCalculated = iRiskLevelScoreLastSuccessfulCalculated
        )
        assertThat(
            result, `is`(sValue)
        )
    }

    private fun formatRiskContactBase(iRiskLevelScore: Int?, iMatchedKeysCount: Int?, sValue: String) {
        every { context.getString(R.string.risk_card_body_contact) } returns R.string.risk_card_body_contact.toString()

        val result = formatRiskContact(riskLevelScore = iRiskLevelScore, matchedKeysCount = iMatchedKeysCount)
        assertThat(
            result, `is`(sValue)
        )
    }

    private fun formatRiskContactLastBase(iRiskLevelScore: Int?, iDaysSinceLastExposure: Int?, sValue: String) {
        every { context.getString(R.string.risk_card_body_contact) } returns R.string.risk_card_body_contact.toString()

        val result =
            formatRiskContactLast(riskLevelScore = iRiskLevelScore, daysSinceLastExposure = iDaysSinceLastExposure)
        assertThat(
            result, `is`(sValue)
        )
    }

    private fun formatRiskActiveTracingDaysInRetentionPeriodBase(
        iRiskLevelScore: Int?,
        bShowDetails: Boolean,
        lActiveTracingDaysInRetentionPeriod: Long,
        sValue: String
    ) {
        every { context.getString(R.string.risk_card_body_saved_days) } returns R.string.risk_card_body_saved_days.toString()

        val result = formatRiskActiveTracingDaysInRetentionPeriod(
            riskLevelScore = iRiskLevelScore,
            showDetails = bShowDetails,
            activeTracingDaysInRetentionPeriod = lActiveTracingDaysInRetentionPeriod
        )
        assertThat(
            result, `is`(sValue)
        )
    }

    private fun formatTimeFetchedBase(
        iRiskLevelScore: Int?,
        iRiskLevelScoreLastSuccessfulCalculated: Int?,
        dLastTimeDiagnosisKeysFetched: Date?,
        sValue: String
    ) {
        every {
            context.getString(
                R.string.risk_card_body_time_fetched,
                formatRelativeDateTimeString(context, Date())
            )
        } returns R.string.risk_card_body_time_fetched.toString()
        every { context.getString(R.string.risk_card_body_not_yet_fetched) } returns R.string.risk_card_body_not_yet_fetched.toString()

        val result = formatTimeFetched(
            riskLevelScore = iRiskLevelScore,
            riskLevelScoreLastSuccessfulCalculated = iRiskLevelScoreLastSuccessfulCalculated,
            lastTimeDiagnosisKeysFetched = dLastTimeDiagnosisKeysFetched
        )
        assertThat(
            result, `is`(sValue)
        )
    }

    private fun formatNextUpdateBase(
        iRiskLevelScore: Int?,
        bIsBackgroundJobEnabled: Boolean?,
        sValue: String
    ) {
        every { context.getString(R.string.risk_card_body_next_update) } returns R.string.risk_card_body_next_update.toString()

        val result =
            formatNextUpdate(
                riskLevelScore = iRiskLevelScore,
                isBackgroundJobEnabled = bIsBackgroundJobEnabled
            )
        assertThat(
            result, `is`(sValue)
        )
    }

    private fun formatNextUpdateContentDescriptionBase(
        iRiskLevelScore: Int?,
        bIsBackgroundJobEnabled: Boolean?,
        sValue: String
    ) {
        every { context.getString(R.string.risk_card_body_next_update) } returns R.string.risk_card_body_next_update.toString()
        every { context.getString(R.string.accessibility_button) } returns R.string.accessibility_button.toString()

        val result =
            formatNextUpdateContentDescription(
                riskLevelScore = iRiskLevelScore,
                isBackgroundJobEnabled = bIsBackgroundJobEnabled
            )
        assertThat(
            result, `is`(sValue)
        )
    }

    private fun formatRiskDetailsRiskLevelBodyBase(
        iRiskLevelScore: Int?,
        iDaysSinceLastExposure: Int?,
        sValue: String
    ) {
        every { context.getString(R.string.risk_details_information_body_outdated_risk) } returns R.string.risk_details_information_body_outdated_risk.toString()
        every { context.getString(R.string.risk_details_information_body_low_risk) } returns R.string.risk_details_information_body_low_risk.toString()
        every { context.getString(R.string.risk_details_information_body_unknown_risk) } returns R.string.risk_details_information_body_unknown_risk.toString()

        val result = formatRiskDetailsRiskLevelBody(
            riskLevelScore = iRiskLevelScore,
            daysSinceLastExposure = iDaysSinceLastExposure
        )
        assertThat(
            result, `is`(sValue)
        )
    }

    private fun formatRiskColorStateListBase(iRiskLevelScore: Int?) {
        every { context.getColorStateList(R.color.card_increased) } returns colorStateList
        every { context.getColorStateList(R.color.card_outdated) } returns colorStateList
        every { context.getColorStateList(R.color.card_no_calculation) } returns colorStateList
        every { context.getColorStateList(R.color.card_low) } returns colorStateList
        every { context.getColorStateList(R.color.card_unknown) } returns colorStateList

        val result = formatRiskColorStateList(riskLevelScore = iRiskLevelScore)
        assertThat(
            result, `is`(colorStateList)
        )
    }

    private fun formatRiskColorBase(iRiskLevelScore: Int?, iValue: Int) {
        every { context.getColor(R.color.colorSemanticNeutralRisk) } returns R.color.colorSemanticNeutralRisk
        every { context.getColor(R.color.colorSemanticHighRisk) } returns R.color.colorSemanticHighRisk
        every { context.getColor(R.color.colorSemanticUnknownRisk) } returns R.color.colorSemanticUnknownRisk
        every { context.getColor(R.color.colorSemanticLowRisk) } returns R.color.colorSemanticLowRisk

        val result = formatRiskColor(riskLevelScore = iRiskLevelScore)
        assertThat(
            result, `is`(iValue)
        )
    }

    private fun formatRiskShapeBase(bShowDetails: Boolean) {
        every { context.getDrawable(any()) } returns drawable

        val result = formatRiskShape(showDetails = bShowDetails)
        assertThat(
            result, `is`(drawable)
        )
    }

    private fun formatStableIconColorBase(iRiskLevelScore: Int?) {
        every { context.getColor(any()) } returns R.color.colorStableLight

        val result = formatStableIconColor(riskLevelScore = iRiskLevelScore)
        assertThat(
            result, `is`(R.color.colorStableLight)
        )
    }

    private fun formatStableTextColorBase(iRiskLevelScore: Int?) {
        every { context.getColor(any()) } returns R.color.colorStableLight

        val result = formatStableTextColor(riskLevelScore = iRiskLevelScore)
        assertThat(
            result, `is`(R.color.colorStableLight)
        )
    }

    private fun formatStableDividerColorBase(iRiskLevelScore: Int?) {
        every { context.getColor(any()) } returns R.color.colorStableLight

        val result = formatStableDividerColor(riskLevelScore = iRiskLevelScore)
        assertThat(
            result, `is`(R.color.colorStableLight)
        )
    }

    private fun formatRiskContactIconBase(iRiskLevelScore: Int?) {
        every { context.getDrawable(any()) } returns drawable

        val result = formatRiskContactIcon(riskLevelScore = iRiskLevelScore)
        assertThat(
            result, `is`(drawable)
        )
    }

    private fun formatButtonEnableTracingVisibilityBase(iRiskLevelScore: Int?, bShowDetails: Boolean?, iValue: Int) {
        val result = formatButtonEnableTracingVisibility(riskLevelScore = iRiskLevelScore, showDetails = bShowDetails)
        assertThat(
            result, `is`(iValue)
        )
    }

    private fun formatRiskDetailsButtonEnableTracingVisibilityBase(iRiskLevelScore: Int?, iValue: Int) {
        val result = formatRiskDetailsButtonEnableTracingVisibility(riskLevelScore = iRiskLevelScore)
        assertThat(
            result, `is`(iValue)
        )
    }

    private fun formatRiskDetailsButtonVisibilityBase(
        iRiskLevelScore: Int?,
        bIsBackgroundJobEnabled: Boolean?,
        iValue: Int
    ) {
        val result = formatRiskDetailsButtonVisibility(
            riskLevelScore = iRiskLevelScore,
            isBackgroundJobEnabled = bIsBackgroundJobEnabled
        )
        assertThat(
            result, `is`(iValue)
        )
    }

    private fun formatButtonUpdateVisibilityBase(
        iRiskLevelScore: Int?,
        bIsBackgroundJobEnabled: Boolean?,
        bShowDetails: Boolean?,
        iValue: Int
    ) {
        val result = formatButtonUpdateVisibility(
            riskLevelScore = iRiskLevelScore,
            isBackgroundJobEnabled = bIsBackgroundJobEnabled,
            showDetails = bShowDetails
        )
        assertThat(
            result, `is`(iValue)
        )
    }

    private fun formatDetailsButtonUpdateVisibilityBase(
        iRiskLevelScore: Int?,
        bIsBackgroundJobEnabled: Boolean?,
        iValue: Int
    ) {
        val result = formatDetailsButtonUpdateVisibility(
            riskLevelScore = iRiskLevelScore,
            isBackgroundJobEnabled = bIsBackgroundJobEnabled
        )
        assertThat(
            result, `is`(iValue)
        )
    }

    private fun formatVisibilityBehaviorBase(iRiskLevelScore: Int?, iValue: Int) {
        val result = formatVisibilityBehavior(riskLevelScore = iRiskLevelScore)
        assertThat(
            result, `is`(iValue)
        )
    }

    private fun formatVisibilityBehaviorIncreasedRiskBase(iRiskLevelScore: Int?, iValue: Int) {
        val result = formatVisibilityBehaviorIncreasedRisk(riskLevelScore = iRiskLevelScore)
        assertThat(
            result, `is`(iValue)
        )
    }

    private fun formatBehaviorIconBase(iRiskLevelScore: Int?, iValue: Int) {
        val result = formatBehaviorIcon(riskLevelScore = iRiskLevelScore)
        assertThat(
            result, `is`(iValue)
        )
    }

    private fun formatBehaviorIconBackgroundBase(iRiskLevelScore: Int?, iValue: Int) {
        val result = formatBehaviorIconBackground(riskLevelScore = iRiskLevelScore)
        assertThat(
            result, `is`(iValue)
        )
    }

    private fun formatButtonUpdateEnabledBase(bEnabled: Boolean?, bValue: Boolean) {
        val result = formatButtonUpdateEnabled(enabled = bEnabled)
        assertThat(
            result, `is`(bValue)
        )
    }

    private fun formatButtonUpdateTextBase(lTime: Long, sValue: String) {
        val result = formatButtonUpdateText(time = lTime)
        assertThat(
            result, `is`(sValue)
        )
    }

    @Test
    fun formatRiskLevelHeadline() {
        formatRiskLevelHeadlineBase(iRiskLevelScore = null, bIsRefreshing = null, sValue = "")
        formatRiskLevelHeadlineBase(
            iRiskLevelScore = RiskLevelConstants.INCREASED_RISK,
            bIsRefreshing = null,
            sValue = R.string.risk_card_increased_risk_headline.toString()
        )
        formatRiskLevelHeadlineBase(
            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS,
            bIsRefreshing = null,
            sValue = R.string.risk_card_outdated_risk_headline.toString()
        )
        formatRiskLevelHeadlineBase(
            iRiskLevelScore = RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF,
            bIsRefreshing = null,
            sValue = R.string.risk_card_no_calculation_possible_headline.toString()
        )
        formatRiskLevelHeadlineBase(
            iRiskLevelScore = RiskLevelConstants.LOW_LEVEL_RISK,
            bIsRefreshing = null,
            sValue = R.string.risk_card_low_risk_headline.toString()
        )
        formatRiskLevelHeadlineBase(
            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_INITIAL,
            bIsRefreshing = null,
            sValue = R.string.risk_card_unknown_risk_headline.toString()
        )
        formatRiskLevelHeadlineBase(
            iRiskLevelScore = RiskLevelConstants.INCREASED_RISK,
            bIsRefreshing = true,
            sValue = R.string.risk_card_loading_headline.toString()
        )
        formatRiskLevelHeadlineBase(
            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS,
            bIsRefreshing = true,
            sValue = R.string.risk_card_loading_headline.toString()
        )
        formatRiskLevelHeadlineBase(
            iRiskLevelScore = RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF,
            bIsRefreshing = true,
            sValue = R.string.risk_card_loading_headline.toString()
        )
        formatRiskLevelHeadlineBase(
            iRiskLevelScore = RiskLevelConstants.LOW_LEVEL_RISK,
            bIsRefreshing = true,
            sValue = R.string.risk_card_loading_headline.toString()
        )
        formatRiskLevelHeadlineBase(
            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_INITIAL,
            bIsRefreshing = true,
            sValue = R.string.risk_card_loading_headline.toString()
        )
    }

    @Test
    fun formatRiskBody() {
        formatRiskBodyBase(iRiskLevelScore = null, sValue = "")
        formatRiskBodyBase(iRiskLevelScore = RiskLevelConstants.INCREASED_RISK, sValue = "")
        formatRiskBodyBase(
            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS,
            sValue = R.string.risk_card_outdated_risk_body.toString()
        )
        formatRiskBodyBase(
            iRiskLevelScore = RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF,
            sValue = R.string.risk_card_body_tracing_off.toString()
        )
        formatRiskBodyBase(iRiskLevelScore = RiskLevelConstants.LOW_LEVEL_RISK, sValue = "")
        formatRiskBodyBase(
            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_INITIAL,
            sValue = R.string.risk_card_unknown_risk_body.toString()
        )
    }

    @Test
    fun formatRiskSavedRisk() {
        formatRiskSavedRiskBase(iRiskLevelScore = null, iRiskLevelScoreLastSuccessfulCalculated = null, sValue = "")
        formatRiskSavedRiskBase(
            iRiskLevelScore = RiskLevelConstants.INCREASED_RISK,
            iRiskLevelScoreLastSuccessfulCalculated = null,
            sValue = ""
        )
        formatRiskSavedRiskBase(
            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS,
            iRiskLevelScoreLastSuccessfulCalculated = null,
            sValue = ""
        )
        formatRiskSavedRiskBase(
            iRiskLevelScore = RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF,
            iRiskLevelScoreLastSuccessfulCalculated = null,
            sValue = ""
        )
        formatRiskSavedRiskBase(
            iRiskLevelScore = RiskLevelConstants.LOW_LEVEL_RISK,
            iRiskLevelScoreLastSuccessfulCalculated = null,
            sValue = ""
        )
        formatRiskSavedRiskBase(
            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_INITIAL,
            iRiskLevelScoreLastSuccessfulCalculated = null,
            sValue = ""
        )

        formatRiskSavedRiskBase(
            iRiskLevelScore = RiskLevelConstants.INCREASED_RISK,
            iRiskLevelScoreLastSuccessfulCalculated = RiskLevelConstants.INCREASED_RISK,
            sValue = ""
        )
        formatRiskSavedRiskBase(
            iRiskLevelScore = RiskLevelConstants.INCREASED_RISK,
            iRiskLevelScoreLastSuccessfulCalculated = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS,
            sValue = ""
        )
        formatRiskSavedRiskBase(
            iRiskLevelScore = RiskLevelConstants.INCREASED_RISK,
            iRiskLevelScoreLastSuccessfulCalculated = RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF,
            sValue = ""
        )
        formatRiskSavedRiskBase(
            iRiskLevelScore = RiskLevelConstants.INCREASED_RISK,
            iRiskLevelScoreLastSuccessfulCalculated = RiskLevelConstants.LOW_LEVEL_RISK,
            sValue = ""
        )
        formatRiskSavedRiskBase(
            iRiskLevelScore = RiskLevelConstants.INCREASED_RISK,
            iRiskLevelScoreLastSuccessfulCalculated = RiskLevelConstants.UNKNOWN_RISK_INITIAL,
            sValue = ""
        )

        formatRiskSavedRiskBase(
            iRiskLevelScore = RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF,
            iRiskLevelScoreLastSuccessfulCalculated = RiskLevelConstants.LOW_LEVEL_RISK,
            sValue = context.getString(R.string.risk_card_no_calculation_possible_body_saved_risk)
                .format(
                    formatRiskLevelHeadline(
                        riskLevelScore = RiskLevelConstants.LOW_LEVEL_RISK,
                        isRefreshing = false
                    )
                )
        )
        formatRiskSavedRiskBase(
            iRiskLevelScore = RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF,
            iRiskLevelScoreLastSuccessfulCalculated = RiskLevelConstants.INCREASED_RISK,
            sValue = context.getString(R.string.risk_card_no_calculation_possible_body_saved_risk)
                .format(
                    formatRiskLevelHeadline(
                        riskLevelScore = RiskLevelConstants.INCREASED_RISK,
                        isRefreshing = false
                    )
                )
        )
        formatRiskSavedRiskBase(
            iRiskLevelScore = RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF,
            iRiskLevelScoreLastSuccessfulCalculated = RiskLevelConstants.UNKNOWN_RISK_INITIAL,
            sValue = context.getString(R.string.risk_card_no_calculation_possible_body_saved_risk)
                .format(
                    formatRiskLevelHeadline(
                        riskLevelScore = RiskLevelConstants.UNKNOWN_RISK_INITIAL,
                        isRefreshing = false
                    )
                )
        )

        formatRiskSavedRiskBase(
            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS,
            iRiskLevelScoreLastSuccessfulCalculated = RiskLevelConstants.LOW_LEVEL_RISK,
            sValue = context.getString(R.string.risk_card_no_calculation_possible_body_saved_risk)
                .format(
                    formatRiskLevelHeadline(
                        riskLevelScore = RiskLevelConstants.LOW_LEVEL_RISK,
                        isRefreshing = false
                    )
                )
        )
        formatRiskSavedRiskBase(
            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS,
            iRiskLevelScoreLastSuccessfulCalculated = RiskLevelConstants.INCREASED_RISK,
            sValue = context.getString(R.string.risk_card_no_calculation_possible_body_saved_risk)
                .format(
                    formatRiskLevelHeadline(
                        riskLevelScore = RiskLevelConstants.INCREASED_RISK,
                        isRefreshing = false
                    )
                )
        )
        formatRiskSavedRiskBase(
            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS,
            iRiskLevelScoreLastSuccessfulCalculated = RiskLevelConstants.UNKNOWN_RISK_INITIAL,
            sValue = context.getString(R.string.risk_card_no_calculation_possible_body_saved_risk)
                .format(
                    formatRiskLevelHeadline(
                        riskLevelScore = RiskLevelConstants.UNKNOWN_RISK_INITIAL,
                        isRefreshing = false
                    )
                )
        )
    }

    @Test
    fun formatRiskContact() {
        formatRiskContactBase(iRiskLevelScore = null, iMatchedKeysCount = null, sValue = "")
        formatRiskContactBase(iRiskLevelScore = null, iMatchedKeysCount = 0, sValue = "")

        formatRiskContactBase(
            iRiskLevelScore = RiskLevelConstants.INCREASED_RISK,
            iMatchedKeysCount = 0,
            sValue = context.getString(R.string.risk_card_body_contact)
        )
        formatRiskContactBase(
            iRiskLevelScore = RiskLevelConstants.INCREASED_RISK,
            iMatchedKeysCount = 2,
            sValue = context.resources.getQuantityString(
                R.plurals.risk_card_body_contact_value,
                2,
                2
            )
        )

        formatRiskContactBase(
            iRiskLevelScore = RiskLevelConstants.LOW_LEVEL_RISK,
            iMatchedKeysCount = 0,
            sValue = context.getString(R.string.risk_card_body_contact)
        )
        formatRiskContactBase(
            iRiskLevelScore = RiskLevelConstants.LOW_LEVEL_RISK,
            iMatchedKeysCount = 2,
            sValue = context.resources.getQuantityString(
                R.plurals.risk_card_body_contact_value,
                2,
                2
            )
        )

        formatRiskContactBase(
            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS,
            iMatchedKeysCount = 0,
            sValue = ""
        )
        formatRiskContactBase(
            iRiskLevelScore = RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF,
            iMatchedKeysCount = 0,
            sValue = ""
        )
        formatRiskContactBase(
            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_INITIAL,
            iMatchedKeysCount = 0,
            sValue = ""
        )

        formatRiskContactBase(
            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS,
            iMatchedKeysCount = 2,
            sValue = ""
        )
        formatRiskContactBase(
            iRiskLevelScore = RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF,
            iMatchedKeysCount = 2,
            sValue = ""
        )
        formatRiskContactBase(
            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_INITIAL,
            iMatchedKeysCount = 2,
            sValue = ""
        )
    }

    @Test
    fun formatRiskContactLast() {
        formatRiskContactLastBase(iRiskLevelScore = null, iDaysSinceLastExposure = 2, sValue = "")
        formatRiskContactLastBase(
            iRiskLevelScore = RiskLevelConstants.INCREASED_RISK,
            iDaysSinceLastExposure = 2,
            sValue = context.resources.getQuantityString(
                R.plurals.risk_card_increased_risk_body_contact_last,
                2,
                2
            )
        )
        formatRiskContactLastBase(
            iRiskLevelScore = RiskLevelConstants.INCREASED_RISK,
            iDaysSinceLastExposure = null,
            sValue = context.resources.getQuantityString(
                R.plurals.risk_card_increased_risk_body_contact_last,
                0,
                0
            )
        )
        formatRiskContactLastBase(
            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS,
            iDaysSinceLastExposure = 2,
            sValue = ""
        )
        formatRiskContactLastBase(
            iRiskLevelScore = RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF,
            iDaysSinceLastExposure = 2,
            sValue = ""
        )
        formatRiskContactLastBase(
            iRiskLevelScore = RiskLevelConstants.LOW_LEVEL_RISK,
            iDaysSinceLastExposure = 2,
            sValue = ""
        )
        formatRiskContactLastBase(
            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_INITIAL,
            iDaysSinceLastExposure = 2,
            sValue = ""
        )
    }

    @Test
    fun formatRiskActiveTracingDaysInRetentionPeriod() {
        formatRiskActiveTracingDaysInRetentionPeriodBase(
            iRiskLevelScore = null,
            bShowDetails = false,
            lActiveTracingDaysInRetentionPeriod = 1,
            sValue = ""
        )
        formatRiskActiveTracingDaysInRetentionPeriodBase(
            iRiskLevelScore = RiskLevelConstants.INCREASED_RISK,
            bShowDetails = false,
            lActiveTracingDaysInRetentionPeriod = 1,
            sValue = ""
        )
        formatRiskActiveTracingDaysInRetentionPeriodBase(
            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS,
            bShowDetails = false,
            lActiveTracingDaysInRetentionPeriod = 1,
            sValue = ""
        )
        formatRiskActiveTracingDaysInRetentionPeriodBase(
            iRiskLevelScore = RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF,
            bShowDetails = false,
            lActiveTracingDaysInRetentionPeriod = 1,
            sValue = ""
        )
        formatRiskActiveTracingDaysInRetentionPeriodBase(
            iRiskLevelScore = RiskLevelConstants.LOW_LEVEL_RISK,
            bShowDetails = false,
            lActiveTracingDaysInRetentionPeriod = 1,
            sValue = context.getString(
                R.string.risk_card_body_saved_days
            )
                .format(1)
        )
        formatRiskActiveTracingDaysInRetentionPeriodBase(
            iRiskLevelScore = RiskLevelConstants.LOW_LEVEL_RISK,
            bShowDetails = false,
            lActiveTracingDaysInRetentionPeriod = 2,
            sValue = context.getString(
                R.string.risk_card_body_saved_days
            )
                .format(2)
        )
        formatRiskActiveTracingDaysInRetentionPeriodBase(
            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_INITIAL,
            bShowDetails = false,
            lActiveTracingDaysInRetentionPeriod = 1,
            sValue = ""
        )

        formatRiskActiveTracingDaysInRetentionPeriodBase(
            iRiskLevelScore = null,
            bShowDetails = true,
            lActiveTracingDaysInRetentionPeriod = 1,
            sValue = ""
        )
        formatRiskActiveTracingDaysInRetentionPeriodBase(
            iRiskLevelScore = RiskLevelConstants.INCREASED_RISK,
            bShowDetails = true,
            lActiveTracingDaysInRetentionPeriod = 1,
            sValue = context.getString(
                R.string.risk_card_body_saved_days
            )
                .format(1)
        )
        formatRiskActiveTracingDaysInRetentionPeriodBase(
            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS,
            bShowDetails = true,
            lActiveTracingDaysInRetentionPeriod = 1,
            sValue = ""
        )
        formatRiskActiveTracingDaysInRetentionPeriodBase(
            iRiskLevelScore = RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF,
            bShowDetails = true,
            lActiveTracingDaysInRetentionPeriod = 1,
            sValue = ""
        )
        formatRiskActiveTracingDaysInRetentionPeriodBase(
            iRiskLevelScore = RiskLevelConstants.LOW_LEVEL_RISK,
            bShowDetails = true,
            lActiveTracingDaysInRetentionPeriod = 1,
            sValue = context.getString(
                R.string.risk_card_body_saved_days
            )
                .format(1)
        )
        formatRiskActiveTracingDaysInRetentionPeriodBase(
            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_INITIAL,
            bShowDetails = true,
            lActiveTracingDaysInRetentionPeriod = 1,
            sValue = ""
        )
    }

    @Test
    fun formatTimeFetched() {
        formatTimeFetchedBase(
            iRiskLevelScore = null,
            iRiskLevelScoreLastSuccessfulCalculated = 2,
            dLastTimeDiagnosisKeysFetched = Date(),
            sValue = ""
        )
        formatTimeFetchedBase(
            iRiskLevelScore = RiskLevelConstants.INCREASED_RISK,
            iRiskLevelScoreLastSuccessfulCalculated = 2,
            dLastTimeDiagnosisKeysFetched = Date(),
            sValue = context.getString(
                R.string.risk_card_body_time_fetched,
                formatRelativeDateTimeString(context, Date())
            )
        )
        formatTimeFetchedBase(
            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS,
            iRiskLevelScoreLastSuccessfulCalculated = 2,
            dLastTimeDiagnosisKeysFetched = Date(),
            sValue = context.getString(
                R.string.risk_card_body_time_fetched,
                formatRelativeDateTimeString(context, Date())
            )
        )
        formatTimeFetchedBase(
            iRiskLevelScore = RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF,
            iRiskLevelScoreLastSuccessfulCalculated = 2,
            dLastTimeDiagnosisKeysFetched = Date(),
            sValue = context.getString(
                R.string.risk_card_body_time_fetched,
                formatRelativeDateTimeString(context, Date())
            )
        )
        formatTimeFetchedBase(
            iRiskLevelScore = RiskLevelConstants.LOW_LEVEL_RISK,
            iRiskLevelScoreLastSuccessfulCalculated = 2,
            dLastTimeDiagnosisKeysFetched = Date(),
            sValue = context.getString(
                R.string.risk_card_body_time_fetched,
                formatRelativeDateTimeString(context, Date())
            )
        )
        formatTimeFetchedBase(
            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_INITIAL,
            iRiskLevelScoreLastSuccessfulCalculated = 2,
            dLastTimeDiagnosisKeysFetched = Date(),
            sValue = ""
        )

        formatTimeFetchedBase(
            iRiskLevelScore = null,
            iRiskLevelScoreLastSuccessfulCalculated = null,
            dLastTimeDiagnosisKeysFetched = Date(),
            sValue = ""
        )
        formatTimeFetchedBase(
            iRiskLevelScore = RiskLevelConstants.INCREASED_RISK,
            iRiskLevelScoreLastSuccessfulCalculated = null,
            dLastTimeDiagnosisKeysFetched = Date(),
            sValue = context.getString(
                R.string.risk_card_body_time_fetched,
                formatRelativeDateTimeString(context, Date())
            )
        )
        formatTimeFetchedBase(
            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS,
            iRiskLevelScoreLastSuccessfulCalculated = null,
            dLastTimeDiagnosisKeysFetched = Date(),
            sValue = ""
        )
        formatTimeFetchedBase(
            iRiskLevelScore = RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF,
            iRiskLevelScoreLastSuccessfulCalculated = null,
            dLastTimeDiagnosisKeysFetched = Date(),
            sValue = ""
        )
        formatTimeFetchedBase(
            iRiskLevelScore = RiskLevelConstants.LOW_LEVEL_RISK,
            iRiskLevelScoreLastSuccessfulCalculated = null,
            dLastTimeDiagnosisKeysFetched = Date(),
            sValue = context.getString(
                R.string.risk_card_body_time_fetched,
                formatRelativeDateTimeString(context, Date())
            )
        )
        formatTimeFetchedBase(
            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_INITIAL,
            iRiskLevelScoreLastSuccessfulCalculated = null,
            dLastTimeDiagnosisKeysFetched = Date(),
            sValue = ""
        )

        formatTimeFetchedBase(
            iRiskLevelScore = null,
            iRiskLevelScoreLastSuccessfulCalculated = null,
            dLastTimeDiagnosisKeysFetched = null,
            sValue = ""
        )
        formatTimeFetchedBase(
            iRiskLevelScore = RiskLevelConstants.INCREASED_RISK,
            iRiskLevelScoreLastSuccessfulCalculated = null,
            dLastTimeDiagnosisKeysFetched = null,
            sValue = context.getString(R.string.risk_card_body_not_yet_fetched)
        )
        formatTimeFetchedBase(
            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS,
            iRiskLevelScoreLastSuccessfulCalculated = null,
            dLastTimeDiagnosisKeysFetched = null,
            sValue = ""
        )
        formatTimeFetchedBase(
            iRiskLevelScore = RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF,
            iRiskLevelScoreLastSuccessfulCalculated = null,
            dLastTimeDiagnosisKeysFetched = null,
            sValue = ""
        )
        formatTimeFetchedBase(
            iRiskLevelScore = RiskLevelConstants.LOW_LEVEL_RISK,
            iRiskLevelScoreLastSuccessfulCalculated = null,
            dLastTimeDiagnosisKeysFetched = null,
            sValue = context.getString(R.string.risk_card_body_not_yet_fetched)
        )
        formatTimeFetchedBase(
            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_INITIAL,
            iRiskLevelScoreLastSuccessfulCalculated = null,
            dLastTimeDiagnosisKeysFetched = null,
            sValue = ""
        )
    }

    @Test
    fun formatNextUpdate() {
        formatNextUpdateBase(iRiskLevelScore = null, bIsBackgroundJobEnabled = null, sValue = "")
        formatNextUpdateBase(
            iRiskLevelScore = RiskLevelConstants.INCREASED_RISK,
            bIsBackgroundJobEnabled = null,
            sValue = ""
        )
        formatNextUpdateBase(
            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS,
            bIsBackgroundJobEnabled = null,
            sValue = ""
        )
        formatNextUpdateBase(
            iRiskLevelScore = RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF,
            bIsBackgroundJobEnabled = null,
            sValue = ""
        )
        formatNextUpdateBase(
            iRiskLevelScore = RiskLevelConstants.LOW_LEVEL_RISK,
            bIsBackgroundJobEnabled = null,
            sValue = ""
        )
        formatNextUpdateBase(
            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_INITIAL,
            bIsBackgroundJobEnabled = null,
            sValue = ""
        )

        formatNextUpdateBase(iRiskLevelScore = null, bIsBackgroundJobEnabled = true, sValue = "")
        formatNextUpdateBase(
            iRiskLevelScore = RiskLevelConstants.INCREASED_RISK,
            bIsBackgroundJobEnabled = true,
            sValue = context.getString(
                R.string.risk_card_body_next_update
            )
        )
        formatNextUpdateBase(
            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS,
            bIsBackgroundJobEnabled = true,
            sValue = ""
        )
        formatNextUpdateBase(
            iRiskLevelScore = RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF,
            bIsBackgroundJobEnabled = true,
            sValue = ""
        )
        formatNextUpdateBase(
            iRiskLevelScore = RiskLevelConstants.LOW_LEVEL_RISK,
            bIsBackgroundJobEnabled = true,
            sValue = context.getString(
                R.string.risk_card_body_next_update
            )
        )
        formatNextUpdateBase(
            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_INITIAL,
            bIsBackgroundJobEnabled = true,
            sValue = context.getString(
                R.string.risk_card_body_next_update
            )
        )
    }

    @Test
    fun formatNextUpdateContentDescription() {
        formatNextUpdateContentDescriptionBase(
            iRiskLevelScore = null,
            bIsBackgroundJobEnabled = null,
            sValue = ""
        )
        formatNextUpdateContentDescriptionBase(
            iRiskLevelScore = RiskLevelConstants.INCREASED_RISK,
            bIsBackgroundJobEnabled = null,
            sValue = ""
        )
        formatNextUpdateContentDescriptionBase(
            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS,
            bIsBackgroundJobEnabled = null,
            sValue = ""
        )
        formatNextUpdateContentDescriptionBase(
            iRiskLevelScore = RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF,
            bIsBackgroundJobEnabled = null,
            sValue = ""
        )
        formatNextUpdateContentDescriptionBase(
            iRiskLevelScore = RiskLevelConstants.LOW_LEVEL_RISK,
            bIsBackgroundJobEnabled = null,
            sValue = ""
        )
        formatNextUpdateContentDescriptionBase(
            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_INITIAL,
            bIsBackgroundJobEnabled = null,
            sValue = ""
        )

        formatNextUpdateContentDescriptionBase(
            iRiskLevelScore = null,
            bIsBackgroundJobEnabled = true,
            sValue = ""
        )
        formatNextUpdateContentDescriptionBase(
            iRiskLevelScore = RiskLevelConstants.INCREASED_RISK,
            bIsBackgroundJobEnabled = true,
            sValue = context.getString(
                R.string.risk_card_body_next_update
            ) + " " + context.getString(
                R.string.accessibility_button
            )
        )
        formatNextUpdateContentDescriptionBase(
            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS,
            bIsBackgroundJobEnabled = true,
            sValue = ""
        )

        formatNextUpdateContentDescriptionBase(
            iRiskLevelScore = RiskLevelConstants.LOW_LEVEL_RISK,
            bIsBackgroundJobEnabled = true,
            sValue = context.getString(
                R.string.risk_card_body_next_update
            ) + " " + context.getString(
                R.string.accessibility_button
            )
        )
        formatNextUpdateContentDescriptionBase(
            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_INITIAL,
            bIsBackgroundJobEnabled = true,
            sValue = context.getString(
                R.string.risk_card_body_next_update
            ) + " " + context.getString(
                R.string.accessibility_button
            )
        )
    }

    @Test
    fun formatRiskDetailsRiskLevelBody() {
        formatRiskDetailsRiskLevelBodyBase(
            iRiskLevelScore = null,
            iDaysSinceLastExposure = 0,
            sValue = ""
        )
        formatRiskDetailsRiskLevelBodyBase(
            iRiskLevelScore = RiskLevelConstants.INCREASED_RISK,
            iDaysSinceLastExposure = 1,
            sValue = resources.getQuantityString(
                R.plurals.risk_details_information_body_increased_risk,
                1,
                1
            )
        )
        formatRiskDetailsRiskLevelBodyBase(
            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS,
            iDaysSinceLastExposure = 1,
            sValue = context.getString(R.string.risk_details_information_body_outdated_risk)
        )
        formatRiskDetailsRiskLevelBodyBase(
            iRiskLevelScore = RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF,
            iDaysSinceLastExposure = 1,
            sValue = ""
        )
        formatRiskDetailsRiskLevelBodyBase(
            iRiskLevelScore = RiskLevelConstants.LOW_LEVEL_RISK,
            iDaysSinceLastExposure = 1,
            sValue = context.getString(R.string.risk_details_information_body_low_risk)
        )
        formatRiskDetailsRiskLevelBodyBase(
            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_INITIAL,
            iDaysSinceLastExposure = 1,
            sValue = context.getString(R.string.risk_details_information_body_unknown_risk)
        )

        formatRiskDetailsRiskLevelBodyBase(iRiskLevelScore = null, iDaysSinceLastExposure = null, sValue = "")
        formatRiskDetailsRiskLevelBodyBase(
            iRiskLevelScore = RiskLevelConstants.INCREASED_RISK,
            iDaysSinceLastExposure = null,
            sValue = resources.getQuantityString(
                R.plurals.risk_details_information_body_increased_risk,
                0,
                0
            )
        )
        formatRiskDetailsRiskLevelBodyBase(
            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS,
            iDaysSinceLastExposure = null,
            sValue = context.getString(R.string.risk_details_information_body_outdated_risk)
        )
        formatRiskDetailsRiskLevelBodyBase(
            iRiskLevelScore = RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF,
            iDaysSinceLastExposure = null,
            sValue = ""
        )
        formatRiskDetailsRiskLevelBodyBase(
            iRiskLevelScore = RiskLevelConstants.LOW_LEVEL_RISK,
            iDaysSinceLastExposure = null,
            sValue = context.getString(R.string.risk_details_information_body_low_risk)
        )
        formatRiskDetailsRiskLevelBodyBase(
            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_INITIAL,
            iDaysSinceLastExposure = null,
            sValue = context.getString(R.string.risk_details_information_body_unknown_risk)
        )
    }

    @Test
    fun formatRiskColorStateList() {
        formatRiskColorStateListBase(iRiskLevelScore = null)
        formatRiskColorStateListBase(iRiskLevelScore = RiskLevelConstants.INCREASED_RISK)
        formatRiskColorStateListBase(iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS)
        formatRiskColorStateListBase(iRiskLevelScore = RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF)
        formatRiskColorStateListBase(iRiskLevelScore = RiskLevelConstants.LOW_LEVEL_RISK)
        formatRiskColorStateListBase(iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_INITIAL)
    }

    @Test
    fun formatRiskColor() {
        formatRiskColorBase(iRiskLevelScore = null, iValue = R.color.colorSemanticNeutralRisk)
        formatRiskColorBase(iRiskLevelScore = RiskLevelConstants.INCREASED_RISK, iValue = R.color.colorSemanticHighRisk)
        formatRiskColorBase(
            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS,
            iValue = R.color.colorSemanticUnknownRisk
        )
        formatRiskColorBase(
            iRiskLevelScore = RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF,
            iValue = R.color.colorSemanticUnknownRisk
        )
        formatRiskColorBase(iRiskLevelScore = RiskLevelConstants.LOW_LEVEL_RISK, iValue = R.color.colorSemanticLowRisk)
        formatRiskColorBase(
            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_INITIAL,
            iValue = R.color.colorSemanticNeutralRisk
        )
    }

    @Test
    fun formatRiskShape() {
        formatRiskShapeBase(bShowDetails = true)
        formatRiskShapeBase(bShowDetails = false)
    }

    @Test
    fun formatStableIconColor() {
        formatStableIconColorBase(iRiskLevelScore = null)
        formatStableIconColorBase(iRiskLevelScore = RiskLevelConstants.INCREASED_RISK)
        formatStableIconColorBase(iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS)
        formatStableIconColorBase(iRiskLevelScore = RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF)
        formatStableIconColorBase(iRiskLevelScore = RiskLevelConstants.LOW_LEVEL_RISK)
        formatStableIconColorBase(iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_INITIAL)
    }

    @Test
    fun formatStableTextColor() {
        formatStableTextColorBase(iRiskLevelScore = null)
        formatStableTextColorBase(iRiskLevelScore = RiskLevelConstants.INCREASED_RISK)
        formatStableTextColorBase(iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS)
        formatStableTextColorBase(iRiskLevelScore = RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF)
        formatStableTextColorBase(iRiskLevelScore = RiskLevelConstants.LOW_LEVEL_RISK)
        formatStableTextColorBase(iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_INITIAL)
    }

    @Test
    fun formatStableDividerColor() {
        formatStableDividerColorBase(iRiskLevelScore = null)
        formatStableDividerColorBase(iRiskLevelScore = RiskLevelConstants.INCREASED_RISK)
        formatStableDividerColorBase(iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS)
        formatStableDividerColorBase(iRiskLevelScore = RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF)
        formatStableDividerColorBase(iRiskLevelScore = RiskLevelConstants.LOW_LEVEL_RISK)
        formatStableDividerColorBase(iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_INITIAL)
    }

    @Test
    fun formatRiskContactIcon() {
        formatRiskContactIconBase(iRiskLevelScore = null)
        formatRiskContactIconBase(iRiskLevelScore = RiskLevelConstants.INCREASED_RISK)
        formatRiskContactIconBase(iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS)
        formatRiskContactIconBase(iRiskLevelScore = RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF)
        formatRiskContactIconBase(iRiskLevelScore = RiskLevelConstants.LOW_LEVEL_RISK)
        formatRiskContactIconBase(iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_INITIAL)
    }

    @Test
    fun formatButtonEnableTracingVisibility() {
        formatButtonEnableTracingVisibilityBase(iRiskLevelScore = null, bShowDetails = true, iValue = View.GONE)
        formatButtonEnableTracingVisibilityBase(
            iRiskLevelScore = RiskLevelConstants.INCREASED_RISK,
            bShowDetails = true,
            iValue = View.GONE
        )
        formatButtonEnableTracingVisibilityBase(
            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS,
            bShowDetails = true,
            iValue = View.GONE
        )
        formatButtonEnableTracingVisibilityBase(
            iRiskLevelScore = RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF,
            bShowDetails = true,
            iValue = View.GONE
        )
        formatButtonEnableTracingVisibilityBase(
            iRiskLevelScore = RiskLevelConstants.LOW_LEVEL_RISK,
            bShowDetails = true,
            iValue = View.GONE
        )
        formatButtonEnableTracingVisibilityBase(
            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_INITIAL,
            bShowDetails = true,
            iValue = View.GONE
        )

        formatButtonEnableTracingVisibilityBase(
            iRiskLevelScore = RiskLevelConstants.INCREASED_RISK,
            bShowDetails = false,
            iValue = View.GONE
        )
        formatButtonEnableTracingVisibilityBase(
            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS,
            bShowDetails = false,
            iValue = View.VISIBLE
        )
        formatButtonEnableTracingVisibilityBase(
            iRiskLevelScore = RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF,
            bShowDetails = false,
            iValue = View.VISIBLE
        )
        formatButtonEnableTracingVisibilityBase(
            iRiskLevelScore = RiskLevelConstants.LOW_LEVEL_RISK,
            bShowDetails = false,
            iValue = View.GONE
        )
        formatButtonEnableTracingVisibilityBase(
            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_INITIAL,
            bShowDetails = false,
            iValue = View.GONE
        )
    }

    @Test
    fun formatRiskDetailsButtonEnableTracingVisibility() {
        formatRiskDetailsButtonEnableTracingVisibilityBase(iRiskLevelScore = null, iValue = View.GONE)
        formatRiskDetailsButtonEnableTracingVisibilityBase(
            iRiskLevelScore = RiskLevelConstants.INCREASED_RISK,
            iValue = View.GONE
        )
        formatRiskDetailsButtonEnableTracingVisibilityBase(
            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS,
            iValue = View.VISIBLE
        )
        formatRiskDetailsButtonEnableTracingVisibilityBase(
            iRiskLevelScore = RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF,
            iValue = View.VISIBLE
        )
        formatRiskDetailsButtonEnableTracingVisibilityBase(
            iRiskLevelScore = RiskLevelConstants.LOW_LEVEL_RISK,
            iValue = View.GONE
        )
        formatRiskDetailsButtonEnableTracingVisibilityBase(
            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_INITIAL,
            iValue = View.GONE
        )
    }

    @Test
    fun formatRiskDetailsButtonVisibility() {
        formatRiskDetailsButtonVisibilityBase(
            iRiskLevelScore = null,
            bIsBackgroundJobEnabled = true,
            iValue = View.GONE
        )
        formatRiskDetailsButtonVisibilityBase(
            iRiskLevelScore = RiskLevelConstants.INCREASED_RISK,
            bIsBackgroundJobEnabled = true,
            iValue = View.GONE
        )
        formatRiskDetailsButtonVisibilityBase(
            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS,
            bIsBackgroundJobEnabled = true,
            iValue = View.VISIBLE
        )
        formatRiskDetailsButtonVisibilityBase(
            iRiskLevelScore = RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF,
            bIsBackgroundJobEnabled = true,
            iValue = View.VISIBLE
        )
        formatRiskDetailsButtonVisibilityBase(
            iRiskLevelScore = RiskLevelConstants.LOW_LEVEL_RISK,
            bIsBackgroundJobEnabled = true,
            iValue = View.GONE
        )
        formatRiskDetailsButtonVisibilityBase(
            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_INITIAL,
            bIsBackgroundJobEnabled = true,
            iValue = View.GONE
        )

        formatRiskDetailsButtonVisibilityBase(
            iRiskLevelScore = RiskLevelConstants.INCREASED_RISK,
            bIsBackgroundJobEnabled = false,
            iValue = View.VISIBLE
        )
        formatRiskDetailsButtonVisibilityBase(
            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS,
            bIsBackgroundJobEnabled = false,
            iValue = View.VISIBLE
        )
        formatRiskDetailsButtonVisibilityBase(
            iRiskLevelScore = RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF,
            bIsBackgroundJobEnabled = false,
            iValue = View.VISIBLE
        )
        formatRiskDetailsButtonVisibilityBase(
            iRiskLevelScore = RiskLevelConstants.LOW_LEVEL_RISK,
            bIsBackgroundJobEnabled = false,
            iValue = View.VISIBLE
        )
        formatRiskDetailsButtonVisibilityBase(
            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_INITIAL,
            bIsBackgroundJobEnabled = false,
            iValue = View.VISIBLE
        )
    }

    @Test
    fun formatButtonUpdateVisibility() {
        formatButtonUpdateVisibilityBase(
            iRiskLevelScore = null,
            bIsBackgroundJobEnabled = true,
            bShowDetails = true,
            iValue = View.GONE
        )
        formatButtonUpdateVisibilityBase(
            iRiskLevelScore = RiskLevelConstants.INCREASED_RISK,
            bIsBackgroundJobEnabled = true,
            bShowDetails = true,
            iValue = View.GONE
        )
        formatButtonUpdateVisibilityBase(
            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS,
            bIsBackgroundJobEnabled = true,
            bShowDetails = true,
            iValue = View.GONE
        )
        formatButtonUpdateVisibilityBase(
            iRiskLevelScore = RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF,
            bIsBackgroundJobEnabled = true,
            bShowDetails = true,
            iValue = View.GONE
        )
        formatButtonUpdateVisibilityBase(
            iRiskLevelScore = RiskLevelConstants.LOW_LEVEL_RISK,
            bIsBackgroundJobEnabled = true,
            bShowDetails = true,
            iValue = View.GONE
        )
        formatButtonUpdateVisibilityBase(
            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_INITIAL,
            bIsBackgroundJobEnabled = true,
            bShowDetails = true,
            iValue = View.GONE
        )

        formatButtonUpdateVisibilityBase(
            iRiskLevelScore = RiskLevelConstants.INCREASED_RISK,
            bIsBackgroundJobEnabled = false,
            bShowDetails = true,
            iValue = View.GONE
        )
        formatButtonUpdateVisibilityBase(
            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS,
            bIsBackgroundJobEnabled = false,
            bShowDetails = true,
            iValue = View.GONE
        )
        formatButtonUpdateVisibilityBase(
            iRiskLevelScore = RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF,
            bIsBackgroundJobEnabled = false,
            bShowDetails = true,
            iValue = View.GONE
        )
        formatButtonUpdateVisibilityBase(
            iRiskLevelScore = RiskLevelConstants.LOW_LEVEL_RISK,
            bIsBackgroundJobEnabled = false,
            bShowDetails = true,
            iValue = View.GONE
        )
        formatButtonUpdateVisibilityBase(
            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_INITIAL,
            bIsBackgroundJobEnabled = false,
            bShowDetails = true,
            iValue = View.GONE
        )

        formatButtonUpdateVisibilityBase(
            iRiskLevelScore = RiskLevelConstants.INCREASED_RISK,
            bIsBackgroundJobEnabled = false,
            bShowDetails = false,
            iValue = View.VISIBLE
        )
        formatButtonUpdateVisibilityBase(
            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS,
            bIsBackgroundJobEnabled = false,
            bShowDetails = false,
            iValue = View.GONE
        )
        formatButtonUpdateVisibilityBase(
            iRiskLevelScore = RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF,
            bIsBackgroundJobEnabled = false,
            bShowDetails = false,
            iValue = View.GONE
        )
        formatButtonUpdateVisibilityBase(
            iRiskLevelScore = RiskLevelConstants.LOW_LEVEL_RISK,
            bIsBackgroundJobEnabled = false,
            bShowDetails = false,
            iValue = View.VISIBLE
        )
        formatButtonUpdateVisibilityBase(
            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_INITIAL,
            bIsBackgroundJobEnabled = false,
            bShowDetails = false,
            iValue = View.VISIBLE
        )

        formatButtonUpdateVisibilityBase(
            iRiskLevelScore = null,
            bIsBackgroundJobEnabled = true,
            bShowDetails = true,
            iValue = View.GONE
        )
        formatButtonUpdateVisibilityBase(
            iRiskLevelScore = null,
            bIsBackgroundJobEnabled = false,
            bShowDetails = true,
            iValue = View.GONE
        )
    }

    @Test
    fun formatDetailsButtonUpdateVisibility() {
        formatDetailsButtonUpdateVisibilityBase(
            iRiskLevelScore = null,
            bIsBackgroundJobEnabled = true,
            iValue = View.GONE
        )
        formatDetailsButtonUpdateVisibilityBase(
            iRiskLevelScore = RiskLevelConstants.INCREASED_RISK,
            bIsBackgroundJobEnabled = true,
            iValue = View.GONE
        )
        formatDetailsButtonUpdateVisibilityBase(
            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS,
            bIsBackgroundJobEnabled = true,
            iValue = View.GONE
        )
        formatDetailsButtonUpdateVisibilityBase(
            iRiskLevelScore = RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF,
            bIsBackgroundJobEnabled = true,
            iValue = View.GONE
        )
        formatDetailsButtonUpdateVisibilityBase(
            iRiskLevelScore = RiskLevelConstants.LOW_LEVEL_RISK,
            bIsBackgroundJobEnabled = true,
            iValue = View.GONE
        )
        formatDetailsButtonUpdateVisibilityBase(
            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_INITIAL,
            bIsBackgroundJobEnabled = true,
            iValue = View.GONE
        )

        formatDetailsButtonUpdateVisibilityBase(
            iRiskLevelScore = RiskLevelConstants.INCREASED_RISK,
            bIsBackgroundJobEnabled = false,
            iValue = View.VISIBLE
        )
        formatDetailsButtonUpdateVisibilityBase(
            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS,
            bIsBackgroundJobEnabled = false,
            iValue = View.GONE
        )
        formatDetailsButtonUpdateVisibilityBase(
            iRiskLevelScore = RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF,
            bIsBackgroundJobEnabled = false,
            iValue = View.GONE
        )
        formatDetailsButtonUpdateVisibilityBase(
            iRiskLevelScore = RiskLevelConstants.LOW_LEVEL_RISK,
            bIsBackgroundJobEnabled = false,
            iValue = View.VISIBLE
        )
        formatDetailsButtonUpdateVisibilityBase(
            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_INITIAL,
            bIsBackgroundJobEnabled = false,
            iValue = View.VISIBLE
        )
    }

    @Test
    fun formatVisibilityBehavior() {
        formatVisibilityBehaviorBase(iRiskLevelScore = null, iValue = View.VISIBLE)
        formatVisibilityBehaviorBase(iRiskLevelScore = RiskLevelConstants.INCREASED_RISK, iValue = View.GONE)
        formatVisibilityBehaviorBase(
            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS,
            iValue = View.VISIBLE
        )
        formatVisibilityBehaviorBase(
            iRiskLevelScore = RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF,
            iValue = View.VISIBLE
        )
        formatVisibilityBehaviorBase(iRiskLevelScore = RiskLevelConstants.LOW_LEVEL_RISK, iValue = View.VISIBLE)
        formatVisibilityBehaviorBase(iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_INITIAL, iValue = View.VISIBLE)
    }

    @Test
    fun formatVisibilityBehaviorIncreasedRisk() {
        formatVisibilityBehaviorIncreasedRiskBase(iRiskLevelScore = null, iValue = View.GONE)
        formatVisibilityBehaviorIncreasedRiskBase(
            iRiskLevelScore = RiskLevelConstants.INCREASED_RISK,
            iValue = View.VISIBLE
        )
        formatVisibilityBehaviorIncreasedRiskBase(
            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS,
            iValue = View.GONE
        )
        formatVisibilityBehaviorIncreasedRiskBase(
            iRiskLevelScore = RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF,
            iValue = View.GONE
        )
        formatVisibilityBehaviorIncreasedRiskBase(
            iRiskLevelScore = RiskLevelConstants.LOW_LEVEL_RISK,
            iValue = View.GONE
        )
        formatVisibilityBehaviorIncreasedRiskBase(
            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_INITIAL,
            iValue = View.GONE
        )
    }

    @Test
    fun formatBehaviorIcon() {
        formatBehaviorIconBase(iRiskLevelScore = null, iValue = context.getColor(R.color.colorStableLight))
        formatBehaviorIconBase(
            iRiskLevelScore = RiskLevelConstants.INCREASED_RISK,
            iValue = context.getColor(R.color.colorStableLight)
        )
        formatBehaviorIconBase(
            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS,
            iValue = context.getColor(R.color.colorTextSemanticNeutral)
        )
        formatBehaviorIconBase(
            iRiskLevelScore = RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF,
            iValue = context.getColor(R.color.colorTextSemanticNeutral)
        )
        formatBehaviorIconBase(
            iRiskLevelScore = RiskLevelConstants.LOW_LEVEL_RISK,
            iValue = context.getColor(R.color.colorStableLight)
        )
        formatBehaviorIconBase(
            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_INITIAL,
            iValue = context.getColor(R.color.colorStableLight)
        )
    }

    @Test
    fun formatBehaviorIconBackground() {
        formatBehaviorIconBackgroundBase(iRiskLevelScore = null, iValue = context.getColor(R.color.colorSurface2))
        formatBehaviorIconBackgroundBase(
            iRiskLevelScore = RiskLevelConstants.INCREASED_RISK,
            iValue = context.getColor(R.color.colorSemanticHighRisk)
        )
        formatBehaviorIconBackgroundBase(
            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS,
            iValue = context.getColor(R.color.colorSurface2)
        )
        formatBehaviorIconBackgroundBase(
            iRiskLevelScore = RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF,
            iValue = context.getColor(R.color.colorSurface2)
        )
        formatBehaviorIconBackgroundBase(
            iRiskLevelScore = RiskLevelConstants.LOW_LEVEL_RISK,
            iValue = context.getColor(R.color.colorSemanticLowRisk)
        )
        formatBehaviorIconBackgroundBase(
            iRiskLevelScore = RiskLevelConstants.UNKNOWN_RISK_INITIAL,
            iValue = context.getColor(R.color.colorSemanticNeutralRisk)
        )
    }

    @Test
    fun formatButtonUpdateEnabled() {
        formatButtonUpdateEnabledBase(bEnabled = true, bValue = true)
        formatButtonUpdateEnabledBase(bEnabled = false, bValue = false)
    }

    @Test
    fun formatButtonUpdateText() {
        formatButtonUpdateTextBase(lTime = 0, sValue = context.getString(R.string.risk_card_button_update))
        formatButtonUpdateTextBase(lTime = 604800, sValue = context.getString(R.string.risk_card_button_cooldown))
    }

    @After
    fun cleanUp() {
        unmockkAll()
    }
}
