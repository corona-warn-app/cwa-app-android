package de.rki.coronawarnapp.coronatest.type

import org.joda.time.Instant

interface CommonSubmissionStates {
    interface TestUnregistered : CommonSubmissionStates

    interface TestFetching : CommonSubmissionStates

    interface SubmissionDone : CommonSubmissionStates {
        val testRegisteredAt: Instant
    }
}
