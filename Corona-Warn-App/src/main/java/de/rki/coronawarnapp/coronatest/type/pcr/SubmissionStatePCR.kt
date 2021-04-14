package de.rki.coronawarnapp.coronatest.type.pcr

import android.content.Context
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toUIFormat
import java.util.Date

sealed class SubmissionStatePCR

object NoTest : SubmissionStatePCR()
object FetchingResult : SubmissionStatePCR()
object TestResultReady : SubmissionStatePCR()
object TestPositive : SubmissionStatePCR()
object TestNegative : SubmissionStatePCR()
object TestError : SubmissionStatePCR()
object TestInvalid : SubmissionStatePCR()
object TestPending : SubmissionStatePCR()
data class SubmissionDone(val testRegisteredOn: Date) : SubmissionStatePCR() {

    fun formatTestRegistrationText(context: Context): String =
        context.getString(R.string.reenable_risk_card_test_registration_string)
            .format(testRegisteredOn.toUIFormat(context))
}
