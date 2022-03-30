package de.rki.coronawarnapp.familytest.ui.testlist.items

import de.rki.coronawarnapp.familytest.core.model.FamilyCoronaTest
import de.rki.coronawarnapp.util.lists.HasStableId

interface FamilyTestListItem : HasStableId {

    val familyCoronaTest: FamilyCoronaTest

    override val stableId: Long
        get() = familyCoronaTest.coronaTest.identifier.hashCode().toLong()
}
