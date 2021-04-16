package de.rki.coronawarnapp.coronatest.type.rapidantigen

import de.rki.coronawarnapp.coronatest.type.CommonSubmissionStates
import org.joda.time.Instant

sealed class SubmissionStateRAT {

    object NoTest : SubmissionStateRAT(), CommonSubmissionStates.TestUnregistered
    object FetchingResult : SubmissionStateRAT(), CommonSubmissionStates.TestFetching
    object TestResultReady : SubmissionStateRAT()
    object TestPositive : SubmissionStateRAT()
    object TestNegative : SubmissionStateRAT()
    object TestError : SubmissionStateRAT()
    object TestInvalid : SubmissionStateRAT()
    object TestPending : SubmissionStateRAT()
    data class SubmissionDone(
        override val testRegisteredAt: Instant
    ) : SubmissionStateRAT(), CommonSubmissionStates.SubmissionDone
}
