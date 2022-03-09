package de.rki.coronawarnapp.ui.main.home

import androidx.annotation.StringRes
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.coronatest.type.TestIdentifier

sealed class HomeFragmentEvents {

    object ShowTracingExplanation : HomeFragmentEvents()

    object ShowErrorResetDialog : HomeFragmentEvents()

    object ShowAdditionalHighRiskDialog : HomeFragmentEvents()

    object GoToStatisticsExplanation : HomeFragmentEvents()

    object GoToFederalStateSelection : HomeFragmentEvents()

    object GoToRiskDetailsFragment : HomeFragmentEvents()

    object GoToSettingsTracingFragment : HomeFragmentEvents()

    object GoToSubmissionDispatcher : HomeFragmentEvents()

    object OpenFAQUrl : HomeFragmentEvents()

    data class GoToRapidTestResultNegativeFragment(val identifier: TestIdentifier) : HomeFragmentEvents()

    data class GoToPcrTestResultNegativeFragment(val type: CoronaTest.Type, val identifier: TestIdentifier) :
        HomeFragmentEvents()

    data class GoToTestResultKeysSharedFragment(val type: CoronaTest.Type, val identifier: TestIdentifier) :
        HomeFragmentEvents()

    data class OpenIncompatibleUrl(val scanningSupported: Boolean) : HomeFragmentEvents() {
        @get:StringRes
        val url: Int
            get() = when {
                scanningSupported -> R.string.incompatible_link_advertising_not_supported
                else -> R.string.incompatible_link_scanning_not_supported
            }
    }

    data class OpenTraceLocationOrganizerGraph(val qrInfoAcknowledged: Boolean) : HomeFragmentEvents()

    data class GoToTestResultPendingFragment(
        val testType: CoronaTest.Type,
        val forceUpdate: Boolean = false,
        val identifier: TestIdentifier
    ) : HomeFragmentEvents()

    data class ShowDeleteTestDialog(
        val type: CoronaTest.Type,
        val submission: Boolean = true,
        val identifier: TestIdentifier
    ) : HomeFragmentEvents()

    data class GoToTestResultAvailableFragment(val type: CoronaTest.Type, val identifier: TestIdentifier) :
        HomeFragmentEvents()

    data class GoToTestResultPositiveFragment(val type: CoronaTest.Type, val identifier: TestIdentifier) :
        HomeFragmentEvents()

    data class DeleteOutdatedRAT(val identifier: TestIdentifier) : HomeFragmentEvents()
}
