package de.rki.coronawarnapp.ui.main.home

import de.rki.coronawarnapp.coronatest.type.CoronaTest

sealed class HomeFragmentEvents {

    object ShowTracingExplanation : HomeFragmentEvents()

    object ShowErrorResetDialog : HomeFragmentEvents()

    object GoToStatisticsExplanation : HomeFragmentEvents()

    data class ShowDeleteTestDialog(val type: CoronaTest.Type) : HomeFragmentEvents()

    data class GoToVaccinationList(val personIdentifierCode: String) : HomeFragmentEvents()
}
