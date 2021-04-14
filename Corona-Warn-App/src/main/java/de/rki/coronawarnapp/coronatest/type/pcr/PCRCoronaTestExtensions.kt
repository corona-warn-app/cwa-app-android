package de.rki.coronawarnapp.coronatest.type.pcr

import de.rki.coronawarnapp.exception.http.CwaServerError
import de.rki.coronawarnapp.util.CWADebug
import de.rki.coronawarnapp.util.DeviceUIState
import de.rki.coronawarnapp.util.NetworkRequestWrapper
import de.rki.coronawarnapp.util.NetworkRequestWrapper.Companion.withSuccess
import timber.log.Timber

fun PCRCoronaTest?.toSubmissionState(): SubmissionStatePCR {
    if (this == null) return NoTest

    val uiState: DeviceUIState = when (state) {
        PCRCoronaTest.State.PENDING -> DeviceUIState.PAIRED_NO_RESULT
        PCRCoronaTest.State.INVALID -> DeviceUIState.PAIRED_ERROR
        PCRCoronaTest.State.POSITIVE -> DeviceUIState.PAIRED_POSITIVE
        PCRCoronaTest.State.NEGATIVE -> DeviceUIState.PAIRED_NEGATIVE
        PCRCoronaTest.State.REDEEMED -> DeviceUIState.PAIRED_REDEEMED
    }

    val networkWrapper = when {
        isProcessing -> NetworkRequestWrapper.RequestStarted
        lastError != null -> NetworkRequestWrapper.RequestFailed(lastError)
        else -> NetworkRequestWrapper.RequestSuccessful(uiState)
    }

    val eval = Evaluation(
        deviceUiState = networkWrapper,
        isDeviceRegistered = registrationToken != null,
        hasTestResultBeenSeen = this.isViewed
    )
    Timber.d("eval: %s", eval)
    return when {
        eval.isUnregistered() -> NoTest
        eval.isFetching() -> FetchingResult
        eval.isTestResultReady() -> TestResultReady
        eval.isResultPositive() -> TestPositive
        eval.isInvalid() -> TestInvalid
        eval.isError() -> TestError
        eval.isResultNegative() -> TestNegative
        eval.isSubmissionDone() -> SubmissionDone(testRegisteredOn = registeredAt.toDate())
        eval.isPending() -> TestPending
        else -> if (CWADebug.isDeviceForTestersBuild) throw IllegalStateException(eval.toString()) else TestPending
    }
}

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
