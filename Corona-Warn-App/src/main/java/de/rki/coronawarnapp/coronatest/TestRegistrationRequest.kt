package de.rki.coronawarnapp.coronatest

import de.rki.coronawarnapp.coronatest.type.CoronaTest
import org.joda.time.LocalDate

interface TestRegistrationRequest {
    val type: CoronaTest.Type
    val identifier: String
    val isDccSupportedByPoc: Boolean
    val isDccConsentGiven: Boolean
    val dateOfBirth: LocalDate?
}
