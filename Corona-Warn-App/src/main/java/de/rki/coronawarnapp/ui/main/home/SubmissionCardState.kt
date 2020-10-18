package de.rki.coronawarnapp.ui.main.home

import de.rki.coronawarnapp.ui.submission.ApiRequestState
import de.rki.coronawarnapp.util.DeviceUIState
import de.rki.coronawarnapp.util.DeviceUIState.PAIRED_POSITIVE
import de.rki.coronawarnapp.util.DeviceUIState.PAIRED_POSITIVE_TELETAN
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

    fun isContentCardVisible(): Boolean = deviceUiState == DeviceUIState.PAIRED_ERROR ||
        deviceUiState == DeviceUIState.PAIRED_NEGATIVE ||
        deviceUiState == DeviceUIState.PAIRED_NO_RESULT ||
        deviceUiState == DeviceUIState.PAIRED_REDEEMED

    fun isFetchingCardVisible(): Boolean = isDeviceRegistered &&
        (uiStateState == ApiRequestState.STARTED || uiStateState == ApiRequestState.FAILED)

    fun isPositiveSubmissionCardVisible(): Boolean = deviceUiState == PAIRED_POSITIVE ||
        deviceUiState == PAIRED_POSITIVE_TELETAN

    fun isSubmissionDoneCardVisible(): Boolean = deviceUiState == SUBMITTED_FINAL
}
