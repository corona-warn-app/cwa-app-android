package de.rki.coronawarnapp.util.formatter

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.View
import de.rki.coronawarnapp.CoronaWarnApplication
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.ui.submission.ApiRequestState
import de.rki.coronawarnapp.util.DeviceUIState
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test

class FormatterSubmissionHelperTest {

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var drawable: Drawable

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        mockkObject(CoronaWarnApplication.Companion)
        mockkStatic(SpannableStringBuilder::class)
        mockkStatic(Spannable::class)

        every { CoronaWarnApplication.getAppContext() } returns context

        every { context.getString(R.string.test_result_card_status_positive) } returns R.string.test_result_card_status_positive.toString()
        every { context.getString(R.string.test_result_card_status_negative) } returns R.string.test_result_card_status_negative.toString()
        every { context.getString(R.string.test_result_card_status_invalid) } returns R.string.test_result_card_status_invalid.toString()
        every { context.getString(R.string.test_result_card_virus_name_text) } returns R.string.test_result_card_virus_name_text.toString()
        every { context.getString(R.string.test_result_card_status_pending) } returns R.string.test_result_card_status_pending.toString()
        every { context.getString(R.string.test_result_card_status_invalid) } returns R.string.test_result_card_status_invalid.toString()

        every { context.getColor(R.color.colorTextSemanticGreen) } returns R.color.colorTextSemanticGreen
        every { context.getColor(R.color.colorTextSemanticRed) } returns R.color.colorTextSemanticRed

        every { context.getDrawable(R.drawable.ic_test_result_illustration_invalid) } returns drawable
        every { context.getDrawable(R.drawable.ic_test_result_illustration_pending) } returns drawable
        every { context.getDrawable(R.drawable.ic_test_result_illustration_positive) } returns drawable
        every { context.getDrawable(R.drawable.ic_main_illustration_negative) } returns drawable

        every { context.getString(R.string.submission_status_card_title_available) } returns R.string.submission_status_card_title_available.toString()
        every { context.getString(R.string.submission_status_card_title_pending) } returns R.string.submission_status_card_title_pending.toString()

        every { context.getString(R.string.submission_status_card_body_invalid) } returns R.string.submission_status_card_body_invalid.toString()
        every { context.getString(R.string.submission_status_card_body_negative) } returns R.string.submission_status_card_body_negative.toString()
        every { context.getString(R.string.submission_status_card_body_positive) } returns R.string.submission_status_card_body_positive.toString()
        every { context.getString(R.string.submission_status_card_body_pending) } returns R.string.submission_status_card_body_pending.toString()

        every { context.getString(R.string.submission_status_card_button_show_results) } returns R.string.submission_status_card_button_show_results.toString()

        every { context.getDrawable(R.drawable.ic_main_illustration_pending) } returns drawable
        every { context.getDrawable(R.drawable.ic_main_illustration_negative) } returns drawable
        every { context.getDrawable(R.drawable.ic_main_illustration_invalid) } returns drawable
        every { context.getDrawable(R.drawable.ic_main_illustration_invalid) } returns drawable

        every { context.getDrawable(R.drawable.ic_test_result_illustration_negative) } returns drawable
    }

    private fun formatTestResultSpinnerVisibleBase(oUiStateState: ApiRequestState?, iResult: Int) {
        val result = formatTestResultSpinnerVisible(uiStateState = oUiStateState)
        assertThat(result, `is`(iResult))
    }

    private fun formatTestResultVisibleBase(oUiStateState: ApiRequestState?, iResult: Int) {
        val result = formatTestResultVisible(uiStateState = oUiStateState)
        assertThat(result, `is`(iResult))
    }

    private fun formatTestResultStatusTextBase(oUiState: DeviceUIState?, iResult: String) {
        val result = formatTestResultStatusText(uiState = oUiState)
        assertThat(result, `is`(iResult))
    }

    private fun formatTestResultStatusColorBase(oUiState: DeviceUIState?, iResult: Int) {
        val result = formatTestResultStatusColor(uiState = oUiState)
        assertThat(result, `is`(iResult))
    }

    private fun formatTestStatusIconBase(oUiState: DeviceUIState?) {
        val result = formatTestStatusIcon(uiState = oUiState)
        assertThat(result, `is`(drawable))
    }

    private fun formatTestResultPendingStepsVisibleBase(oUiState: DeviceUIState?, iResult: Int) {
        val result = formatTestResultPendingStepsVisible(uiState = oUiState)
        assertThat(result, `is`(iResult))
    }

    private fun formatTestResultNegativeStepsVisibleBase(oUiState: DeviceUIState?, iResult: Int) {
        val result = formatTestResultNegativeStepsVisible(uiState = oUiState)
        assertThat(result, `is`(iResult))
    }

    private fun formatTestResultPositiveStepsVisibleBase(oUiState: DeviceUIState?, iResult: Int) {
        val result = formatTestResultPositiveStepsVisible(uiState = oUiState)
        assertThat(result, `is`(iResult))
    }

    private fun formatTestResultInvalidStepsVisibleBase(oUiState: DeviceUIState?, iResult: Int) {
        val result = formatTestResultInvalidStepsVisible(uiState = oUiState)
        assertThat(result, `is`(iResult))
    }

    private fun formatSubmissionStatusCardContentTitleTextBase(
        oUiState: DeviceUIState?,
        iResult: String
    ) {
        val result = formatSubmissionStatusCardContentTitleText(uiState = oUiState)
        assertThat(result, `is`(iResult))
    }

    private fun formatSubmissionStatusCardContentBodyTextBase(
        oUiState: DeviceUIState?,
        iResult: String
    ) {
        val result = formatSubmissionStatusCardContentBodyText(uiState = oUiState)
        assertThat(result, `is`(iResult))
    }

    private fun formatSubmissionStatusCardContentStatusTextVisibleBase(
        oUiState: DeviceUIState?,
        iResult: Int
    ) {
        val result = formatSubmissionStatusCardContentStatusTextVisible(uiState = oUiState)
        assertThat(result, `is`(iResult))
    }

    private fun formatSubmissionStatusCardContentIconBase(oUiState: DeviceUIState?) {
        val result = formatSubmissionStatusCardContentIcon(uiState = oUiState)
        assertThat(result, `is`(drawable))
    }

    private fun formatSubmissionStatusCardFetchingVisibleBase(
        bDeviceRegistered: Boolean?,
        bUiStateState: ApiRequestState?,
        iResult: Int
    ) {
        val result = formatSubmissionStatusCardFetchingVisible(
            deviceRegistered = bDeviceRegistered,
            uiStateState = bUiStateState
        )
        assertThat(result, `is`(iResult))
    }

    private fun formatSubmissionStatusCardContentVisibleBase(
        oDeviceUiState: DeviceUIState?,
        iResult: Int
    ) {
        val result =
            formatSubmissionStatusCardContentVisible(deviceUiState = oDeviceUiState)
        assertThat(result, `is`(iResult))
    }

    private fun formatShowSubmissionStatusPositiveCardBase(
        oDeviceUIState: DeviceUIState?,
        iResult: Int
    ) {
        val result = formatShowSubmissionStatusPositiveCard(deviceUiState = oDeviceUIState)
        assertThat(result, `is`(iResult))
    }

    private fun formatShowSubmissionDoneCardBase(oDeviceUIState: DeviceUIState?, iResult: Int) {
        val result = formatShowSubmissionDoneCard(deviceUiState = oDeviceUIState)
        assertThat(result, `is`(iResult))
    }

    private fun formatShowRiskStatusCardBase(oDeviceUIState: DeviceUIState?, iResult: Int) {
        val result = formatShowRiskStatusCard(deviceUiState = oDeviceUIState)
        assertThat(result, `is`(iResult))
    }

    private fun formatTestResultBase(oUiState: DeviceUIState?) {
        mockkConstructor(SpannableStringBuilder::class)

        val spannableStringBuilder1 =
            mockk<SpannableStringBuilder>(R.string.test_result_card_virus_name_text.toString())
        val spannableStringBuilder2 =
            mockk<SpannableStringBuilder>(R.string.test_result_card_virus_name_text.toString() + " ")
        val spannableStringBuilder3 = mockk<SpannableStringBuilder>("result")

        every { SpannableStringBuilder().append(any<String>()) } returns spannableStringBuilder1
        every { spannableStringBuilder1.append("\n") } returns spannableStringBuilder2
        every { context.getString(R.string.test_result_card_virus_name_text) } returns R.string.test_result_card_virus_name_text.toString()
        every {
            spannableStringBuilder2.append(
                any<String>(),
                any<ForegroundColorSpan>(),
                Spannable.SPAN_EXCLUSIVE_INCLUSIVE
            )
        } returns spannableStringBuilder3

        val result = formatTestResult(uiState = oUiState)
        assertThat(result, `is`(spannableStringBuilder3 as Spannable?))
    }

    @Test
    fun formatTestResultSpinnerVisible() {
        formatTestResultSpinnerVisibleBase(oUiStateState = null, iResult = View.VISIBLE)
        formatTestResultSpinnerVisibleBase(
            oUiStateState = ApiRequestState.FAILED,
            iResult = View.VISIBLE
        )
        formatTestResultSpinnerVisibleBase(
            oUiStateState = ApiRequestState.IDLE,
            iResult = View.VISIBLE
        )
        formatTestResultSpinnerVisibleBase(
            oUiStateState = ApiRequestState.STARTED,
            iResult = View.VISIBLE
        )
        formatTestResultSpinnerVisibleBase(
            oUiStateState = ApiRequestState.SUCCESS,
            iResult = View.GONE
        )
    }

    @Test
    fun formatTestResultVisible() {
        formatTestResultVisibleBase(oUiStateState = null, iResult = View.GONE)
        formatTestResultVisibleBase(oUiStateState = ApiRequestState.FAILED, iResult = View.GONE)
        formatTestResultVisibleBase(oUiStateState = ApiRequestState.IDLE, iResult = View.GONE)
        formatTestResultVisibleBase(oUiStateState = ApiRequestState.STARTED, iResult = View.GONE)
        formatTestResultVisibleBase(oUiStateState = ApiRequestState.SUCCESS, iResult = View.VISIBLE)
    }

    @Test
    fun formatTestResultStatusText() {
        formatTestResultStatusTextBase(
            oUiState = null,
            iResult = context.getString(R.string.test_result_card_status_invalid)
        )
        formatTestResultStatusTextBase(
            oUiState = DeviceUIState.PAIRED_NEGATIVE,
            iResult = context.getString(R.string.test_result_card_status_negative)
        )
        formatTestResultStatusTextBase(
            oUiState = DeviceUIState.PAIRED_ERROR,
            iResult = context.getString(R.string.test_result_card_status_invalid)
        )
        formatTestResultStatusTextBase(
            oUiState = DeviceUIState.PAIRED_NO_RESULT,
            iResult = context.getString(R.string.test_result_card_status_invalid)
        )
        formatTestResultStatusTextBase(
            oUiState = DeviceUIState.PAIRED_POSITIVE,
            iResult = context.getString(R.string.test_result_card_status_positive)
        )
        formatTestResultStatusTextBase(
            oUiState = DeviceUIState.PAIRED_POSITIVE_TELETAN,
            iResult = context.getString(R.string.test_result_card_status_positive)
        )
        formatTestResultStatusTextBase(
            oUiState = DeviceUIState.SUBMITTED_FINAL,
            iResult = context.getString(R.string.test_result_card_status_invalid)
        )
        formatTestResultStatusTextBase(
            oUiState = DeviceUIState.SUBMITTED_INITIAL,
            iResult = context.getString(R.string.test_result_card_status_invalid)
        )
        formatTestResultStatusTextBase(
            oUiState = DeviceUIState.UNPAIRED,
            iResult = context.getString(R.string.test_result_card_status_invalid)
        )
    }

    @Test
    fun formatTestResultStatusColor() {
        formatTestResultStatusColorBase(
            oUiState = null,
            iResult = context.getColor(R.color.colorTextSemanticRed)
        )
        formatTestResultStatusColorBase(
            oUiState = DeviceUIState.PAIRED_NEGATIVE,
            iResult = context.getColor(R.color.colorTextSemanticGreen)
        )
        formatTestResultStatusColorBase(
            oUiState = DeviceUIState.PAIRED_ERROR,
            iResult = context.getColor(R.color.colorTextSemanticRed)
        )
        formatTestResultStatusColorBase(
            oUiState = DeviceUIState.PAIRED_NO_RESULT,
            iResult = context.getColor(R.color.colorTextSemanticRed)
        )
        formatTestResultStatusColorBase(
            oUiState = DeviceUIState.PAIRED_POSITIVE,
            iResult = context.getColor(R.color.colorTextSemanticRed)
        )
        formatTestResultStatusColorBase(
            oUiState = DeviceUIState.PAIRED_POSITIVE_TELETAN,
            iResult = context.getColor(R.color.colorTextSemanticRed)
        )
        formatTestResultStatusColorBase(
            oUiState = DeviceUIState.SUBMITTED_FINAL,
            iResult = context.getColor(R.color.colorTextSemanticRed)
        )
        formatTestResultStatusColorBase(
            oUiState = DeviceUIState.SUBMITTED_INITIAL,
            iResult = context.getColor(R.color.colorTextSemanticRed)
        )
        formatTestResultStatusColorBase(
            oUiState = DeviceUIState.UNPAIRED,
            iResult = context.getColor(R.color.colorTextSemanticRed)
        )
    }

    @Test
    fun formatTestStatusIcon() {
        formatTestStatusIconBase(oUiState = null)
        formatTestStatusIconBase(oUiState = DeviceUIState.PAIRED_NEGATIVE)
        formatTestStatusIconBase(oUiState = DeviceUIState.PAIRED_ERROR)
        formatTestStatusIconBase(oUiState = DeviceUIState.PAIRED_NO_RESULT)
        formatTestStatusIconBase(oUiState = DeviceUIState.PAIRED_POSITIVE)
        formatTestStatusIconBase(oUiState = DeviceUIState.PAIRED_POSITIVE_TELETAN)
        formatTestStatusIconBase(oUiState = DeviceUIState.SUBMITTED_FINAL)
        formatTestStatusIconBase(oUiState = DeviceUIState.SUBMITTED_INITIAL)
        formatTestStatusIconBase(oUiState = DeviceUIState.UNPAIRED)
    }

    @Test
    fun formatTestResultPendingStepsVisible() {
        formatTestResultPendingStepsVisibleBase(oUiState = null, iResult = View.GONE)
        formatTestResultPendingStepsVisibleBase(
            oUiState = DeviceUIState.PAIRED_NEGATIVE,
            iResult = View.GONE
        )
        formatTestResultPendingStepsVisibleBase(
            oUiState = DeviceUIState.PAIRED_ERROR,
            iResult = View.GONE
        )
        formatTestResultPendingStepsVisibleBase(
            oUiState = DeviceUIState.PAIRED_NO_RESULT,
            iResult = View.VISIBLE
        )
        formatTestResultPendingStepsVisibleBase(
            oUiState = DeviceUIState.PAIRED_POSITIVE,
            iResult = View.GONE
        )
        formatTestResultPendingStepsVisibleBase(
            oUiState = DeviceUIState.PAIRED_POSITIVE_TELETAN,
            iResult = View.GONE
        )
        formatTestResultPendingStepsVisibleBase(
            oUiState = DeviceUIState.SUBMITTED_FINAL,
            iResult = View.GONE
        )
        formatTestResultPendingStepsVisibleBase(
            oUiState = DeviceUIState.SUBMITTED_INITIAL,
            iResult = View.GONE
        )
        formatTestResultPendingStepsVisibleBase(
            oUiState = DeviceUIState.UNPAIRED,
            iResult = View.GONE
        )
    }

    @Test
    fun formatTestResultNegativeStepsVisible() {
        formatTestResultNegativeStepsVisibleBase(oUiState = null, iResult = View.GONE)
        formatTestResultNegativeStepsVisibleBase(
            oUiState = DeviceUIState.PAIRED_NEGATIVE,
            iResult = View.VISIBLE
        )
        formatTestResultNegativeStepsVisibleBase(
            oUiState = DeviceUIState.PAIRED_ERROR,
            iResult = View.GONE
        )
        formatTestResultNegativeStepsVisibleBase(
            oUiState = DeviceUIState.PAIRED_NO_RESULT,
            iResult = View.GONE
        )
        formatTestResultNegativeStepsVisibleBase(
            oUiState = DeviceUIState.PAIRED_POSITIVE,
            iResult = View.GONE
        )
        formatTestResultNegativeStepsVisibleBase(
            oUiState = DeviceUIState.PAIRED_POSITIVE_TELETAN,
            iResult = View.GONE
        )
        formatTestResultNegativeStepsVisibleBase(
            oUiState = DeviceUIState.SUBMITTED_FINAL,
            iResult = View.GONE
        )
        formatTestResultNegativeStepsVisibleBase(
            oUiState = DeviceUIState.SUBMITTED_INITIAL,
            iResult = View.GONE
        )
        formatTestResultNegativeStepsVisibleBase(
            oUiState = DeviceUIState.UNPAIRED,
            iResult = View.GONE
        )
    }

    @Test
    fun formatTestResultPositiveStepsVisible() {
        formatTestResultPositiveStepsVisibleBase(oUiState = null, iResult = View.GONE)
        formatTestResultPositiveStepsVisibleBase(
            oUiState = DeviceUIState.PAIRED_NEGATIVE,
            iResult = View.GONE
        )
        formatTestResultPositiveStepsVisibleBase(
            oUiState = DeviceUIState.PAIRED_ERROR,
            iResult = View.GONE
        )
        formatTestResultPositiveStepsVisibleBase(
            oUiState = DeviceUIState.PAIRED_NO_RESULT,
            iResult = View.GONE
        )
        formatTestResultPositiveStepsVisibleBase(
            oUiState = DeviceUIState.PAIRED_POSITIVE,
            iResult = View.VISIBLE
        )
        formatTestResultPositiveStepsVisibleBase(
            oUiState = DeviceUIState.PAIRED_POSITIVE_TELETAN,
            iResult = View.VISIBLE
        )
        formatTestResultPositiveStepsVisibleBase(
            oUiState = DeviceUIState.SUBMITTED_FINAL,
            iResult = View.GONE
        )
        formatTestResultPositiveStepsVisibleBase(
            oUiState = DeviceUIState.SUBMITTED_INITIAL,
            iResult = View.GONE
        )
        formatTestResultPositiveStepsVisibleBase(
            oUiState = DeviceUIState.UNPAIRED,
            iResult = View.GONE
        )
    }

    @Test
    fun formatTestResultInvalidStepsVisible() {
        formatTestResultInvalidStepsVisibleBase(oUiState = null, iResult = View.GONE)
        formatTestResultInvalidStepsVisibleBase(
            oUiState = DeviceUIState.PAIRED_NEGATIVE,
            iResult = View.GONE
        )
        formatTestResultInvalidStepsVisibleBase(
            oUiState = DeviceUIState.PAIRED_ERROR,
            iResult = View.VISIBLE
        )
        formatTestResultInvalidStepsVisibleBase(
            oUiState = DeviceUIState.PAIRED_NO_RESULT,
            iResult = View.GONE
        )
        formatTestResultInvalidStepsVisibleBase(
            oUiState = DeviceUIState.PAIRED_POSITIVE,
            iResult = View.GONE
        )
        formatTestResultInvalidStepsVisibleBase(
            oUiState = DeviceUIState.PAIRED_POSITIVE_TELETAN,
            iResult = View.GONE
        )
        formatTestResultInvalidStepsVisibleBase(
            oUiState = DeviceUIState.SUBMITTED_FINAL,
            iResult = View.GONE
        )
        formatTestResultInvalidStepsVisibleBase(
            oUiState = DeviceUIState.SUBMITTED_INITIAL,
            iResult = View.GONE
        )
        formatTestResultInvalidStepsVisibleBase(
            oUiState = DeviceUIState.UNPAIRED,
            iResult = View.GONE
        )
    }

    @Test
    fun formatSubmissionStatusCardContentTitleText() {
        formatSubmissionStatusCardContentTitleTextBase(
            oUiState = null,
            iResult = context.getString(R.string.submission_status_card_title_pending)
        )
        formatSubmissionStatusCardContentTitleTextBase(
            oUiState = DeviceUIState.PAIRED_NEGATIVE,
            iResult = context.getString(R.string.submission_status_card_title_available)
        )
        formatSubmissionStatusCardContentTitleTextBase(
            oUiState = DeviceUIState.PAIRED_ERROR,
            iResult = context.getString(R.string.submission_status_card_title_available)
        )
        formatSubmissionStatusCardContentTitleTextBase(
            oUiState = DeviceUIState.PAIRED_NO_RESULT,
            iResult = context.getString(R.string.submission_status_card_title_pending)
        )
        formatSubmissionStatusCardContentTitleTextBase(
            oUiState = DeviceUIState.PAIRED_POSITIVE,
            iResult = context.getString(R.string.submission_status_card_title_pending)
        )
        formatSubmissionStatusCardContentTitleTextBase(
            oUiState = DeviceUIState.PAIRED_POSITIVE_TELETAN,
            iResult = context.getString(R.string.submission_status_card_title_pending)
        )
        formatSubmissionStatusCardContentTitleTextBase(
            oUiState = DeviceUIState.SUBMITTED_FINAL,
            iResult = context.getString(R.string.submission_status_card_title_pending)
        )
        formatSubmissionStatusCardContentTitleTextBase(
            oUiState = DeviceUIState.SUBMITTED_INITIAL,
            iResult = context.getString(R.string.submission_status_card_title_pending)
        )
        formatSubmissionStatusCardContentTitleTextBase(
            oUiState = DeviceUIState.UNPAIRED,
            iResult = context.getString(R.string.submission_status_card_title_pending)
        )
    }

    @Test
    fun formatSubmissionStatusCardContentBodyText() {
        formatSubmissionStatusCardContentBodyTextBase(
            oUiState = null,
            iResult = context.getString(R.string.submission_status_card_body_pending)
        )
        formatSubmissionStatusCardContentBodyTextBase(
            oUiState = DeviceUIState.PAIRED_NEGATIVE,
            iResult = context.getString(R.string.submission_status_card_body_negative)
        )
        formatSubmissionStatusCardContentBodyTextBase(
            oUiState = DeviceUIState.PAIRED_ERROR,
            iResult = context.getString(R.string.submission_status_card_body_invalid)
        )
        formatSubmissionStatusCardContentBodyTextBase(
            oUiState = DeviceUIState.PAIRED_NO_RESULT,
            iResult = context.getString(R.string.submission_status_card_body_pending)
        )
        formatSubmissionStatusCardContentBodyTextBase(
            oUiState = DeviceUIState.PAIRED_POSITIVE,
            iResult = context.getString(R.string.submission_status_card_body_pending)
        )
        formatSubmissionStatusCardContentBodyTextBase(
            oUiState = DeviceUIState.PAIRED_POSITIVE_TELETAN,
            iResult = context.getString(R.string.submission_status_card_body_pending)
        )
        formatSubmissionStatusCardContentBodyTextBase(
            oUiState = DeviceUIState.SUBMITTED_FINAL,
            iResult = context.getString(R.string.submission_status_card_body_pending)
        )
        formatSubmissionStatusCardContentBodyTextBase(
            oUiState = DeviceUIState.SUBMITTED_INITIAL,
            iResult = context.getString(R.string.submission_status_card_body_pending)
        )
        formatSubmissionStatusCardContentBodyTextBase(
            oUiState = DeviceUIState.UNPAIRED,
            iResult = context.getString(R.string.submission_status_card_body_pending)
        )
    }

    @Test
    fun formatSubmissionStatusCardContentStatusTextVisible() {
        formatSubmissionStatusCardContentStatusTextVisibleBase(oUiState = null, iResult = View.GONE)
        formatSubmissionStatusCardContentStatusTextVisibleBase(
            oUiState = DeviceUIState.PAIRED_NEGATIVE,
            iResult = View.VISIBLE
        )
        formatSubmissionStatusCardContentStatusTextVisibleBase(
            oUiState = DeviceUIState.PAIRED_ERROR,
            iResult = View.VISIBLE
        )
        formatSubmissionStatusCardContentStatusTextVisibleBase(
            oUiState = DeviceUIState.PAIRED_NO_RESULT,
            iResult = View.GONE
        )
        formatSubmissionStatusCardContentStatusTextVisibleBase(
            oUiState = DeviceUIState.PAIRED_POSITIVE,
            iResult = View.GONE
        )
        formatSubmissionStatusCardContentStatusTextVisibleBase(
            oUiState = DeviceUIState.PAIRED_POSITIVE_TELETAN,
            iResult = View.GONE
        )
        formatSubmissionStatusCardContentStatusTextVisibleBase(
            oUiState = DeviceUIState.SUBMITTED_FINAL,
            iResult = View.GONE
        )
        formatSubmissionStatusCardContentStatusTextVisibleBase(
            oUiState = DeviceUIState.SUBMITTED_INITIAL,
            iResult = View.GONE
        )
        formatSubmissionStatusCardContentStatusTextVisibleBase(
            oUiState = DeviceUIState.UNPAIRED,
            iResult = View.GONE
        )
    }

    @Test
    fun formatSubmissionStatusCardContentIcon() {
        formatSubmissionStatusCardContentIconBase(oUiState = null)
        formatSubmissionStatusCardContentIconBase(oUiState = DeviceUIState.PAIRED_NEGATIVE)
        formatSubmissionStatusCardContentIconBase(oUiState = DeviceUIState.PAIRED_ERROR)
        formatSubmissionStatusCardContentIconBase(oUiState = DeviceUIState.PAIRED_NO_RESULT)
        formatSubmissionStatusCardContentIconBase(oUiState = DeviceUIState.PAIRED_POSITIVE)
        formatSubmissionStatusCardContentIconBase(oUiState = DeviceUIState.PAIRED_POSITIVE_TELETAN)
        formatSubmissionStatusCardContentIconBase(oUiState = DeviceUIState.SUBMITTED_FINAL)
        formatSubmissionStatusCardContentIconBase(oUiState = DeviceUIState.SUBMITTED_INITIAL)
        formatSubmissionStatusCardContentIconBase(oUiState = DeviceUIState.UNPAIRED)
    }

    @Test
    fun formatSubmissionStatusCardFetchingVisible() {
        formatSubmissionStatusCardFetchingVisibleBase(
            bDeviceRegistered = null,
            bUiStateState = null,
            iResult = View.GONE
        )
        formatSubmissionStatusCardFetchingVisibleBase(
            bDeviceRegistered = null,
            bUiStateState = ApiRequestState.SUCCESS,
            iResult = View.GONE
        )
        formatSubmissionStatusCardFetchingVisibleBase(
            bDeviceRegistered = null,
            bUiStateState = ApiRequestState.STARTED,
            iResult = View.GONE
        )
        formatSubmissionStatusCardFetchingVisibleBase(
            bDeviceRegistered = null,
            bUiStateState = ApiRequestState.IDLE,
            iResult = View.GONE
        )
        formatSubmissionStatusCardFetchingVisibleBase(
            bDeviceRegistered = null,
            bUiStateState = ApiRequestState.FAILED,
            iResult = View.GONE
        )
        formatSubmissionStatusCardFetchingVisibleBase(
            bDeviceRegistered = false,
            bUiStateState = ApiRequestState.SUCCESS,
            iResult = View.GONE
        )
        formatSubmissionStatusCardFetchingVisibleBase(
            bDeviceRegistered = false,
            bUiStateState = ApiRequestState.STARTED,
            iResult = View.GONE
        )
        formatSubmissionStatusCardFetchingVisibleBase(
            bDeviceRegistered = false,
            bUiStateState = ApiRequestState.IDLE,
            iResult = View.GONE
        )
        formatSubmissionStatusCardFetchingVisibleBase(
            bDeviceRegistered = false,
            bUiStateState = ApiRequestState.FAILED,
            iResult = View.GONE
        )
        formatSubmissionStatusCardFetchingVisibleBase(
            bDeviceRegistered = true,
            bUiStateState = ApiRequestState.SUCCESS,
            iResult = View.GONE
        )
        formatSubmissionStatusCardFetchingVisibleBase(
            bDeviceRegistered = true,
            bUiStateState = ApiRequestState.STARTED,
            iResult = View.VISIBLE
        )
        formatSubmissionStatusCardFetchingVisibleBase(
            bDeviceRegistered = true,
            bUiStateState = ApiRequestState.IDLE,
            iResult = View.GONE
        )
        formatSubmissionStatusCardFetchingVisibleBase(
            bDeviceRegistered = true,
            bUiStateState = ApiRequestState.FAILED,
            iResult = View.VISIBLE
        )
    }

    @Test
    fun formatSubmissionStatusCardContentVisible() {
        formatSubmissionStatusCardContentVisibleBase(oDeviceUiState = null, iResult = View.GONE)
        formatSubmissionStatusCardContentVisibleBase(
            oDeviceUiState = DeviceUIState.PAIRED_NEGATIVE,
            iResult = View.VISIBLE
        )
        formatSubmissionStatusCardContentVisibleBase(
            oDeviceUiState = DeviceUIState.PAIRED_ERROR,
            iResult = View.VISIBLE
        )
        formatSubmissionStatusCardContentVisibleBase(
            oDeviceUiState = DeviceUIState.PAIRED_NO_RESULT,
            iResult = View.VISIBLE
        )
        formatSubmissionStatusCardContentVisibleBase(
            oDeviceUiState = DeviceUIState.PAIRED_POSITIVE,
            iResult = View.GONE
        )
        formatSubmissionStatusCardContentVisibleBase(
            oDeviceUiState = DeviceUIState.PAIRED_POSITIVE_TELETAN,
            iResult = View.GONE
        )
        formatSubmissionStatusCardContentVisibleBase(
            oDeviceUiState = DeviceUIState.SUBMITTED_FINAL,
            iResult = View.GONE
        )
        formatSubmissionStatusCardContentVisibleBase(
            oDeviceUiState = DeviceUIState.SUBMITTED_INITIAL,
            iResult = View.GONE
        )
        formatSubmissionStatusCardContentVisibleBase(
            oDeviceUiState = DeviceUIState.UNPAIRED,
            iResult = View.GONE
        )
    }

    @Test
    fun formatShowSubmissionStatusPositiveCard() {
        formatShowSubmissionStatusPositiveCardBase(oDeviceUIState = null, iResult = View.GONE)
        formatShowSubmissionStatusPositiveCardBase(
            oDeviceUIState = DeviceUIState.PAIRED_NEGATIVE,
            iResult = View.GONE
        )
        formatShowSubmissionStatusPositiveCardBase(
            oDeviceUIState = DeviceUIState.PAIRED_ERROR,
            iResult = View.GONE
        )
        formatShowSubmissionStatusPositiveCardBase(
            oDeviceUIState = DeviceUIState.PAIRED_NO_RESULT,
            iResult = View.GONE
        )
        formatShowSubmissionStatusPositiveCardBase(
            oDeviceUIState = DeviceUIState.PAIRED_POSITIVE,
            iResult = View.VISIBLE
        )
        formatShowSubmissionStatusPositiveCardBase(
            oDeviceUIState = DeviceUIState.PAIRED_POSITIVE_TELETAN,
            iResult = View.VISIBLE
        )
        formatShowSubmissionStatusPositiveCardBase(
            oDeviceUIState = DeviceUIState.SUBMITTED_FINAL,
            iResult = View.GONE
        )
        formatShowSubmissionStatusPositiveCardBase(
            oDeviceUIState = DeviceUIState.SUBMITTED_INITIAL,
            iResult = View.GONE
        )
        formatShowSubmissionStatusPositiveCardBase(
            oDeviceUIState = DeviceUIState.UNPAIRED,
            iResult = View.GONE
        )
    }

    @Test
    fun formatShowSubmissionDoneCard() {
        formatShowSubmissionDoneCardBase(oDeviceUIState = null, iResult = View.GONE)
        formatShowSubmissionDoneCardBase(
            oDeviceUIState = DeviceUIState.PAIRED_NEGATIVE,
            iResult = View.GONE
        )
        formatShowSubmissionDoneCardBase(
            oDeviceUIState = DeviceUIState.PAIRED_ERROR,
            iResult = View.GONE
        )
        formatShowSubmissionDoneCardBase(
            oDeviceUIState = DeviceUIState.PAIRED_NO_RESULT,
            iResult = View.GONE
        )
        formatShowSubmissionDoneCardBase(
            oDeviceUIState = DeviceUIState.PAIRED_POSITIVE,
            iResult = View.GONE
        )
        formatShowSubmissionDoneCardBase(
            oDeviceUIState = DeviceUIState.PAIRED_POSITIVE_TELETAN,
            iResult = View.GONE
        )
        formatShowSubmissionDoneCardBase(
            oDeviceUIState = DeviceUIState.SUBMITTED_FINAL,
            iResult = View.VISIBLE
        )
        formatShowSubmissionDoneCardBase(
            oDeviceUIState = DeviceUIState.SUBMITTED_INITIAL,
            iResult = View.GONE
        )
        formatShowSubmissionDoneCardBase(
            oDeviceUIState = DeviceUIState.UNPAIRED,
            iResult = View.GONE
        )
    }

    @Test
    fun formatShowRiskStatusCard() {
        formatShowRiskStatusCardBase(oDeviceUIState = null, iResult = View.VISIBLE)
        formatShowRiskStatusCardBase(
            oDeviceUIState = DeviceUIState.PAIRED_NEGATIVE,
            iResult = View.VISIBLE
        )
        formatShowRiskStatusCardBase(
            oDeviceUIState = DeviceUIState.PAIRED_ERROR,
            iResult = View.VISIBLE
        )
        formatShowRiskStatusCardBase(
            oDeviceUIState = DeviceUIState.PAIRED_NO_RESULT,
            iResult = View.VISIBLE
        )
        formatShowRiskStatusCardBase(
            oDeviceUIState = DeviceUIState.PAIRED_POSITIVE,
            iResult = View.GONE
        )
        formatShowRiskStatusCardBase(
            oDeviceUIState = DeviceUIState.PAIRED_POSITIVE_TELETAN,
            iResult = View.GONE
        )
        formatShowRiskStatusCardBase(
            oDeviceUIState = DeviceUIState.SUBMITTED_FINAL,
            iResult = View.GONE
        )
        formatShowRiskStatusCardBase(
            oDeviceUIState = DeviceUIState.SUBMITTED_INITIAL,
            iResult = View.VISIBLE
        )
        formatShowRiskStatusCardBase(
            oDeviceUIState = DeviceUIState.UNPAIRED,
            iResult = View.VISIBLE
        )
    }

    @Test
    fun formatTestResult() {
        formatTestResultBase(oUiState = null)
        formatTestResultBase(oUiState = DeviceUIState.PAIRED_NEGATIVE)
        formatTestResultBase(oUiState = DeviceUIState.PAIRED_ERROR)
        formatTestResultBase(oUiState = DeviceUIState.PAIRED_NO_RESULT)
        formatTestResultBase(oUiState = DeviceUIState.PAIRED_POSITIVE)
        formatTestResultBase(oUiState = DeviceUIState.PAIRED_POSITIVE_TELETAN)
        formatTestResultBase(oUiState = DeviceUIState.SUBMITTED_FINAL)
        formatTestResultBase(oUiState = DeviceUIState.SUBMITTED_INITIAL)
        formatTestResultBase(oUiState = DeviceUIState.UNPAIRED)
    }

    @After
    fun cleanUp() {
        unmockkAll()
    }
}
