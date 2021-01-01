package de.rki.coronawarnapp.ui.view

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.Spannable
import android.text.SpannableString
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.withStyledAttributes
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.util.DeviceUIState
import de.rki.coronawarnapp.util.NetworkRequestWrapper
import de.rki.coronawarnapp.util.NetworkRequestWrapper.Companion.withSuccess
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toUIFormat
import de.rki.coronawarnapp.util.formatter.formatTestResult
import java.util.Date

/**
 * The [TestResultSectionView] Displays the appropriate test result.
 */
class TestResultSectionView @JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {
    private lateinit var testResultSectionHeadline: TextView
    private lateinit var testResultSectionContent: TextView
    private lateinit var testResultSectionRegisteredAt: TextView
    private lateinit var testResultSectionStatusIcon: ImageView

    init {
        inflate(context, R.layout.view_test_result_section, this)
        context.withStyledAttributes(attrs, R.styleable.TestResultSection) {
            testResultSectionHeadline = findViewById(R.id.test_result_section_headline)
            testResultSectionContent = findViewById(R.id.test_result_section_content)
            testResultSectionRegisteredAt = findViewById(R.id.test_result_section_registered_at_text)
            testResultSectionStatusIcon = findViewById(R.id.test_result_section_status_icon)

            testResultSectionHeadline.text =
                getText(R.styleable.TestResultSection_test_result_section_headline)
            testResultSectionContent.text =
                getText(R.styleable.TestResultSection_test_result_section_content)
            testResultSectionRegisteredAt.text =
                getText(R.styleable.TestResultSection_test_result_section_registered_at_text)
            val resultIconId = getResourceId(R.styleable.TestResultSection_test_result_section_status_icon, 0)
            if (resultIconId != 0) {
                val drawable = getDrawable(context, resultIconId)
                testResultSectionStatusIcon.setImageDrawable(drawable)
            }
        }
    }

    fun setTestResultSection(uiState: NetworkRequestWrapper<DeviceUIState, Throwable>?, registeredAt: Date?) {
        testResultSectionHeadline.text = context.getString(R.string.test_result_card_headline)
        testResultSectionRegisteredAt.text = formatTestResultRegisteredAtText(registeredAt)
        val testResultIcon = formatTestStatusIcon(uiState)
        testResultSectionStatusIcon.setImageDrawable(testResultIcon)
        testResultSectionContent.text = formatTestResultSectionContent(uiState)
    }

    private fun formatTestStatusIcon(uiState: NetworkRequestWrapper<DeviceUIState, Throwable>?): Drawable? {
        return uiState.withSuccess(R.drawable.ic_test_result_illustration_invalid) {
            when (it) {
                DeviceUIState.PAIRED_NO_RESULT -> R.drawable.ic_test_result_illustration_pending
                DeviceUIState.PAIRED_POSITIVE_TELETAN,
                DeviceUIState.PAIRED_POSITIVE -> R.drawable.ic_test_result_illustration_positive
                DeviceUIState.PAIRED_NEGATIVE -> R.drawable.ic_test_result_illustration_negative
                DeviceUIState.PAIRED_ERROR,
                DeviceUIState.PAIRED_REDEEMED -> R.drawable.ic_test_result_illustration_invalid
                else -> R.drawable.ic_test_result_illustration_invalid
            }
        }.let { context.getDrawable(it) }
    }

    private fun formatTestResultRegisteredAtText(registeredAt: Date?): String {
        return context.getString(R.string.test_result_card_registered_at_text)
            .format(registeredAt?.toUIFormat(context))
    }

    private fun formatTestResultSectionContent(uiState: NetworkRequestWrapper<DeviceUIState, Throwable>?): Spannable {
        return uiState.withSuccess(SpannableString("")) {
            when (it) {
                DeviceUIState.PAIRED_NO_RESULT ->
                    SpannableString(context.getString(R.string.test_result_card_status_pending))
                DeviceUIState.PAIRED_ERROR,
                DeviceUIState.PAIRED_REDEEMED ->
                    SpannableString(context.getString(R.string.test_result_card_status_invalid))

                DeviceUIState.PAIRED_POSITIVE,
                DeviceUIState.PAIRED_POSITIVE_TELETAN,
                DeviceUIState.PAIRED_NEGATIVE -> formatTestResult(uiState)
                else -> SpannableString("")
            }
        }
    }
}
