package de.rki.coronawarnapp.coronatest.type.rapidantigen

import de.rki.coronawarnapp.coronatest.type.rapidantigen.RACoronaTest.State.INVALID
import de.rki.coronawarnapp.coronatest.type.rapidantigen.RACoronaTest.State.NEGATIVE
import de.rki.coronawarnapp.coronatest.type.rapidantigen.RACoronaTest.State.OUTDATED
import de.rki.coronawarnapp.coronatest.type.rapidantigen.RACoronaTest.State.PENDING
import de.rki.coronawarnapp.coronatest.type.rapidantigen.RACoronaTest.State.POSITIVE
import de.rki.coronawarnapp.coronatest.type.rapidantigen.RACoronaTest.State.REDEEMED
import de.rki.coronawarnapp.coronatest.type.rapidantigen.SubmissionStateRAT.FetchingResult
import de.rki.coronawarnapp.coronatest.type.rapidantigen.SubmissionStateRAT.NoTest
import de.rki.coronawarnapp.coronatest.type.rapidantigen.SubmissionStateRAT.TestError
import de.rki.coronawarnapp.coronatest.type.rapidantigen.SubmissionStateRAT.TestInvalid
import de.rki.coronawarnapp.coronatest.type.rapidantigen.SubmissionStateRAT.TestNegative
import de.rki.coronawarnapp.coronatest.type.rapidantigen.SubmissionStateRAT.TestPending
import de.rki.coronawarnapp.coronatest.type.rapidantigen.SubmissionStateRAT.TestPositive
import de.rki.coronawarnapp.coronatest.type.rapidantigen.SubmissionStateRAT.TestResultReady
import de.rki.coronawarnapp.exception.http.CwaServerError
import org.joda.time.Instant

fun RACoronaTest?.toSubmissionState(nowUTC: Instant = Instant.now()) = when {
    this == null -> NoTest
    isProcessing -> FetchingResult
    lastError != null -> if (lastError is CwaServerError) TestPending else TestInvalid
    else -> when (getState(nowUTC)) {
        INVALID -> TestError
        POSITIVE -> {
            if (isViewed) TestPositive(testRegisteredAt = registeredAt)
            else TestResultReady
        }
        NEGATIVE -> TestNegative(testRegisteredAt = registeredAt)
        REDEEMED -> TestInvalid
        PENDING -> TestPending
        // TODO: Should be updated once the logic for OUTDATED tests is in
        OUTDATED -> TestNegative(testRegisteredAt = registeredAt)
    }
}
