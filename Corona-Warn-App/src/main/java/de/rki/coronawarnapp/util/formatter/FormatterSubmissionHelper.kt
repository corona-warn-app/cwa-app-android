@file:JvmName("FormatterSubmissionHelper")

package de.rki.coronawarnapp.util.formatter

import android.graphics.drawable.Drawable
import android.view.View
import de.rki.coronawarnapp.CoronaWarnApplication
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.ui.register.ApiRequestState
import de.rki.coronawarnapp.ui.submission.TestResultStatus
import de.rki.coronawarnapp.util.formatter.TestResult.INVALID
import de.rki.coronawarnapp.util.formatter.TestResult.NEGATIVE
import de.rki.coronawarnapp.util.formatter.TestResult.PENDING
import de.rki.coronawarnapp.util.formatter.TestResult.POSITIVE
import java.util.Date

fun formatTestResultStatusVisibility(testResultStatus: TestResultStatus?): Int =
    formatVisibility(testResultStatus != TestResultStatus.SUCCESS)

fun formatTestResultHeadingTextVisible(testResult: TestResult?): Int =
    formatVisibility(testResult != PENDING)

fun formatTestResultVirusNameTextVisible(testResult: TestResult?): Int {
    return when (testResult) {
        POSITIVE, NEGATIVE -> View.VISIBLE
        else -> View.GONE
    }
}

fun formatTestResultStatusTextVisible(testResult: TestResult?): Int {
    return when (testResult) {
        POSITIVE, NEGATIVE -> View.VISIBLE
        else -> View.GONE
    }
}

fun formatTestResultStatusText(testResult: TestResult?): String {
    val appContext = CoronaWarnApplication.getAppContext()
    return when (testResult) {
        NEGATIVE -> appContext.getString(R.string.test_result_card_status_negative)
        POSITIVE -> appContext.getString(R.string.test_result_card_status_positive)
        else -> appContext.getString(R.string.test_result_card_status_invalid)
    }
}

fun formatTestResultStatusColor(testResult: TestResult?): Int {
    val appContext = CoronaWarnApplication.getAppContext()
    return when (testResult) {
        NEGATIVE -> appContext.getColor(R.color.colorGreen)
        POSITIVE -> appContext.getColor(R.color.colorRed)
        else -> appContext.getColor(R.color.colorRed)
    }
}

fun formatTestStatusIcon(testResult: TestResult?): Drawable? {
    val appContext = CoronaWarnApplication.getAppContext()
    // TODO Replace with real drawables when design is finished
    return when (testResult) {
        PENDING -> appContext.getDrawable(R.drawable.ic_risk_details_stethoscope)
        POSITIVE -> appContext.getDrawable(R.drawable.rectangle)
        NEGATIVE -> appContext.getDrawable(R.drawable.circle)
        INVALID -> appContext.getDrawable(R.drawable.button)
        else -> appContext.getDrawable(R.drawable.button)
    }
}

fun formatTestResultInvalidStatusTextVisible(testResult: TestResult?): Int =
    formatVisibility(testResult == INVALID)

fun formatTestResultRegisteredAtVisible(testResult: TestResult?): Int {
    return when (testResult) {
        POSITIVE, NEGATIVE, INVALID -> View.VISIBLE
        else -> View.GONE
    }
}

fun formatTestResultRegisteredAtText(registeredAt: Date?): String {
    val appContext = CoronaWarnApplication.getAppContext()
    return appContext.getString(R.string.test_result_card_registered_at_text).format(registeredAt)
}

fun formatTestResultPendingStepsVisible(testResult: TestResult?): Int =
    formatVisibility(testResult == PENDING)

fun formatTestResultNegativeStepsVisible(testResult: TestResult?): Int =
    formatVisibility(testResult == NEGATIVE)

fun formatTestResultPositiveStepsVisible(testResult: TestResult?): Int =
    formatVisibility(testResult == POSITIVE)

fun formatTestResultInvalidStepsVisible(testResult: TestResult?): Int =
    formatVisibility(testResult == INVALID)

fun formatSubmissionStatusCardContentTitleText(testResult: TestResult?): String {
    val appContext = CoronaWarnApplication.getAppContext()
    return when (testResult) {
        INVALID, NEGATIVE, POSITIVE -> appContext.getString(R.string.submission_status_card_title_available)
        PENDING -> appContext.getString(R.string.submission_status_card_title_pending)
        else -> appContext.getString(R.string.submission_status_card_title_pending)
    }
}

fun formatSubmissionStatusCardContentBodyText(testResult: TestResult?): String {
    val appContext = CoronaWarnApplication.getAppContext()
    return when (testResult) {
        INVALID -> appContext.getString(R.string.submission_status_card_body_invalid)
        NEGATIVE -> appContext.getString(R.string.submission_status_card_body_negative)
        POSITIVE -> appContext.getString(R.string.submission_status_card_body_positive)
        PENDING -> appContext.getString(R.string.submission_status_card_body_pending)
        else -> appContext.getString(R.string.submission_status_card_body_pending)
    }
}

fun formatSubmissionStatusCardContentButtonText(testResult: TestResult?): String {
    val appContext = CoronaWarnApplication.getAppContext()
    return when (testResult) {
        INVALID, NEGATIVE, POSITIVE -> appContext.getString(R.string.submission_status_card_button_show_results)
        else -> appContext.getString(R.string.submission_status_card_button_show_details)
    }
}

fun formatSubmissionStatusCardContentButtonColor(testResult: TestResult?): Int {
    val appContext = CoronaWarnApplication.getAppContext()
    return when (testResult) {
        INVALID, NEGATIVE, POSITIVE -> appContext.getColor(R.color.tracingIconActive)
        else -> appContext.getColor(R.color.colorLight)
    }
}

fun formatSubmissionStatusCardContentButtonTextColor(testResult: TestResult?): Int {
    val appContext = CoronaWarnApplication.getAppContext()
    return when (testResult) {
        INVALID, NEGATIVE, POSITIVE -> appContext.getColor(R.color.colorLight)
        else -> appContext.getColor(R.color.tracingIconActive)
    }
}

fun formatSubmissionStatusCardContentStatusTextVisible(testResult: TestResult?): Int {
    return when (testResult) {
        POSITIVE, NEGATIVE, INVALID -> View.VISIBLE
        else -> View.GONE
    }
}

fun formatSubmissionStatusCardContentIcon(testResult: TestResult?): Drawable? {
    val appContext = CoronaWarnApplication.getAppContext()
    // TODO Replace with real drawables when design is finished
    return when (testResult) {
        PENDING -> appContext.getDrawable(R.drawable.ic_main_illustration_pending)
        POSITIVE -> appContext.getDrawable(R.drawable.ic_main_illustration_pending)
        NEGATIVE -> appContext.getDrawable(R.drawable.ic_main_illustration_negative)
        INVALID -> appContext.getDrawable(R.drawable.ic_main_illustration_invalid)
        else -> appContext.getDrawable(R.drawable.ic_main_illustration_invalid)
    }
}

fun formatSubmissionStatusCardFetchingVisible(
    deviceRegistered: Boolean?,
    testResultState: ApiRequestState?
): Int = formatVisibility(
    deviceRegistered == true && (
            testResultState == ApiRequestState.STARTED ||
                    testResultState == ApiRequestState.FAILED)
)

fun formatSubmissionStatusCardContentVisible(
    deviceRegistered: Boolean?,
    testResultState: ApiRequestState?
): Int = formatVisibility(deviceRegistered == true && testResultState == ApiRequestState.SUCCESS)

fun formatSubmissionTanButtonTint(isValidTanFormat: Boolean) = formatColor(
    isValidTanFormat,
    R.color.button_primary,
    R.color.colorGreyLight
)

fun formatSubmissionTanButtonTextColor(isValidTanFormat: Boolean) = formatColor(
    isValidTanFormat,
    R.color.textColorLight,
    R.color.colorGreyDisabled
)
