package de.rki.coronawarnapp.ui.main.home.items.testresult

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
