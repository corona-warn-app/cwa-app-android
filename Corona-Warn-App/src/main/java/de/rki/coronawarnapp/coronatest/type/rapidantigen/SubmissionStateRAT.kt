package de.rki.coronawarnapp.coronatest.type.rapidantigen

import de.rki.coronawarnapp.coronatest.type.CommonSubmissionStates
import java.time.Instant

sealed class SubmissionStateRAT {

    object NoTest : SubmissionStateRAT(), CommonSubmissionStates.TestUnregistered

    object FetchingResult : SubmissionStateRAT(), CommonSubmissionStates.TestFetching

    object TestResultReady : SubmissionStateRAT()

    data class TestPositive(
        override val testRegisteredAt: Instant
    ) : SubmissionStateRAT(), CommonSubmissionStates.PositiveTest

    data class TestNegative(
        override val testRegisteredAt: Instant
    ) : SubmissionStateRAT(), CommonSubmissionStates.NegativeTest

    object TestError : SubmissionStateRAT()

    object TestInvalid : SubmissionStateRAT()

    object TestPending : SubmissionStateRAT()

    object TestOutdated : SubmissionStateRAT()

    data class SubmissionDone(
        override val testRegisteredAt: Instant
    ) : SubmissionStateRAT(), CommonSubmissionStates.SubmissionDone
}
