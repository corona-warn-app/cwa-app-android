package de.rki.coronawarnapp.submission.ui.homecards

import android.content.Context
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toUIFormat
import java.util.Date

sealed class SubmissionState

object NoTest : SubmissionState()
object FetchingResult : SubmissionState()
object TestResultReady : SubmissionState()
object TestPositive : SubmissionState()
object TestNegative : SubmissionState()
object TestError : SubmissionState()
object TestInvalid : SubmissionState()
object TestPending : SubmissionState()
data class SubmissionDone(val testRegisteredOn: Date) : SubmissionState() {

    fun formatTestRegistrationText(context: Context): String =
        context.getString(R.string.reenable_risk_card_test_registration_string)
            .format(testRegisteredOn.toUIFormat(context))
}
