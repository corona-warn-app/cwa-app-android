package de.rki.coronawarnapp.coronatest.type.pcr

import de.rki.coronawarnapp.coronatest.type.CommonSubmissionStates
import java.time.Instant

sealed class SubmissionStatePCR {

    object NoTest : SubmissionStatePCR(), CommonSubmissionStates.TestUnregistered

    object FetchingResult : SubmissionStatePCR(), CommonSubmissionStates.TestFetching

    object TestResultReady : SubmissionStatePCR()

    data class TestPositive(
        override val testRegisteredAt: Instant
    ) : SubmissionStatePCR(), CommonSubmissionStates.PositiveTest

    data class TestNegative(
        override val testRegisteredAt: Instant
    ) : SubmissionStatePCR(), CommonSubmissionStates.NegativeTest

    object TestError : SubmissionStatePCR()

    object TestInvalid : SubmissionStatePCR()

    object TestPending : SubmissionStatePCR()

    data class SubmissionDone(
        override val testRegisteredAt: Instant
    ) : SubmissionStatePCR(), CommonSubmissionStates.SubmissionDone
}
