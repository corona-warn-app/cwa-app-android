package de.rki.coronawarnapp.ui.main.home

sealed class HomeFragmentEvents {
    object ShowInteropDeltaOnboarding : HomeFragmentEvents()

    data class ShowTracingExplanation(
        val activeTracingDaysInRetentionPeriod: Long
    ) : HomeFragmentEvents()

    object ShowErrorResetDialog : HomeFragmentEvents()
}
