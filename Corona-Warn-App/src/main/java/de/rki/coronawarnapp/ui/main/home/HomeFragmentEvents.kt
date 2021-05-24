package de.rki.coronawarnapp.ui.main.home

import androidx.annotation.StringRes
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.coronatest.type.CoronaTest

sealed class HomeFragmentEvents {

    object ShowTracingExplanation : HomeFragmentEvents()

    object ShowErrorResetDialog : HomeFragmentEvents()

    object GoToStatisticsExplanation : HomeFragmentEvents()

    object GoToRiskDetailsFragment : HomeFragmentEvents()

    object GoToSettingsTracingFragment : HomeFragmentEvents()

    object GoToSubmissionDispatcher : HomeFragmentEvents()

    object OpenFAQUrl : HomeFragmentEvents()

    object GoToRapidTestResultNegativeFragment : HomeFragmentEvents()

    data class GoToPcrTestResultNegativeFragment(val type: CoronaTest.Type) : HomeFragmentEvents()

    data class GoToTestResultKeysSharedFragment(val type: CoronaTest.Type) : HomeFragmentEvents()

    data class OpenIncompatibleUrl(val scanningSupported: Boolean) : HomeFragmentEvents() {
        @get:StringRes
        val url: Int
            get() = when {
                scanningSupported -> R.string.incompatible_link_advertising_not_supported
                else -> R.string.incompatible_link_scanning_not_supported
            }
    }

    data class OpenVaccinationRegistrationGraph(val registrationAcknowledged: Boolean) : HomeFragmentEvents()

    data class OpenTraceLocationOrganizerGraph(val qrInfoAcknowledged: Boolean) : HomeFragmentEvents()

    data class GoToTestResultPendingFragment(
        val testType: CoronaTest.Type,
        val forceUpdate: Boolean = false
    ) : HomeFragmentEvents()

    data class ShowDeleteTestDialog(val type: CoronaTest.Type) : HomeFragmentEvents()

    data class GoToTestResultAvailableFragment(val type: CoronaTest.Type) : HomeFragmentEvents()

    data class GoToTestResultPositiveFragment(val type: CoronaTest.Type) : HomeFragmentEvents()

    data class GoToVaccinationList(val personIdentifierCodeSha256: String) : HomeFragmentEvents()
}
