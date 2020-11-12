package de.rki.coronawarnapp.ui.main.home

import android.content.Context
import android.graphics.drawable.Drawable
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.ui.submission.ApiRequestState
import de.rki.coronawarnapp.util.DeviceUIState
import de.rki.coronawarnapp.util.DeviceUIState.PAIRED_ERROR
import de.rki.coronawarnapp.util.DeviceUIState.PAIRED_NEGATIVE
import de.rki.coronawarnapp.util.DeviceUIState.PAIRED_NO_RESULT
import de.rki.coronawarnapp.util.DeviceUIState.PAIRED_POSITIVE
import de.rki.coronawarnapp.util.DeviceUIState.PAIRED_POSITIVE_TELETAN
import de.rki.coronawarnapp.util.DeviceUIState.PAIRED_REDEEMED
import de.rki.coronawarnapp.util.DeviceUIState.SUBMITTED_FINAL

data class SubmissionCardState(
    val deviceUiState: DeviceUIState,
    val isDeviceRegistered: Boolean,
    val uiStateState: ApiRequestState
) {

    fun isRiskCardVisible(): Boolean = deviceUiState != PAIRED_POSITIVE &&
        deviceUiState != PAIRED_POSITIVE_TELETAN &&
        deviceUiState != SUBMITTED_FINAL

    fun isUnregisteredCardVisible(): Boolean = !isDeviceRegistered

    fun isFetchingCardVisible(): Boolean =
        isDeviceRegistered && (uiStateState == ApiRequestState.STARTED || uiStateState == ApiRequestState.FAILED)

    fun isFailedCardVisible(): Boolean =
        isDeviceRegistered && uiStateState == ApiRequestState.SUCCESS && deviceUiState == PAIRED_REDEEMED

    fun isPositiveSubmissionCardVisible(): Boolean = uiStateState == ApiRequestState.SUCCESS &&
        (deviceUiState == PAIRED_POSITIVE ||
            deviceUiState == PAIRED_POSITIVE_TELETAN)

    fun isSubmissionDoneCardVisible(): Boolean =
        uiStateState == ApiRequestState.SUCCESS && deviceUiState == SUBMITTED_FINAL

    fun isContentCardVisible(): Boolean =
        uiStateState == ApiRequestState.SUCCESS && (deviceUiState == PAIRED_ERROR ||
            deviceUiState == PAIRED_NEGATIVE ||
            deviceUiState == PAIRED_NO_RESULT)

    fun getContentCardTitleText(c: Context): String = when (deviceUiState) {
        PAIRED_ERROR, PAIRED_REDEEMED, PAIRED_NEGATIVE -> R.string.submission_status_card_title_available
        PAIRED_NO_RESULT -> R.string.submission_status_card_title_pending
        else -> R.string.submission_status_card_title_pending
    }.let { c.getString(it) }

    fun getContentCardSubTitleText(c: Context): String = when (deviceUiState) {
        PAIRED_NEGATIVE -> R.string.submission_status_card_subtitle_negative
        PAIRED_ERROR, PAIRED_REDEEMED -> R.string.submission_status_card_subtitle_invalid
        else -> null
    }?.let { c.getString(it) } ?: ""

    fun getContentCardSubTitleTextColor(c: Context): Int = when (deviceUiState) {
        PAIRED_NEGATIVE -> R.color.colorTextSemanticGreen
        PAIRED_ERROR, PAIRED_REDEEMED -> R.color.colorTextSemanticNeutral
        else -> R.color.colorTextPrimary1
    }.let { c.getColor(it) }

    fun isContentCardStatusTextVisible(): Boolean = when (deviceUiState) {
        PAIRED_NEGATIVE, PAIRED_REDEEMED, PAIRED_ERROR -> true
        else -> false
    }

    fun getContentCardBodyText(c: Context): String = when (deviceUiState) {
        PAIRED_ERROR, PAIRED_REDEEMED -> R.string.submission_status_card_body_invalid
        PAIRED_NEGATIVE -> R.string.submission_status_card_body_negative
        PAIRED_NO_RESULT -> R.string.submission_status_card_body_pending
        else -> R.string.submission_status_card_body_pending
    }.let { c.getString(it) }

    fun getContentCardIcon(c: Context): Drawable? = when (deviceUiState) {
        PAIRED_NO_RESULT -> R.drawable.ic_main_illustration_pending
        PAIRED_POSITIVE, PAIRED_POSITIVE_TELETAN -> R.drawable.ic_main_illustration_pending
        PAIRED_NEGATIVE -> R.drawable.ic_main_illustration_negative
        PAIRED_ERROR, PAIRED_REDEEMED -> R.drawable.ic_main_illustration_invalid
        else -> R.drawable.ic_main_illustration_invalid
    }.let { c.getDrawable(it) }
}
