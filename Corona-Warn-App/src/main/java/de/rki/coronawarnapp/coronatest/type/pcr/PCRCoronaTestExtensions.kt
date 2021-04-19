package de.rki.coronawarnapp.coronatest.type.pcr

import de.rki.coronawarnapp.coronatest.type.pcr.SubmissionStatePCR.FetchingResult
import de.rki.coronawarnapp.coronatest.type.pcr.SubmissionStatePCR.NoTest
import de.rki.coronawarnapp.coronatest.type.pcr.SubmissionStatePCR.SubmissionDone
import de.rki.coronawarnapp.coronatest.type.pcr.SubmissionStatePCR.TestError
import de.rki.coronawarnapp.coronatest.type.pcr.SubmissionStatePCR.TestInvalid
import de.rki.coronawarnapp.coronatest.type.pcr.SubmissionStatePCR.TestNegative
import de.rki.coronawarnapp.coronatest.type.pcr.SubmissionStatePCR.TestPending
import de.rki.coronawarnapp.coronatest.type.pcr.SubmissionStatePCR.TestPositive
import de.rki.coronawarnapp.coronatest.type.pcr.SubmissionStatePCR.TestResultReady
import de.rki.coronawarnapp.exception.http.CwaServerError
import de.rki.coronawarnapp.util.DeviceUIState
import de.rki.coronawarnapp.util.DeviceUIState.PAIRED_ERROR
import de.rki.coronawarnapp.util.DeviceUIState.PAIRED_NEGATIVE
import de.rki.coronawarnapp.util.DeviceUIState.PAIRED_NO_RESULT
import de.rki.coronawarnapp.util.DeviceUIState.PAIRED_POSITIVE
import de.rki.coronawarnapp.util.DeviceUIState.PAIRED_REDEEMED
import de.rki.coronawarnapp.util.DeviceUIState.SUBMITTED_FINAL
import de.rki.coronawarnapp.util.DeviceUIState.UNPAIRED
import de.rki.coronawarnapp.util.NetworkRequestWrapper.RequestFailed
import de.rki.coronawarnapp.util.NetworkRequestWrapper.RequestIdle
import de.rki.coronawarnapp.util.NetworkRequestWrapper.RequestStarted
import de.rki.coronawarnapp.util.NetworkRequestWrapper.RequestSuccessful

fun PCRCoronaTest?.toSubmissionState(): SubmissionStatePCR {
    if (this == null) return NoTest

    val uiState: DeviceUIState = when (state) {
        PCRCoronaTest.State.PENDING -> PAIRED_NO_RESULT
        PCRCoronaTest.State.INVALID -> PAIRED_ERROR
        PCRCoronaTest.State.POSITIVE -> PAIRED_POSITIVE
        PCRCoronaTest.State.NEGATIVE -> PAIRED_NEGATIVE
        PCRCoronaTest.State.REDEEMED -> PAIRED_REDEEMED
    }

    val networkWrapper = when {
        isProcessing -> RequestStarted
        lastError != null -> RequestFailed(lastError)
        else -> RequestSuccessful(uiState)
    }

    return when (networkWrapper) {
        is RequestStarted, is RequestIdle -> FetchingResult
        is RequestFailed -> if (networkWrapper.error is CwaServerError) TestPending else TestInvalid
        is RequestSuccessful -> when (networkWrapper.data) {
            PAIRED_ERROR -> TestError
            SUBMITTED_FINAL -> SubmissionDone(testRegisteredAt = registeredAt)
            PAIRED_POSITIVE -> if (isViewed) TestPositive else TestResultReady
            PAIRED_NEGATIVE -> TestNegative
            PAIRED_REDEEMED -> TestInvalid
            PAIRED_NO_RESULT -> TestPending
            UNPAIRED -> NoTest
        }
    }
}
