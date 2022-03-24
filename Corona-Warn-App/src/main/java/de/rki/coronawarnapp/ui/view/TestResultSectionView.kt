package de.rki.coronawarnapp.ui.view

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.Spannable
import android.text.SpannableString
import android.util.AttributeSet
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.withStyledAttributes
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import de.rki.coronawarnapp.coronatest.type.rapidantigen.RACoronaTest
import de.rki.coronawarnapp.databinding.ViewTestResultSectionBinding
import de.rki.coronawarnapp.submission.toDeviceUIState
import de.rki.coronawarnapp.util.ContextExtensions.getDrawableCompat
import de.rki.coronawarnapp.util.DeviceUIState
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toUIFormat
import de.rki.coronawarnapp.util.formatter.formatTestResult

/**
 * The [TestResultSectionView] Displays the appropriate test result.
 */
class TestResultSectionView @JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {
    private val binding: ViewTestResultSectionBinding

    init {
        inflate(context, R.layout.view_test_result_section, this)
        binding = ViewTestResultSectionBinding.bind(this)
        context.withStyledAttributes(attrs, R.styleable.TestResultSection) {
            binding.testResultSectionHeadline.text =
                getText(R.styleable.TestResultSection_test_result_section_headline)
            binding.testResultSectionContent.text =
                getText(R.styleable.TestResultSection_test_result_section_content)
            binding.testResultSectionRegisteredAtText.text =
                getText(R.styleable.TestResultSection_test_result_section_registered_at_text)
            val resultIconId = getResourceId(R.styleable.TestResultSection_test_result_section_status_icon, 0)
            if (resultIconId != 0) {
                val drawable = getDrawable(context, resultIconId)
                binding.testResultSectionStatusIcon.setImageDrawable(drawable)
            }
        }
    }

    fun setTestResultSection(coronaTest: BaseCoronaTest?) {
        binding.apply {
            when (coronaTest?.type) {
                BaseCoronaTest.Type.PCR ->
                    testResultSectionHeadline.text = context.getString(R.string.test_result_card_headline)
                        .format(context.getString(R.string.ag_homescreen_card_pcr_title))
                BaseCoronaTest.Type.RAPID_ANTIGEN ->
                    testResultSectionHeadline.text = context.getString(R.string.test_result_card_headline)
                        .format(context.getString(R.string.submission_test_result_antigen_title))

                else -> Unit
            }

            testResultSectionRegisteredAtText.text = formatTestResultTimestampText(coronaTest)
            val testResultIcon = formatTestStatusIcon(coronaTest)
            testResultSectionStatusIcon.setImageDrawable(testResultIcon)
            testResultSectionContent.text = formatTestResultSectionContent(coronaTest)
        }
    }

    private fun formatTestStatusIcon(coronaTest: BaseCoronaTest?): Drawable? {
        val drawable = when (coronaTest?.testResult.toDeviceUIState()) {
            DeviceUIState.PAIRED_NO_RESULT -> R.drawable.ic_test_result_illustration_pending
            DeviceUIState.PAIRED_POSITIVE -> R.drawable.ic_test_result_illustration_positive
            DeviceUIState.PAIRED_NEGATIVE -> R.drawable.ic_test_result_illustration_negative
            DeviceUIState.PAIRED_ERROR,
            DeviceUIState.PAIRED_REDEEMED -> R.drawable.ic_test_result_illustration_invalid
            else -> R.drawable.ic_test_result_illustration_invalid
        }
        return context.getDrawableCompat(drawable)
    }

    private fun formatTestResultTimestampText(coronaTest: BaseCoronaTest?): String {
        return if (coronaTest is RACoronaTest) {
            context.getString(
                R.string.ag_homescreen_card_rapid_body_result_date,
                coronaTest.testTakenAt.toDate()?.toUIFormat(context)
            )
        } else {
            context.getString(
                R.string.test_result_card_registered_at_text,
                coronaTest?.registeredAt?.toDate()?.toUIFormat(context)
            )
        }
    }

    private fun formatTestResultSectionContent(coronaTest: BaseCoronaTest?): Spannable {
        return when (val uiState = coronaTest?.testResult.toDeviceUIState()) {
            DeviceUIState.PAIRED_NO_RESULT ->
                SpannableString(context.getString(R.string.test_result_card_status_pending))
            DeviceUIState.PAIRED_ERROR,
            DeviceUIState.PAIRED_REDEEMED ->
                SpannableString(context.getString(R.string.test_result_card_status_invalid))

            DeviceUIState.PAIRED_POSITIVE,
            DeviceUIState.PAIRED_NEGATIVE -> SpannableString(formatTestResult(context, uiState))
            else -> SpannableString("")
        }
    }
}
