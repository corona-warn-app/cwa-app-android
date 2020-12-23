package de.rki.coronawarnapp.submission.ui.homecards

sealed class SubmissionState

object NoTest : SubmissionState()
object FetchingResult : SubmissionState()
object TestResultReady : SubmissionState()
object TestPositive : SubmissionState()
object TestNegative : SubmissionState()
object TestError : SubmissionState()
object TestInvalid : SubmissionState()
object TestPending : SubmissionState()
object SubmissionDone : SubmissionState()
