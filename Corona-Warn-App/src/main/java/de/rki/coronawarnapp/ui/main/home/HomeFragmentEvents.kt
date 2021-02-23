package de.rki.coronawarnapp.ui.main.home

sealed class HomeFragmentEvents {

    data class ShowTracingExplanation(
        val activeTracingDaysInRetentionPeriod: Long
    ) : HomeFragmentEvents()

    object ShowErrorResetDialog : HomeFragmentEvents()

    object ShowDeleteTestDialog : HomeFragmentEvents()

    object ShowReactivateRiskCheckDialog : HomeFragmentEvents()

    object GoToStatisticsExplanation : HomeFragmentEvents()
}
