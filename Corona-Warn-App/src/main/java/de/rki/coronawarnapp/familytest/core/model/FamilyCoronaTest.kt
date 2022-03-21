package de.rki.coronawarnapp.familytest.core.model

import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.reyclebin.common.Recyclable

interface FamilyCoronaTest : CoronaTest, Recyclable {
    val personName: String
}
