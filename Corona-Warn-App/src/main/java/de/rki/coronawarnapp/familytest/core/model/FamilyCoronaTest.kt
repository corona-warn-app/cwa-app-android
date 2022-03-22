package de.rki.coronawarnapp.familytest.core.model

import de.rki.coronawarnapp.coronatest.type.CoronaTest

interface FamilyCoronaTest : CoronaTest {
    val personName: String
}
