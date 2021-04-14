package de.rki.coronawarnapp.coronatest.type.rapidantigen

import android.content.Context
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toUIFormat
import java.util.Date

sealed class SubmissionStateRAT

object NoTest : SubmissionStateRAT()
object FetchingResult : SubmissionStateRAT()
object TestResultReady : SubmissionStateRAT()
object TestPositive : SubmissionStateRAT()
object TestNegative : SubmissionStateRAT()
object TestError : SubmissionStateRAT()
object TestInvalid : SubmissionStateRAT()
object TestPending : SubmissionStateRAT()
data class SubmissionDone(val testRegisteredOn: Date) : SubmissionStateRAT() {

    fun formatTestRegistrationText(context: Context): String =
        context.getString(R.string.reenable_risk_card_test_registration_string)
            .format(testRegisteredOn.toUIFormat(context))
}
