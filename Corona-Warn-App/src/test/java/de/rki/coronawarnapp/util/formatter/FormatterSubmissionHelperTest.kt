package de.rki.coronawarnapp.util.formatter

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.View
import de.rki.coronawarnapp.CoronaWarnApplication
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.util.DeviceUIState
import de.rki.coronawarnapp.util.NetworkRequestWrapper
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

    private fun formatTestResultSpinnerVisibleBase(
        oUiStateState: NetworkRequestWrapper<DeviceUIState, Throwable>?,
        iResult: Int
    ) {
        val result = formatTestResultSpinnerVisible(uiState = oUiStateState)
        assertThat(result, `is`(iResult))
    }

    private fun formatTestResultVisibleBase(
        oUiStateState: NetworkRequestWrapper<DeviceUIState, Throwable>?,
        iResult: Int
    ) {
        val result = formatTestResultVisible(uiState = oUiStateState)
        assertThat(result, `is`(iResult))
    }

    private fun formatTestResultStatusTextBase(
        oUiState: NetworkRequestWrapper<DeviceUIState, Throwable>?,
        iResult: String
    ) {
        val result = formatTestResultStatusText(uiState = oUiState)
        assertThat(result, `is`(iResult))
    }

    private fun formatTestResultStatusColorBase(
        oUiState: NetworkRequestWrapper<DeviceUIState, Throwable>?,
        iResult: Int
    ) {
        val result = formatTestResultStatusColor(uiState = oUiState)
        assertThat(result, `is`(iResult))
    }

    private fun formatTestStatusIconBase(oUiState: NetworkRequestWrapper.RequestSuccessful<DeviceUIState, Throwable>?) {
        val result = formatTestStatusIcon(uiState = oUiState)
        assertThat(result, `is`(drawable))
    }

    private fun formatTestResultPendingStepsVisibleBase(
        oUiState: NetworkRequestWrapper.RequestSuccessful<DeviceUIState, Throwable>?,
        iResult: Int
    ) {
        val result = formatTestResultPendingStepsVisible(uiState = oUiState)
        assertThat(result, `is`(iResult))
    }

    private fun formatTestResultNegativeStepsVisibleBase(
        oUiState: NetworkRequestWrapper.RequestSuccessful<DeviceUIState, Throwable>?,
        iResult: Int
    ) {
        val result = formatTestResultNegativeStepsVisible(uiState = oUiState)
        assertThat(result, `is`(iResult))
    }

    private fun formatTestResultPositiveStepsVisibleBase(
        oUiState: NetworkRequestWrapper.RequestSuccessful<DeviceUIState, Throwable>?,
        iResult: Int
    ) {
        val result = formatTestResultPositiveStepsVisible(uiState = oUiState)
        assertThat(result, `is`(iResult))
    }

    private fun formatTestResultInvalidStepsVisibleBase(
        oUiState: NetworkRequestWrapper.RequestSuccessful<DeviceUIState, Throwable>?,
        iResult: Int
    ) {
        val result = formatTestResultInvalidStepsVisible(uiState = oUiState)
        assertThat(result, `is`(iResult))
    }

    private fun formatTestResultBase(oUiState: NetworkRequestWrapper.RequestSuccessful<DeviceUIState, Throwable>?) {
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
            oUiStateState = NetworkRequestWrapper.RequestFailed(mockk()),
            iResult = View.VISIBLE
        )
        formatTestResultSpinnerVisibleBase(
            oUiStateState = NetworkRequestWrapper.RequestIdle,
            iResult = View.VISIBLE
        )
        formatTestResultSpinnerVisibleBase(
            oUiStateState = NetworkRequestWrapper.RequestStarted,
            iResult = View.VISIBLE
        )
        formatTestResultSpinnerVisibleBase(
            oUiStateState = NetworkRequestWrapper.RequestSuccessful(mockk()),
            iResult = View.GONE
        )
    }

    @Test
    fun formatTestResultVisible() {
        formatTestResultVisibleBase(oUiStateState = null, iResult = View.GONE)
        formatTestResultVisibleBase(oUiStateState = NetworkRequestWrapper.RequestFailed(mockk()), iResult = View.GONE)
        formatTestResultVisibleBase(oUiStateState = NetworkRequestWrapper.RequestIdle, iResult = View.GONE)
        formatTestResultVisibleBase(oUiStateState = NetworkRequestWrapper.RequestStarted, iResult = View.GONE)
        formatTestResultVisibleBase(
            oUiStateState = NetworkRequestWrapper.RequestSuccessful(mockk()),
            iResult = View.VISIBLE
        )
    }

    @Test
    fun formatTestResultStatusText() {
        formatTestResultStatusTextBase(
            oUiState = null,
            iResult = context.getString(R.string.test_result_card_status_invalid)
        )
        formatTestResultStatusTextBase(
            oUiState = NetworkRequestWrapper.RequestSuccessful(DeviceUIState.PAIRED_NEGATIVE),
            iResult = context.getString(R.string.test_result_card_status_negative)
        )
        formatTestResultStatusTextBase(
            oUiState = NetworkRequestWrapper.RequestSuccessful(DeviceUIState.PAIRED_ERROR),
            iResult = context.getString(R.string.test_result_card_status_invalid)
        )
        formatTestResultStatusTextBase(
            oUiState = NetworkRequestWrapper.RequestSuccessful(DeviceUIState.PAIRED_NO_RESULT),
            iResult = context.getString(R.string.test_result_card_status_invalid)
        )
        formatTestResultStatusTextBase(
            oUiState = NetworkRequestWrapper.RequestSuccessful(DeviceUIState.PAIRED_POSITIVE),
            iResult = context.getString(R.string.test_result_card_status_positive)
        )
        formatTestResultStatusTextBase(
            oUiState = NetworkRequestWrapper.RequestSuccessful(DeviceUIState.PAIRED_POSITIVE_TELETAN),
            iResult = context.getString(R.string.test_result_card_status_positive)
        )
        formatTestResultStatusTextBase(
            oUiState = NetworkRequestWrapper.RequestSuccessful(DeviceUIState.SUBMITTED_FINAL),
            iResult = context.getString(R.string.test_result_card_status_invalid)
        )
        formatTestResultStatusTextBase(
            oUiState = NetworkRequestWrapper.RequestSuccessful(DeviceUIState.SUBMITTED_INITIAL),
            iResult = context.getString(R.string.test_result_card_status_invalid)
        )
        formatTestResultStatusTextBase(
            oUiState = NetworkRequestWrapper.RequestSuccessful(DeviceUIState.UNPAIRED),
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
            oUiState = NetworkRequestWrapper.RequestSuccessful(DeviceUIState.PAIRED_NEGATIVE),
            iResult = context.getColor(R.color.colorTextSemanticGreen)
        )
        formatTestResultStatusColorBase(
            oUiState = NetworkRequestWrapper.RequestSuccessful(DeviceUIState.PAIRED_ERROR),
            iResult = context.getColor(R.color.colorTextSemanticRed)
        )
        formatTestResultStatusColorBase(
            oUiState = NetworkRequestWrapper.RequestSuccessful(DeviceUIState.PAIRED_NO_RESULT),
            iResult = context.getColor(R.color.colorTextSemanticRed)
        )
        formatTestResultStatusColorBase(
            oUiState = NetworkRequestWrapper.RequestSuccessful(DeviceUIState.PAIRED_POSITIVE),
            iResult = context.getColor(R.color.colorTextSemanticRed)
        )
        formatTestResultStatusColorBase(
            oUiState = NetworkRequestWrapper.RequestSuccessful(DeviceUIState.PAIRED_POSITIVE_TELETAN),
            iResult = context.getColor(R.color.colorTextSemanticRed)
        )
        formatTestResultStatusColorBase(
            oUiState = NetworkRequestWrapper.RequestSuccessful(DeviceUIState.SUBMITTED_FINAL),
            iResult = context.getColor(R.color.colorTextSemanticRed)
        )
        formatTestResultStatusColorBase(
            oUiState = NetworkRequestWrapper.RequestSuccessful(DeviceUIState.SUBMITTED_INITIAL),
            iResult = context.getColor(R.color.colorTextSemanticRed)
        )
        formatTestResultStatusColorBase(
            oUiState = NetworkRequestWrapper.RequestSuccessful(DeviceUIState.UNPAIRED),
            iResult = context.getColor(R.color.colorTextSemanticRed)
        )
    }

    @Test
    fun formatTestStatusIcon() {
        formatTestStatusIconBase(oUiState = null)
        formatTestStatusIconBase(oUiState = NetworkRequestWrapper.RequestSuccessful(DeviceUIState.PAIRED_NEGATIVE))
        formatTestStatusIconBase(oUiState = NetworkRequestWrapper.RequestSuccessful(DeviceUIState.PAIRED_ERROR))
        formatTestStatusIconBase(oUiState = NetworkRequestWrapper.RequestSuccessful(DeviceUIState.PAIRED_NO_RESULT))
        formatTestStatusIconBase(oUiState = NetworkRequestWrapper.RequestSuccessful(DeviceUIState.PAIRED_POSITIVE))
        formatTestStatusIconBase(oUiState = NetworkRequestWrapper.RequestSuccessful(DeviceUIState.PAIRED_POSITIVE_TELETAN))
        formatTestStatusIconBase(oUiState = NetworkRequestWrapper.RequestSuccessful(DeviceUIState.SUBMITTED_FINAL))
        formatTestStatusIconBase(oUiState = NetworkRequestWrapper.RequestSuccessful(DeviceUIState.SUBMITTED_INITIAL))
        formatTestStatusIconBase(oUiState = NetworkRequestWrapper.RequestSuccessful(DeviceUIState.UNPAIRED))
    }

    @Test
    fun formatTestResultPendingStepsVisible() {
        formatTestResultPendingStepsVisibleBase(oUiState = null, iResult = View.GONE)
        formatTestResultPendingStepsVisibleBase(
            oUiState = NetworkRequestWrapper.RequestSuccessful(DeviceUIState.PAIRED_NEGATIVE),
            iResult = View.GONE
        )
        formatTestResultPendingStepsVisibleBase(
            oUiState = NetworkRequestWrapper.RequestSuccessful(DeviceUIState.PAIRED_ERROR),
            iResult = View.GONE
        )
        formatTestResultPendingStepsVisibleBase(
            oUiState = NetworkRequestWrapper.RequestSuccessful(DeviceUIState.PAIRED_NO_RESULT),
            iResult = View.VISIBLE
        )
        formatTestResultPendingStepsVisibleBase(
            oUiState = NetworkRequestWrapper.RequestSuccessful(DeviceUIState.PAIRED_POSITIVE),
            iResult = View.GONE
        )
        formatTestResultPendingStepsVisibleBase(
            oUiState = NetworkRequestWrapper.RequestSuccessful(DeviceUIState.PAIRED_POSITIVE_TELETAN),
            iResult = View.GONE
        )
        formatTestResultPendingStepsVisibleBase(
            oUiState = NetworkRequestWrapper.RequestSuccessful(DeviceUIState.SUBMITTED_FINAL),
            iResult = View.GONE
        )
        formatTestResultPendingStepsVisibleBase(
            oUiState = NetworkRequestWrapper.RequestSuccessful(DeviceUIState.SUBMITTED_INITIAL),
            iResult = View.GONE
        )
        formatTestResultPendingStepsVisibleBase(
            oUiState = NetworkRequestWrapper.RequestSuccessful(DeviceUIState.UNPAIRED),
            iResult = View.GONE
        )
    }

    @Test
    fun formatTestResultNegativeStepsVisible() {
        formatTestResultNegativeStepsVisibleBase(oUiState = null, iResult = View.GONE)
        formatTestResultNegativeStepsVisibleBase(
            oUiState = NetworkRequestWrapper.RequestSuccessful(DeviceUIState.PAIRED_NEGATIVE),
            iResult = View.VISIBLE
        )
        formatTestResultNegativeStepsVisibleBase(
            oUiState = NetworkRequestWrapper.RequestSuccessful(DeviceUIState.PAIRED_ERROR),
            iResult = View.GONE
        )
        formatTestResultNegativeStepsVisibleBase(
            oUiState = NetworkRequestWrapper.RequestSuccessful(DeviceUIState.PAIRED_NO_RESULT),
            iResult = View.GONE
        )
        formatTestResultNegativeStepsVisibleBase(
            oUiState = NetworkRequestWrapper.RequestSuccessful(DeviceUIState.PAIRED_POSITIVE),
            iResult = View.GONE
        )
        formatTestResultNegativeStepsVisibleBase(
            oUiState = NetworkRequestWrapper.RequestSuccessful(DeviceUIState.PAIRED_POSITIVE_TELETAN),
            iResult = View.GONE
        )
        formatTestResultNegativeStepsVisibleBase(
            oUiState = NetworkRequestWrapper.RequestSuccessful(DeviceUIState.SUBMITTED_FINAL),
            iResult = View.GONE
        )
        formatTestResultNegativeStepsVisibleBase(
            oUiState = NetworkRequestWrapper.RequestSuccessful(DeviceUIState.SUBMITTED_INITIAL),
            iResult = View.GONE
        )
        formatTestResultNegativeStepsVisibleBase(
            oUiState = NetworkRequestWrapper.RequestSuccessful(DeviceUIState.UNPAIRED),
            iResult = View.GONE
        )
    }

    @Test
    fun formatTestResultPositiveStepsVisible() {
        formatTestResultPositiveStepsVisibleBase(oUiState = null, iResult = View.GONE)
        formatTestResultPositiveStepsVisibleBase(
            oUiState = NetworkRequestWrapper.RequestSuccessful(DeviceUIState.PAIRED_NEGATIVE),
            iResult = View.GONE
        )
        formatTestResultPositiveStepsVisibleBase(
            oUiState = NetworkRequestWrapper.RequestSuccessful(DeviceUIState.PAIRED_ERROR),
            iResult = View.GONE
        )
        formatTestResultPositiveStepsVisibleBase(
            oUiState = NetworkRequestWrapper.RequestSuccessful(DeviceUIState.PAIRED_NO_RESULT),
            iResult = View.GONE
        )
        formatTestResultPositiveStepsVisibleBase(
            oUiState = NetworkRequestWrapper.RequestSuccessful(DeviceUIState.PAIRED_POSITIVE),
            iResult = View.VISIBLE
        )
        formatTestResultPositiveStepsVisibleBase(
            oUiState = NetworkRequestWrapper.RequestSuccessful(DeviceUIState.PAIRED_POSITIVE_TELETAN),
            iResult = View.VISIBLE
        )
        formatTestResultPositiveStepsVisibleBase(
            oUiState = NetworkRequestWrapper.RequestSuccessful(DeviceUIState.SUBMITTED_FINAL),
            iResult = View.GONE
        )
        formatTestResultPositiveStepsVisibleBase(
            oUiState = NetworkRequestWrapper.RequestSuccessful(DeviceUIState.SUBMITTED_INITIAL),
            iResult = View.GONE
        )
        formatTestResultPositiveStepsVisibleBase(
            oUiState = NetworkRequestWrapper.RequestSuccessful(DeviceUIState.UNPAIRED),
            iResult = View.GONE
        )
    }

    @Test
    fun formatTestResultInvalidStepsVisible() {
        formatTestResultInvalidStepsVisibleBase(oUiState = null, iResult = View.GONE)
        formatTestResultInvalidStepsVisibleBase(
            oUiState = NetworkRequestWrapper.RequestSuccessful(DeviceUIState.PAIRED_NEGATIVE),
            iResult = View.GONE
        )
        formatTestResultInvalidStepsVisibleBase(
            oUiState = NetworkRequestWrapper.RequestSuccessful(DeviceUIState.PAIRED_ERROR),
            iResult = View.VISIBLE
        )
        formatTestResultInvalidStepsVisibleBase(
            oUiState = NetworkRequestWrapper.RequestSuccessful(DeviceUIState.PAIRED_NO_RESULT),
            iResult = View.GONE
        )
        formatTestResultInvalidStepsVisibleBase(
            oUiState = NetworkRequestWrapper.RequestSuccessful(DeviceUIState.PAIRED_POSITIVE),
            iResult = View.GONE
        )
        formatTestResultInvalidStepsVisibleBase(
            oUiState = NetworkRequestWrapper.RequestSuccessful(DeviceUIState.PAIRED_POSITIVE_TELETAN),
            iResult = View.GONE
        )
        formatTestResultInvalidStepsVisibleBase(
            oUiState = NetworkRequestWrapper.RequestSuccessful(DeviceUIState.SUBMITTED_FINAL),
            iResult = View.GONE
        )
        formatTestResultInvalidStepsVisibleBase(
            oUiState = NetworkRequestWrapper.RequestSuccessful(DeviceUIState.SUBMITTED_INITIAL),
            iResult = View.GONE
        )
        formatTestResultInvalidStepsVisibleBase(
            oUiState = NetworkRequestWrapper.RequestSuccessful(DeviceUIState.UNPAIRED),
            iResult = View.GONE
        )
    }

    @Test
    fun formatTestResult() {
        formatTestResultBase(oUiState = null)
        formatTestResultBase(oUiState = NetworkRequestWrapper.RequestSuccessful(DeviceUIState.PAIRED_NEGATIVE))
        formatTestResultBase(oUiState = NetworkRequestWrapper.RequestSuccessful(DeviceUIState.PAIRED_ERROR))
        formatTestResultBase(oUiState = NetworkRequestWrapper.RequestSuccessful(DeviceUIState.PAIRED_NO_RESULT))
        formatTestResultBase(oUiState = NetworkRequestWrapper.RequestSuccessful(DeviceUIState.PAIRED_POSITIVE))
        formatTestResultBase(oUiState = NetworkRequestWrapper.RequestSuccessful(DeviceUIState.PAIRED_POSITIVE_TELETAN))
        formatTestResultBase(oUiState = NetworkRequestWrapper.RequestSuccessful(DeviceUIState.SUBMITTED_FINAL))
        formatTestResultBase(oUiState = NetworkRequestWrapper.RequestSuccessful(DeviceUIState.SUBMITTED_INITIAL))
        formatTestResultBase(oUiState = NetworkRequestWrapper.RequestSuccessful(DeviceUIState.UNPAIRED))
    }

    @After
    fun cleanUp() {
        unmockkAll()
    }
}
