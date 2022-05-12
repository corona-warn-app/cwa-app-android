package de.rki.coronawarnapp.familytest.ui.testlist

import de.rki.coronawarnapp.familytest.core.model.FamilyCoronaTest

sealed class FamilyTestListEvent {

    data class NavigateToDetails(val familyCoronaTest: FamilyCoronaTest) : FamilyTestListEvent()

    data class ConfirmRemoveTest(val familyCoronaTest: FamilyCoronaTest) : FamilyTestListEvent()

    data class DeleteTest(val familyCoronaTest: FamilyCoronaTest) : FamilyTestListEvent()

    object ConfirmRemoveAllTests : FamilyTestListEvent()

    data class ConfirmSwipeTest(val familyCoronaTest: FamilyCoronaTest, val position: Int) : FamilyTestListEvent()

    object NavigateBack : FamilyTestListEvent()
}
