package de.rki.coronawarnapp.coronatest.type

import de.rki.coronawarnapp.util.TimeAndDateExtensions.toShortDayFormat
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toUserTimeZone
import java.time.Instant

interface CommonSubmissionStates {
    interface TestUnregistered : CommonSubmissionStates

    interface TestFetching : CommonSubmissionStates

    interface PositiveTest : HasTestRegistrationDate, CommonSubmissionStates

    interface NegativeTest : HasTestRegistrationDate, CommonSubmissionStates

    interface SubmissionDone : HasTestRegistrationDate, CommonSubmissionStates

    interface HasTestRegistrationDate {
        val testRegisteredAt: Instant

        fun getFormattedRegistrationDate(): String =
            testRegisteredAt.toUserTimeZone().toLocalDate().toShortDayFormat()
    }
}
