package de.rki.coronawarnapp.ui.main.home

import androidx.annotation.StringRes
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import de.rki.coronawarnapp.coronatest.type.TestIdentifier

sealed class HomeFragmentEvents {

    data class ShowTracingExplanation(val maxEncounterAgeInDays: Long) : HomeFragmentEvents()

    object ShowErrorResetDialog : HomeFragmentEvents()

    data class HighRiskLevelDialog(
        val maxEncounterAgeInDays: Long
    ) : HomeFragmentEvents()

    data class LoweredRiskLevelDialog(
        val maxEncounterAgeInDays: Long
    ) : HomeFragmentEvents()

    object GoToStatisticsExplanation : HomeFragmentEvents()

    object GoToFederalStateSelection : HomeFragmentEvents()

    object GoToRiskDetailsFragment : HomeFragmentEvents()

    object GoToSettingsTracingFragment : HomeFragmentEvents()

    object GoToSubmissionDispatcher : HomeFragmentEvents()

    object OpenFAQUrl : HomeFragmentEvents()

    object GoToFamilyTests : HomeFragmentEvents()

    data class GoToRapidTestResultNegativeFragment(val identifier: TestIdentifier) : HomeFragmentEvents()

    data class GoToPcrTestResultNegativeFragment(val type: BaseCoronaTest.Type, val identifier: TestIdentifier) :
        HomeFragmentEvents()

    data class GoToTestResultKeysSharedFragment(val type: BaseCoronaTest.Type, val identifier: TestIdentifier) :
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
        val testType: BaseCoronaTest.Type,
        val forceUpdate: Boolean = false,
        val identifier: TestIdentifier
    ) : HomeFragmentEvents()

    data class ShowDeleteTestDialog(
        val type: BaseCoronaTest.Type,
        val submission: Boolean = true,
        val identifier: TestIdentifier
    ) : HomeFragmentEvents()

    data class GoToTestResultAvailableFragment(val type: BaseCoronaTest.Type, val identifier: TestIdentifier) :
        HomeFragmentEvents()

    data class GoToTestResultPositiveFragment(val type: BaseCoronaTest.Type, val identifier: TestIdentifier) :
        HomeFragmentEvents()

    data class DeleteOutdatedRAT(val identifier: TestIdentifier) : HomeFragmentEvents()
}
