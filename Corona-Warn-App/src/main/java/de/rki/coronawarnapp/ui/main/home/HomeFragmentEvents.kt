package de.rki.coronawarnapp.ui.main.home

sealed class HomeFragmentEvents {

    object ShowErrorResetDialog : HomeFragmentEvents()

    object ShowDeleteTestDialog : HomeFragmentEvents()

    object ShowReactivateRiskCheckDialog : HomeFragmentEvents()

    object GoToStatisticsExplanation : HomeFragmentEvents()
}
