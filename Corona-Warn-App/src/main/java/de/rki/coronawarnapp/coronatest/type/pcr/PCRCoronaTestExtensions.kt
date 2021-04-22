package de.rki.coronawarnapp.coronatest.type.pcr

import de.rki.coronawarnapp.coronatest.type.pcr.PCRCoronaTest.State.INVALID
import de.rki.coronawarnapp.coronatest.type.pcr.PCRCoronaTest.State.NEGATIVE
import de.rki.coronawarnapp.coronatest.type.pcr.PCRCoronaTest.State.PENDING
import de.rki.coronawarnapp.coronatest.type.pcr.PCRCoronaTest.State.POSITIVE
import de.rki.coronawarnapp.coronatest.type.pcr.PCRCoronaTest.State.REDEEMED
import de.rki.coronawarnapp.coronatest.type.pcr.SubmissionStatePCR.FetchingResult
import de.rki.coronawarnapp.coronatest.type.pcr.SubmissionStatePCR.NoTest
import de.rki.coronawarnapp.coronatest.type.pcr.SubmissionStatePCR.TestError
import de.rki.coronawarnapp.coronatest.type.pcr.SubmissionStatePCR.TestInvalid
import de.rki.coronawarnapp.coronatest.type.pcr.SubmissionStatePCR.TestNegative
import de.rki.coronawarnapp.coronatest.type.pcr.SubmissionStatePCR.TestPending
import de.rki.coronawarnapp.coronatest.type.pcr.SubmissionStatePCR.TestPositive
import de.rki.coronawarnapp.coronatest.type.pcr.SubmissionStatePCR.TestResultReady
import de.rki.coronawarnapp.exception.http.CwaServerError

fun PCRCoronaTest?.toSubmissionState() = when {
    this == null -> NoTest
    isSubmitted -> SubmissionStatePCR.SubmissionDone(testRegisteredAt = registeredAt)
    isProcessing -> FetchingResult
    lastError != null -> if (lastError is CwaServerError) TestPending else TestInvalid
    else -> when (state) {
        INVALID -> TestError
        POSITIVE -> when {
            isViewed -> TestPositive(testRegisteredAt = registeredAt)
            else -> TestResultReady
        }
        NEGATIVE -> TestNegative(testRegisteredAt = registeredAt)
        REDEEMED -> TestInvalid
        PENDING -> TestPending
    }
}
