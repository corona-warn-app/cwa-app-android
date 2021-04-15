package de.rki.coronawarnapp.coronatest.type.pcr

import de.rki.coronawarnapp.coronatest.type.CommonSubmissionStates
import org.joda.time.Instant

sealed class SubmissionStatePCR {

    object NoTest : SubmissionStatePCR(), CommonSubmissionStates.TestUnregistered
    object FetchingResult : SubmissionStatePCR(), CommonSubmissionStates.TestFetching
    object TestResultReady : SubmissionStatePCR()
    object TestPositive : SubmissionStatePCR()
    object TestNegative : SubmissionStatePCR()
    object TestError : SubmissionStatePCR()
    object TestInvalid : SubmissionStatePCR()
    object TestPending : SubmissionStatePCR()
    data class SubmissionDone(
        override val testRegisteredAt: Instant
    ) : SubmissionStatePCR(), CommonSubmissionStates.SubmissionDone
}
