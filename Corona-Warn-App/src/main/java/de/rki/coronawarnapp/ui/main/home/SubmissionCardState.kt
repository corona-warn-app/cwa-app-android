package de.rki.coronawarnapp.ui.main.home

import android.content.Context
import android.graphics.drawable.Drawable
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.exception.http.CwaServerError
import de.rki.coronawarnapp.util.DeviceUIState
import de.rki.coronawarnapp.util.DeviceUIState.PAIRED_ERROR
import de.rki.coronawarnapp.util.DeviceUIState.PAIRED_NEGATIVE
import de.rki.coronawarnapp.util.DeviceUIState.PAIRED_NO_RESULT
import de.rki.coronawarnapp.util.DeviceUIState.PAIRED_POSITIVE
import de.rki.coronawarnapp.util.DeviceUIState.PAIRED_POSITIVE_TELETAN
import de.rki.coronawarnapp.util.DeviceUIState.PAIRED_REDEEMED
import de.rki.coronawarnapp.util.DeviceUIState.SUBMITTED_FINAL
import de.rki.coronawarnapp.util.NetworkRequestWrapper
import de.rki.coronawarnapp.util.NetworkRequestWrapper.Companion.withSuccess

data class SubmissionCardState(
    val deviceUiState: NetworkRequestWrapper<DeviceUIState, Throwable>,
    val isDeviceRegistered: Boolean
) {

    fun isRiskCardVisible(): Boolean =
        deviceUiState.withSuccess(true) {
            when (it) {
                PAIRED_POSITIVE, PAIRED_POSITIVE_TELETAN, SUBMITTED_FINAL -> false
                else -> true
            }
        }

    fun isUnregisteredCardVisible(): Boolean = !isDeviceRegistered

    fun isFetchingCardVisible(): Boolean =
        isDeviceRegistered && when (deviceUiState) {
            is NetworkRequestWrapper.RequestFailed -> deviceUiState.error is CwaServerError
            is NetworkRequestWrapper.RequestStarted -> true
            else -> false
        }

    fun isFailedCardVisible(): Boolean =
        isDeviceRegistered && when (deviceUiState) {
            is NetworkRequestWrapper.RequestFailed -> deviceUiState.error !is CwaServerError
            is NetworkRequestWrapper.RequestSuccessful -> deviceUiState.data == PAIRED_REDEEMED
            else -> false
        }

    fun isPositiveSubmissionCardVisible(): Boolean =
        deviceUiState.withSuccess(false) {
            when (it) {
                PAIRED_POSITIVE, PAIRED_POSITIVE_TELETAN -> true
                else -> false
            }
        }

    fun isSubmissionDoneCardVisible(): Boolean =
        when (deviceUiState) {
            is NetworkRequestWrapper.RequestSuccessful -> deviceUiState.data == SUBMITTED_FINAL
            else -> false
        }

    fun isContentCardVisible(): Boolean =
        deviceUiState.withSuccess(false) {
            when (it) {
                PAIRED_ERROR, PAIRED_NEGATIVE, PAIRED_NO_RESULT -> true
                else -> false
            }
        }

    fun getContentCardTitleText(c: Context): String =
        deviceUiState.withSuccess(R.string.submission_status_card_title_pending) {
            when (it) {
                PAIRED_ERROR, PAIRED_REDEEMED, PAIRED_NEGATIVE -> R.string.submission_status_card_title_available
                PAIRED_NO_RESULT -> R.string.submission_status_card_title_pending
                else -> R.string.submission_status_card_title_pending
            }
        }.let { c.getString(it) }

    fun getContentCardSubTitleText(c: Context): String =
        deviceUiState.withSuccess(null) {
            when (it) {
                PAIRED_NEGATIVE -> R.string.submission_status_card_subtitle_negative
                PAIRED_ERROR, PAIRED_REDEEMED -> R.string.submission_status_card_subtitle_invalid
                else -> null
            }
        }?.let { c.getString(it) } ?: ""

    fun getContentCardSubTitleTextColor(c: Context): Int =
        deviceUiState.withSuccess(R.color.colorTextPrimary1) {
            when (it) {
                PAIRED_NEGATIVE -> R.color.colorTextSemanticGreen
                PAIRED_ERROR, PAIRED_REDEEMED -> R.color.colorTextSemanticNeutral
                else -> R.color.colorTextPrimary1
            }
        }.let { c.getColor(it) }

    fun isContentCardStatusTextVisible(): Boolean =
        deviceUiState.withSuccess(false) {
            when (it) {
                PAIRED_NEGATIVE, PAIRED_REDEEMED, PAIRED_ERROR -> true
                else -> false
            }
        }

    fun getContentCardBodyText(c: Context): String =
        deviceUiState.withSuccess(R.string.submission_status_card_body_pending) {
            when (it) {
                PAIRED_ERROR, PAIRED_REDEEMED -> R.string.submission_status_card_body_invalid
                PAIRED_NEGATIVE -> R.string.submission_status_card_body_negative
                PAIRED_NO_RESULT -> R.string.submission_status_card_body_pending
                else -> R.string.submission_status_card_body_pending
            }
        }.let { c.getString(it) }

    fun getContentCardIcon(c: Context): Drawable? =
        deviceUiState.withSuccess(R.drawable.ic_main_illustration_invalid) {
            when (it) {
                PAIRED_NO_RESULT -> R.drawable.ic_main_illustration_pending
                PAIRED_POSITIVE, PAIRED_POSITIVE_TELETAN -> R.drawable.ic_main_illustration_pending
                PAIRED_NEGATIVE -> R.drawable.ic_main_illustration_negative
                PAIRED_ERROR, PAIRED_REDEEMED -> R.drawable.ic_main_illustration_invalid
                else -> R.drawable.ic_main_illustration_invalid
            }
        }.let { c.getDrawable(it) }
}
