package de.rki.coronawarnapp.ui.main.home.items.testresult

import dagger.Reusable
import de.rki.coronawarnapp.exception.http.CwaServerError
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.storage.SubmissionRepository
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
            else -> throw IllegalStateException()
        }
    }
        .onStart { Timber.v("SubmissionCardState FLOW start") }
        .onEach { Timber.w("SubmissionCardState FLOW emission: %s", it) }
        .onCompletion { Timber.v("SubmissionCardState FLOW completed.") }

    // TODO Refactor this to be easier to understand
    private data class Evaluation(
        val deviceUiState: NetworkRequestWrapper<DeviceUIState, Throwable>,
        val isDeviceRegistered: Boolean,
        val hasTestResultBeenSeen: Boolean
    ) {

        fun isTestResultReady(): Boolean = deviceUiState.withSuccess(false) {
            when (it) {
                DeviceUIState.PAIRED_POSITIVE,
                DeviceUIState.PAIRED_POSITIVE_TELETAN -> !hasTestResultBeenSeen
                else -> false
            }
        }

        fun isUnregistered(): Boolean = !isDeviceRegistered

        fun isFetching(): Boolean =
            isDeviceRegistered && when (deviceUiState) {
                is NetworkRequestWrapper.RequestFailed -> deviceUiState.error is CwaServerError
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
            deviceUiState.withSuccess(false) {
                when (it) {
                    DeviceUIState.PAIRED_ERROR, DeviceUIState.PAIRED_NO_RESULT -> true
                    else -> false
                }
            }
    }
}
