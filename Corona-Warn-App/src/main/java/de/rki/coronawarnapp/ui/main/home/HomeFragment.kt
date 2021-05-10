package de.rki.coronawarnapp.ui.main.home

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import androidx.navigation.NavGraph
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
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBindingLazy
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
    private val viewModel: HomeFragmentViewModel by cwaViewModels(
        ownerProducer = { requireActivity().viewModelStore },
        factoryProducer = { viewModelFactory }
    )

    val binding: HomeFragmentLayoutBinding by viewBindingLazy()

    @Inject lateinit var tracingExplanationDialog: TracingExplanationDialog
    @Inject lateinit var deviceTimeIncorrectDialog: DeviceTimeIncorrectDialog

    private val homeAdapter = HomeAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding.toolbar) {
            menu.findItem(R.id.test_nav_graph).isVisible = CWADebug.isDeviceForTestersBuild
            setOnMenuItemClickListener { it.onNavDestinationSelected(findNavController()) }
        }

        viewModel.tracingHeaderState.observe2(this) {
            binding.tracingHeader = it
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            itemAnimator = DefaultItemAnimator()
            addItemDecoration(TopBottomPaddingDecorator(topPadding = R.dimen.spacing_tiny))
            adapter = homeAdapter
        }

        viewModel.homeItems.observe2(this) {
            homeAdapter.update(it)
        }

        viewModel.routeToScreen.observe2(this) {
            doNavigate(it)
        }

        binding.mainTracing.setOnClickListener {
            doNavigate(HomeFragmentDirections.actionMainFragmentToSettingsTracingFragment())
        }

        viewModel.openFAQUrlEvent.observe2(this) {
            openUrl(getString(R.string.main_about_link))
        }

        viewModel.openIncompatibleEvent.observe2(this) {
            openUrl(getString(R.string.incompatible_link))
        }

        viewModel.openTraceLocationOrganizerFlow.observe2(this) {
            if (viewModel.wasQRInfoWasAcknowledged()) {
                val nestedGraph =
                    findNavController().graph.findNode(R.id.trace_location_organizer_nav_graph) as NavGraph
                nestedGraph.startDestination = R.id.traceLocationsFragment
            }
            doNavigate(HomeFragmentDirections.actionMainFragmentToTraceLocationOrganizerNavGraph())
        }

        viewModel.openVaccinationRegistrationFlow.observe2(this) {
            // TODO: should we show consent only once?
            doNavigate(HomeFragmentDirections.actionMainFragmentToTraceVaccinationNavGraph())
        }

        viewModel.popupEvents.observe2(this) { event ->
            when (event) {
                HomeFragmentEvents.ShowErrorResetDialog -> {
                    RecoveryByResetDialogFactory(this).showDialog(
                        detailsLink = R.string.errors_generic_text_catastrophic_error_encryption_failure,
                        onPositive = { viewModel.errorResetDialogDismissed() }
                    )
                }
                is HomeFragmentEvents.ShowDeleteTestDialog -> showRemoveTestDialog(event.type)
                HomeFragmentEvents.GoToStatisticsExplanation -> doNavigate(
                    HomeFragmentDirections.actionMainFragmentToStatisticsExplanationFragment()
                )
                HomeFragmentEvents.ShowTracingExplanation -> {
                    tracingExplanationDialog.show {
                        viewModel.tracingExplanationWasShown()
                    }
                }
            }
        }

        viewModel.showPopUps()

        viewModel.showLoweredRiskLevelDialog.observe2(this) {
            if (it) showRiskLevelLoweredDialog()
        }
        viewModel.showIncorrectDeviceTimeDialog.observe2(this) { showDialog ->
            if (!showDialog) return@observe2
            deviceTimeIncorrectDialog.show { viewModel.userHasAcknowledgedIncorrectDeviceTime() }
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

        viewModel.errorEvent.observe2(this) {
            it.toErrorDialogBuilder(requireContext()).show()
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
            getButton(AlertDialog.BUTTON_POSITIVE)
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
            getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(context.getColorCompat(R.color.colorTextTint))
        }
    }
}
