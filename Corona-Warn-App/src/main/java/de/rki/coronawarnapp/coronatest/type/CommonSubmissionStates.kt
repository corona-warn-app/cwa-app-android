package de.rki.coronawarnapp.coronatest.type

import de.rki.coronawarnapp.util.TimeAndDateExtensions.toUserTimeZone
import org.joda.time.Instant

interface CommonSubmissionStates {
    interface TestUnregistered : CommonSubmissionStates

    interface TestFetching : CommonSubmissionStates

    interface PositiveTest : HasTestRegistrationDate, CommonSubmissionStates

    interface NegativeTest : HasTestRegistrationDate, CommonSubmissionStates

    interface SubmissionDone : HasTestRegistrationDate, CommonSubmissionStates

    interface HasTestRegistrationDate {
        val testRegisteredAt: Instant

        fun getFormattedRegistrationDate(): String =
            testRegisteredAt.toUserTimeZone().toLocalDate().toString("dd.MM.yy")
    }
}
