package de.rki.coronawarnapp.ui.main.home

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.onNavDestinationSelected
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.bugreporting.ui.toErrorDialogBuilder
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.databinding.HomeFragmentLayoutBinding
import de.rki.coronawarnapp.tracing.ui.TracingExplanationDialog
import de.rki.coronawarnapp.ui.main.home.popups.DeviceTimeIncorrectDialog
import de.rki.coronawarnapp.util.CWADebug
import de.rki.coronawarnapp.util.ContextExtensions.getColorCompat
import de.rki.coronawarnapp.util.DialogHelper
import de.rki.coronawarnapp.util.ExternalActionHelper.openUrl
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.errors.RecoveryByResetDialogFactory
import de.rki.coronawarnapp.util.lists.decorations.TopBottomPaddingDecorator
import de.rki.coronawarnapp.util.lists.diffutil.update
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.findNestedGraph
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

/**
 * After the user has finished the onboarding this fragment will be the heart of the application.
 * Three ViewModels are needed that this fragment shows all relevant information to the user.
 * Also the Menu is set here.
 */
class HomeFragment : Fragment(R.layout.home_fragment_layout), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    @Inject lateinit var tracingExplanationDialog: TracingExplanationDialog
    @Inject lateinit var deviceTimeIncorrectDialog: DeviceTimeIncorrectDialog

    private val viewModel by cwaViewModels<HomeFragmentViewModel>(
        ownerProducer = { requireActivity().viewModelStore },
        factoryProducer = { viewModelFactory }
    )

    private val binding by viewBinding<HomeFragmentLayoutBinding>()
    private val homeAdapter = HomeAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        with(binding.toolbar) {
            menu.findItem(R.id.test_nav_graph).isVisible = CWADebug.isDeviceForTestersBuild
            setOnMenuItemClickListener { it.onNavDestinationSelected(findNavController()) }
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            itemAnimator = DefaultItemAnimator()
            addItemDecoration(TopBottomPaddingDecorator(topPadding = R.dimen.spacing_tiny))
            adapter = homeAdapter
        }

        binding.mainTracing.setOnClickListener {
            doNavigate(HomeFragmentDirections.actionMainFragmentToSettingsTracingFragment())
        }

        viewModel.showPopUps()
        viewModel.events.observe2(this) { event -> event?.let { navigate(event) } }
        viewModel.homeItems.observe2(this) { homeAdapter.update(it) }
        viewModel.errorEvent.observe2(this) { it.toErrorDialogBuilder(requireContext()).show() }
        viewModel.tracingHeaderState.observe2(this) { binding.tracingHeader = it }
        viewModel.showLoweredRiskLevelDialog.observe2(this) { if (it) showRiskLevelLoweredDialog() }
        viewModel.showIncorrectDeviceTimeDialog.observe2(this) { showDialog ->
            if (showDialog) deviceTimeIncorrectDialog.show { viewModel.userHasAcknowledgedIncorrectDeviceTime() }
        }
        viewModel.coronaTestErrors.observe2(this) { tests ->
            tests.forEach { test ->
                test.lastError?.toErrorDialogBuilder(requireContext())?.apply {
                    val testName = when (test.type) {
                        CoronaTest.Type.PCR -> R.string.ag_homescreen_card_pcr_title
                        CoronaTest.Type.RAPID_ANTIGEN -> R.string.ag_homescreen_card_rapidtest_title
                    }
                    setTitle(getString(testName) + " " + getString(R.string.errors_generic_headline_short))
                }?.show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshRequiredData()
        viewModel.restoreAppShortcuts()
        binding.container.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }

    private fun showRemoveTestDialog(type: CoronaTest.Type) {
        val removeTestDialog = DialogHelper.DialogInstance(
            requireActivity(),
            R.string.submission_test_result_dialog_remove_test_title,
            R.string.submission_test_result_dialog_remove_test_message,
            R.string.submission_test_result_dialog_remove_test_button_positive,
            R.string.submission_test_result_dialog_remove_test_button_negative,
            positiveButtonFunction = {
                viewModel.deregisterWarningAccepted(type)
            }
        )
        DialogHelper.showDialog(removeTestDialog).apply {
            getButton(DialogInterface.BUTTON_POSITIVE)
                .setTextColor(context.getColorCompat(R.color.colorTextSemanticRed))
        }
    }

    private fun showRiskLevelLoweredDialog() {
        val riskLevelLoweredDialog = DialogHelper.DialogInstance(
            context = requireActivity(),
            title = R.string.risk_lowered_dialog_headline,
            message = R.string.risk_lowered_dialog_body,
            positiveButton = R.string.risk_lowered_dialog_button_confirm,
            negativeButton = null,
            cancelable = false,
            positiveButtonFunction = { viewModel.userHasAcknowledgedTheLoweredRiskLevel() }
        )

        DialogHelper.showDialog(riskLevelLoweredDialog).apply {
            getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(context.getColorCompat(R.color.colorTextTint))
        }
    }

    private fun navigate(event: HomeFragmentEvents) {
        when (event) {
            HomeFragmentEvents.ShowErrorResetDialog -> {
                RecoveryByResetDialogFactory(this).showDialog(
                    detailsLink = R.string.errors_generic_text_catastrophic_error_encryption_failure,
                    onPositive = { viewModel.errorResetDialogDismissed() }
                )
            }
            HomeFragmentEvents.GoToStatisticsExplanation -> doNavigate(
                HomeFragmentDirections.actionMainFragmentToStatisticsExplanationFragment()
            )
            HomeFragmentEvents.ShowTracingExplanation -> tracingExplanationDialog.show {
                viewModel.tracingExplanationWasShown()
            }
            HomeFragmentEvents.GoToRiskDetailsFragment -> doNavigate(
                HomeFragmentDirections.actionMainFragmentToRiskDetailsFragment()
            )
            HomeFragmentEvents.GoToSettingsTracingFragment -> doNavigate(
                HomeFragmentDirections.actionMainFragmentToSettingsTracingFragment()
            )
            HomeFragmentEvents.GoToSubmissionDispatcher -> doNavigate(
                HomeFragmentDirections.actionMainFragmentToSubmissionDispatcher()
            )
            HomeFragmentEvents.OpenFAQUrl -> openUrl(getString(R.string.main_about_link))
            HomeFragmentEvents.GoToRapidTestResultNegativeFragment -> doNavigate(
                HomeFragmentDirections.actionMainFragmentToSubmissionNegativeAntigenTestResultFragment()
            )
            is HomeFragmentEvents.ShowDeleteTestDialog -> showRemoveTestDialog(event.type)
            is HomeFragmentEvents.OpenIncompatibleUrl -> openUrl(getString(event.url))
            is HomeFragmentEvents.OpenTraceLocationOrganizerGraph -> openPresenceTracingOrganizerGraph(event)
            is HomeFragmentEvents.GoToTestResultAvailableFragment -> doNavigate(
                HomeFragmentDirections.actionMainFragmentToSubmissionTestResultAvailableFragment(event.type)
            )
            is HomeFragmentEvents.GoToPcrTestResultNegativeFragment -> doNavigate(
                HomeFragmentDirections.actionMainFragmentToSubmissionTestResultNegativeFragment(event.type)
            )
            is HomeFragmentEvents.GoToTestResultKeysSharedFragment -> doNavigate(
                HomeFragmentDirections.actionMainFragmentToSubmissionTestResultKeysSharedFragment(event.type)
            )
            is HomeFragmentEvents.GoToTestResultPositiveFragment -> doNavigate(
                HomeFragmentDirections.actionMainFragmentToSubmissionResultPositiveOtherWarningNoConsentFragment(
                    event.type
                )
            )
            is HomeFragmentEvents.GoToTestResultPendingFragment -> doNavigate(
                HomeFragmentDirections.actionMainFragmentToSubmissionTestResultPendingFragment(
                    event.testType,
                    event.forceUpdate
                )
            )
        }
    }

    private fun openPresenceTracingOrganizerGraph(event: HomeFragmentEvents.OpenTraceLocationOrganizerGraph) {
        if (event.qrInfoAcknowledged) {
            findNestedGraph(R.id.trace_location_organizer_nav_graph).startDestination = R.id.traceLocationsFragment
        }
        doNavigate(HomeFragmentDirections.actionMainFragmentToTraceLocationOrganizerNavGraph())
    }
}
