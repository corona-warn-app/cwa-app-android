package de.rki.coronawarnapp.coronatest.type.rapidantigen

import de.rki.coronawarnapp.appconfig.CoronaTestConfig
import de.rki.coronawarnapp.coronatest.type.rapidantigen.RACoronaTest.State.INVALID
import de.rki.coronawarnapp.coronatest.type.rapidantigen.RACoronaTest.State.NEGATIVE
import de.rki.coronawarnapp.coronatest.type.rapidantigen.RACoronaTest.State.OUTDATED
import de.rki.coronawarnapp.coronatest.type.rapidantigen.RACoronaTest.State.PENDING
import de.rki.coronawarnapp.coronatest.type.rapidantigen.RACoronaTest.State.POSITIVE
import de.rki.coronawarnapp.coronatest.type.rapidantigen.RACoronaTest.State.RECYCLED
import de.rki.coronawarnapp.coronatest.type.rapidantigen.RACoronaTest.State.REDEEMED
import de.rki.coronawarnapp.coronatest.type.rapidantigen.SubmissionStateRAT.FetchingResult
import de.rki.coronawarnapp.coronatest.type.rapidantigen.SubmissionStateRAT.NoTest
import de.rki.coronawarnapp.coronatest.type.rapidantigen.SubmissionStateRAT.SubmissionDone
import de.rki.coronawarnapp.coronatest.type.rapidantigen.SubmissionStateRAT.TestError
import de.rki.coronawarnapp.coronatest.type.rapidantigen.SubmissionStateRAT.TestInvalid
import de.rki.coronawarnapp.coronatest.type.rapidantigen.SubmissionStateRAT.TestNegative
import de.rki.coronawarnapp.coronatest.type.rapidantigen.SubmissionStateRAT.TestOutdated
import de.rki.coronawarnapp.coronatest.type.rapidantigen.SubmissionStateRAT.TestPending
import de.rki.coronawarnapp.coronatest.type.rapidantigen.SubmissionStateRAT.TestPositive
import de.rki.coronawarnapp.coronatest.type.rapidantigen.SubmissionStateRAT.TestResultReady
import de.rki.coronawarnapp.exception.http.BadRequestException
import org.joda.time.Instant

fun RACoronaTest?.toSubmissionState(nowUTC: Instant = Instant.now(), coronaTestConfig: CoronaTestConfig): SubmissionStateRAT {
    if (this == null) return NoTest
    val state = getState(nowUTC, coronaTestConfig)
    return when {
        isSubmitted && isViewed -> SubmissionDone(testRegisteredAt = registeredAt)
        isProcessing && state == PENDING-> FetchingResult
        lastError is BadRequestException -> TestInvalid
        else -> when (state) {
            INVALID -> TestError
            POSITIVE -> {
                if (isViewed) TestPositive(testRegisteredAt = testTakenAt)
                else TestResultReady
            }
            NEGATIVE -> TestNegative(testRegisteredAt = testTakenAt)
            REDEEMED -> TestInvalid
            PENDING -> TestPending
            OUTDATED -> TestOutdated
            RECYCLED -> NoTest
        }
    }
}
