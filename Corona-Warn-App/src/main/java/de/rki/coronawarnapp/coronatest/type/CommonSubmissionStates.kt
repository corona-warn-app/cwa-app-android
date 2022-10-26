package de.rki.coronawarnapp.coronatest.type

import de.rki.coronawarnapp.util.toLocalDateTimeUserTz
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

interface CommonSubmissionStates {
    interface TestUnregistered : CommonSubmissionStates

    interface TestFetching : CommonSubmissionStates

    interface PositiveTest : HasTestRegistrationDate, CommonSubmissionStates

    interface NegativeTest : HasTestRegistrationDate, CommonSubmissionStates

    interface SubmissionDone : HasTestRegistrationDate, CommonSubmissionStates

    interface HasTestRegistrationDate {
        val testRegisteredAt: Instant

        fun getFormattedRegistrationDate(): String = testRegisteredAt
            .toLocalDateTimeUserTz()
            .toLocalDate()
            .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT))
    }
}
