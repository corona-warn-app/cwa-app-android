package de.rki.coronawarnapp.familytest.core.model

import de.rki.coronawarnapp.coronatest.type.CoronaTest

data class FamilyTest(
    val coronaTest: CoronaTest,
    val personName: String,
) : CoronaTest by coronaTest // To be manipulated as CoronaTest where needed
