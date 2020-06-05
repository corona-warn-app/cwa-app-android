@file:JvmName("FormatterSubmissionHelper")

package de.rki.coronawarnapp.util.formatter

import android.graphics.drawable.Drawable
import android.view.View
import de.rki.coronawarnapp.CoronaWarnApplication
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.ui.submission.ApiRequestState
import de.rki.coronawarnapp.util.DeviceUIState
import java.util.Date

fun formatTestResultSpinnerVisible(uiStateState: ApiRequestState?): Int =
    formatVisibility(uiStateState != ApiRequestState.SUCCESS)

fun formatTestResultVisible(uiStateState: ApiRequestState?): Int =
    formatVisibility(uiStateState == ApiRequestState.SUCCESS)

fun formatTestResultVirusNameTextVisible(uiState: DeviceUIState?): Int {
    return when (uiState) {
        DeviceUIState.PAIRED_POSITIVE,
        DeviceUIState.PAIRED_POSITIVE_TELETAN,
        DeviceUIState.PAIRED_NEGATIVE -> View.VISIBLE
        else -> View.GONE
    }
}

fun formatTestResultStatusTextVisible(uiState: DeviceUIState?): Int {
    return when (uiState) {
        DeviceUIState.PAIRED_POSITIVE,
        DeviceUIState.PAIRED_POSITIVE_TELETAN,
        DeviceUIState.PAIRED_NEGATIVE -> View.VISIBLE
        else -> View.GONE
    }
}

fun formatTestResultStatusText(uiState: DeviceUIState?): String {
    val appContext = CoronaWarnApplication.getAppContext()
    return when (uiState) {
        DeviceUIState.PAIRED_NEGATIVE -> appContext.getString(R.string.test_result_card_status_negative)
        DeviceUIState.PAIRED_POSITIVE,
        DeviceUIState.PAIRED_POSITIVE_TELETAN -> appContext.getString(R.string.test_result_card_status_positive)
        else -> appContext.getString(R.string.test_result_card_status_invalid)
    }
}

fun formatTestResultStatusColor(uiState: DeviceUIState?): Int {
    val appContext = CoronaWarnApplication.getAppContext()
    return when (uiState) {
        DeviceUIState.PAIRED_NEGATIVE -> appContext.getColor(R.color.colorTextSemanticGreen)
        DeviceUIState.PAIRED_POSITIVE,
        DeviceUIState.PAIRED_POSITIVE_TELETAN -> appContext.getColor(R.color.colorTextSemanticRed)
        else -> appContext.getColor(R.color.colorTextSemanticRed)
    }
}

fun formatTestStatusIcon(uiState: DeviceUIState?): Drawable? {
    val appContext = CoronaWarnApplication.getAppContext()
    // TODO Replace with real drawables when design is finished
    return when (uiState) {
        DeviceUIState.PAIRED_NO_RESULT -> appContext.getDrawable(R.drawable.ic_test_result_illustration_pending)
        DeviceUIState.PAIRED_POSITIVE_TELETAN,
        DeviceUIState.PAIRED_POSITIVE -> appContext.getDrawable(R.drawable.ic_test_result_illustration_positive)
        DeviceUIState.PAIRED_NEGATIVE -> appContext.getDrawable(R.drawable.ic_main_illustration_negative)
        DeviceUIState.PAIRED_ERROR -> appContext.getDrawable(R.drawable.ic_test_result_illustration_invalid)
        else -> appContext.getDrawable(R.drawable.ic_test_result_illustration_invalid)
    }
}

fun formatTestResultInvalidStatusTextVisible(uiState: DeviceUIState?): Int =
    formatVisibility(uiState == DeviceUIState.PAIRED_ERROR)

fun formatTestResultPendingStatusTextVisible(uiState: DeviceUIState?): Int =
    formatVisibility(uiState == DeviceUIState.PAIRED_NO_RESULT)

fun formatTestResultRegisteredAtText(registeredAt: Date?): String {
    val appContext = CoronaWarnApplication.getAppContext()
    return appContext.getString(R.string.test_result_card_registered_at_text).format(registeredAt)
}

fun formatTestResultPendingStepsVisible(uiState: DeviceUIState?): Int =
    formatVisibility(uiState == DeviceUIState.PAIRED_NO_RESULT)

fun formatTestResultNegativeStepsVisible(uiState: DeviceUIState?): Int =
    formatVisibility(uiState == DeviceUIState.PAIRED_NEGATIVE)

fun formatTestResultPositiveStepsVisible(uiState: DeviceUIState?): Int =
    formatVisibility(uiState == DeviceUIState.PAIRED_POSITIVE || uiState == DeviceUIState.PAIRED_POSITIVE_TELETAN)

fun formatTestResultInvalidStepsVisible(uiState: DeviceUIState?): Int =
    formatVisibility(uiState == DeviceUIState.PAIRED_ERROR)

fun formatSubmissionStatusCardContentTitleText(uiState: DeviceUIState?): String {
    val appContext = CoronaWarnApplication.getAppContext()
    return when (uiState) {
        DeviceUIState.PAIRED_ERROR,
        DeviceUIState.PAIRED_NEGATIVE,
        DeviceUIState.PAIRED_POSITIVE_TELETAN,
        DeviceUIState.PAIRED_POSITIVE -> appContext.getString(R.string.submission_status_card_title_available)
        DeviceUIState.PAIRED_NO_RESULT -> appContext.getString(R.string.submission_status_card_title_pending)
        else -> appContext.getString(R.string.submission_status_card_title_pending)
    }
}

fun formatSubmissionStatusCardContentBodyText(uiState: DeviceUIState?): String {
    val appContext = CoronaWarnApplication.getAppContext()
    return when (uiState) {
        DeviceUIState.PAIRED_ERROR -> appContext.getString(R.string.submission_status_card_body_invalid)
        DeviceUIState.PAIRED_NEGATIVE -> appContext.getString(R.string.submission_status_card_body_negative)
        DeviceUIState.PAIRED_POSITIVE,
        DeviceUIState.PAIRED_POSITIVE_TELETAN -> appContext.getString(R.string.submission_status_card_body_positive)
        DeviceUIState.PAIRED_NO_RESULT -> appContext.getString(R.string.submission_status_card_body_pending)
        else -> appContext.getString(R.string.submission_status_card_body_pending)
    }
}

fun formatSubmissionStatusCardContentButtonText(uiState: DeviceUIState?): String {
    val appContext = CoronaWarnApplication.getAppContext()
    return when (uiState) {
        DeviceUIState.PAIRED_ERROR,
        DeviceUIState.PAIRED_NEGATIVE,
        DeviceUIState.PAIRED_POSITIVE_TELETAN,
        DeviceUIState.PAIRED_POSITIVE -> appContext.getString(R.string.submission_status_card_button_show_results)
        else -> appContext.getString(R.string.submission_status_card_button_show_details)
    }
}

fun formatSubmissionStatusCardContentStatusTextVisible(uiState: DeviceUIState?): Int {
    return when (uiState) {
        DeviceUIState.PAIRED_POSITIVE,
        DeviceUIState.PAIRED_POSITIVE_TELETAN,
        DeviceUIState.PAIRED_NEGATIVE,
        DeviceUIState.PAIRED_ERROR -> View.VISIBLE
        else -> View.GONE
    }
}

fun formatSubmissionStatusCardContentIcon(uiState: DeviceUIState?): Drawable? {
    val appContext = CoronaWarnApplication.getAppContext()
    // TODO Replace with real drawables when design is finished
    return when (uiState) {
        DeviceUIState.PAIRED_NO_RESULT -> appContext.getDrawable(R.drawable.ic_main_illustration_pending)
        DeviceUIState.PAIRED_POSITIVE,
        DeviceUIState.PAIRED_POSITIVE_TELETAN -> appContext.getDrawable(R.drawable.ic_main_illustration_pending)
        DeviceUIState.PAIRED_NEGATIVE -> appContext.getDrawable(R.drawable.ic_main_illustration_negative)
        DeviceUIState.PAIRED_ERROR -> appContext.getDrawable(R.drawable.ic_main_illustration_invalid)
        else -> appContext.getDrawable(R.drawable.ic_main_illustration_invalid)
    }
}

fun formatSubmissionStatusCardFetchingVisible(
    deviceRegistered: Boolean?,
    uiStateState: ApiRequestState?
): Int = formatVisibility(
    deviceRegistered == true && (
            uiStateState == ApiRequestState.STARTED ||
                    uiStateState == ApiRequestState.FAILED)
)

fun formatSubmissionStatusCardContentVisible(
    deviceRegistered: Boolean?,
    uiStateState: ApiRequestState?
): Int = formatVisibility(deviceRegistered == true && uiStateState == ApiRequestState.SUCCESS)

fun formatShowSubmissionStatusCard(deviceUiState: DeviceUIState?): Int =
    formatVisibility(
        deviceUiState != DeviceUIState.PAIRED_POSITIVE &&
                deviceUiState != DeviceUIState.PAIRED_POSITIVE_TELETAN &&
                deviceUiState != DeviceUIState.SUBMITTED_FINAL
    )

fun formatShowSubmissionStatusPositiveCard(deviceUiState: DeviceUIState?): Int =
    formatVisibility(
        deviceUiState == DeviceUIState.PAIRED_POSITIVE ||
                deviceUiState == DeviceUIState.PAIRED_POSITIVE_TELETAN
    )

fun formatShowSubmissionDoneCard(deviceUiState: DeviceUIState?): Int =
    formatVisibility(deviceUiState == DeviceUIState.SUBMITTED_FINAL)

fun formatShowRiskStatusCard(deviceUiState: DeviceUIState?): Int =
    formatVisibility(
        deviceUiState != DeviceUIState.PAIRED_POSITIVE &&
                deviceUiState != DeviceUIState.PAIRED_POSITIVE_TELETAN &&
                deviceUiState != DeviceUIState.SUBMITTED_FINAL
    )
