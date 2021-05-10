package de.rki.coronawarnapp.ui.main.home

import de.rki.coronawarnapp.coronatest.type.CoronaTest

sealed class HomeFragmentEvents {

    object ShowTracingExplanation : HomeFragmentEvents()

    object ShowErrorResetDialog : HomeFragmentEvents()

    data class ShowDeleteTestDialog(val type: CoronaTest.Type) : HomeFragmentEvents()

    object GoToStatisticsExplanation : HomeFragmentEvents()
    object VaccinationList : HomeFragmentEvents()
}
