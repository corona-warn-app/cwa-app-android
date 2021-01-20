package de.rki.coronawarnapp.submission.ui.homecards

import dagger.Reusable
import de.rki.coronawarnapp.exception.http.CwaServerError
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.util.CWADebug
import de.rki.coronawarnapp.util.DeviceUIState
import de.rki.coronawarnapp.util.NetworkRequestWrapper
import de.rki.coronawarnapp.util.NetworkRequestWrapper.Companion.withSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import timber.log.Timber
import javax.inject.Inject

@Reusable
class SubmissionStateProvider @Inject constructor(
    submissionRepository: SubmissionRepository
) {

    val state: Flow<SubmissionState> = combine(
        submissionRepository.deviceUIStateFlow,
        submissionRepository.hasViewedTestResult
    ) { uiState, hasTestBeenSeen ->

        val eval = Evaluation(
            deviceUiState = uiState,
            isDeviceRegistered = LocalData.registrationToken() != null,
            hasTestResultBeenSeen = hasTestBeenSeen
        )
        Timber.d("eval: %s", eval)
        when {
            eval.isUnregistered() -> NoTest
            eval.isFetching() -> FetchingResult
            eval.isTestResultReady() -> TestResultReady
            eval.isResultPositive() -> TestPositive
            eval.isInvalid() -> TestInvalid
            eval.isError() -> TestError
            eval.isResultNegative() -> TestNegative
            eval.isSubmissionDone() -> SubmissionDone
            eval.isPending() -> TestPending
            else -> if (CWADebug.isDeviceForTestersBuild) throw IllegalStateException() else TestPending
        }
    }
        .onStart { Timber.v("SubmissionStateProvider FLOW start") }
        .onEach { Timber.w("SubmissionStateProvider FLOW emission: %s", it) }
        .onCompletion { Timber.v("SubmissionStateProvider FLOW completed.") }

    // TODO Refactor this to be easier to understand, probably remove the "withSuccess" logic.
    private data class Evaluation(
        val deviceUiState: NetworkRequestWrapper<DeviceUIState, Throwable>,
        val isDeviceRegistered: Boolean,
        val hasTestResultBeenSeen: Boolean
    ) {

        fun isUnregistered(): Boolean = !isDeviceRegistered

        fun isTestResultReady(): Boolean = deviceUiState.withSuccess(false) {
            when (it) {
                DeviceUIState.PAIRED_POSITIVE,
                DeviceUIState.PAIRED_POSITIVE_TELETAN -> !hasTestResultBeenSeen
                else -> false
            }
        }

        fun isFetching(): Boolean =
            isDeviceRegistered && when (deviceUiState) {
                is NetworkRequestWrapper.RequestFailed -> false
                is NetworkRequestWrapper.RequestStarted -> true
                is NetworkRequestWrapper.RequestIdle -> true
                else -> false
            }

        fun isResultPositive(): Boolean =
            deviceUiState.withSuccess(false) {
                when (it) {
                    DeviceUIState.PAIRED_POSITIVE, DeviceUIState.PAIRED_POSITIVE_TELETAN -> hasTestResultBeenSeen
                    else -> false
                }
            }

        fun isResultNegative(): Boolean =
            deviceUiState.withSuccess(false) {
                when (it) {
                    DeviceUIState.PAIRED_NEGATIVE -> true
                    else -> false
                }
            }

        fun isSubmissionDone(): Boolean =
            when (deviceUiState) {
                is NetworkRequestWrapper.RequestSuccessful -> deviceUiState.data == DeviceUIState.SUBMITTED_FINAL
                else -> false
            }

        fun isInvalid(): Boolean =
            isDeviceRegistered && when (deviceUiState) {
                is NetworkRequestWrapper.RequestFailed -> deviceUiState.error !is CwaServerError
                is NetworkRequestWrapper.RequestSuccessful -> deviceUiState.data == DeviceUIState.PAIRED_REDEEMED
                else -> false
            }

        fun isError(): Boolean =
            deviceUiState.withSuccess(false) {
                when (it) {
                    DeviceUIState.PAIRED_ERROR -> true
                    else -> false
                }
            }

        fun isPending(): Boolean =
            when (deviceUiState) {
                is NetworkRequestWrapper.RequestFailed -> true
                is NetworkRequestWrapper.RequestSuccessful -> {
                    deviceUiState.data == DeviceUIState.PAIRED_ERROR ||
                        deviceUiState.data == DeviceUIState.PAIRED_NO_RESULT
                }
                else -> false
            }
    }
}
