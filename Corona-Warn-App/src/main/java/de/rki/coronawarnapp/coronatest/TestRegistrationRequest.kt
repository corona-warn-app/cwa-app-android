package de.rki.coronawarnapp.coronatest

import de.rki.coronawarnapp.coronatest.type.CoronaTest

interface TestRegistrationRequest {
    val type: CoronaTest.Type
    val identifier: String
}
