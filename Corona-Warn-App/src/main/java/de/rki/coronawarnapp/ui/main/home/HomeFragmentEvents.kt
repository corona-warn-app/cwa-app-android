package de.rki.coronawarnapp.ui.main.home

sealed class HomeFragmentEvents {

    object ShowTracingExplanation : HomeFragmentEvents()

    object ShowErrorResetDialog : HomeFragmentEvents()

    object ShowDeleteTestDialog : HomeFragmentEvents()

    object GoToStatisticsExplanation : HomeFragmentEvents()
}
