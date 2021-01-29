package de.rki.coronawarnapp.ui.main.home

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.ui.ContactDiaryActivity
import de.rki.coronawarnapp.databinding.HomeFragmentLayoutBinding
import de.rki.coronawarnapp.tracing.ui.TracingExplanationDialog
import de.rki.coronawarnapp.ui.main.home.popups.DeviceTimeIncorrectDialog
import de.rki.coronawarnapp.util.ContextExtensions.getColorCompat
import de.rki.coronawarnapp.util.DialogHelper
import de.rki.coronawarnapp.util.ExternalActionHelper
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
    private val vm: HomeFragmentViewModel by cwaViewModels(
        ownerProducer = { requireActivity().viewModelStore },
        factoryProducer = { viewModelFactory }
    )

    val binding: HomeFragmentLayoutBinding by viewBindingLazy()

    @Inject lateinit var homeMenu: HomeMenu
    @Inject lateinit var tracingExplanationDialog: TracingExplanationDialog
    @Inject lateinit var deviceTimeIncorrectDialog: DeviceTimeIncorrectDialog

    private val homeAdapter = HomeAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        homeMenu.setupMenu(binding.toolbar)

        vm.tracingHeaderState.observe2(this) {
            binding.tracingHeader = it
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            itemAnimator = DefaultItemAnimator()
            addItemDecoration(TopBottomPaddingDecorator(topPadding = R.dimen.spacing_tiny))
            adapter = homeAdapter
        }

        vm.homeItems.observe2(this) {
            homeAdapter.update(it)
        }

        vm.routeToScreen.observe2(this) {
            doNavigate(it)
        }

        binding.mainTracing.setOnClickListener {
            doNavigate(HomeFragmentDirections.actionMainFragmentToSettingsTracingFragment())
        }

        vm.openFAQUrlEvent.observe2(this) {
            ExternalActionHelper.openUrl(this@HomeFragment, getString(R.string.main_about_link))
        }

        vm.popupEvents.observe2(this) { event ->
            when (event) {
                HomeFragmentEvents.ShowInteropDeltaOnboarding -> {
                    doNavigate(
                        HomeFragmentDirections.actionMainFragmentToOnboardingDeltaInteroperabilityFragment()
                    )
                }
                is HomeFragmentEvents.ShowTracingExplanation -> {
                    tracingExplanationDialog.show(event.activeTracingDaysInRetentionPeriod) {
                        vm.tracingExplanationWasShown()
                    }
                }
                HomeFragmentEvents.ShowErrorResetDialog -> {
                    RecoveryByResetDialogFactory(this).showDialog(
                        detailsLink = R.string.errors_generic_text_catastrophic_error_encryption_failure,
                        onPositive = { vm.errorResetDialogDismissed() }
                    )
                }
                HomeFragmentEvents.ShowDeleteTestDialog -> {
                    showRemoveTestDialog()
                }
                HomeFragmentEvents.GoToContactDiary -> {
                    context?.let { ContactDiaryActivity.start(it) }
                }
                HomeFragmentEvents.GoToStatisticsExplanation -> {
                    doNavigate(
                        HomeFragmentDirections.actionMainFragmentToStatisticsExplanationFragment()
                    )
                }
            }
        }

        vm.showLoweredRiskLevelDialog.observe2(this) {
            if (it) showRiskLevelLoweredDialog()
        }
        vm.showIncorrectDeviceTimeDialog.observe2(this) { showDialog ->
            if (!showDialog) return@observe2
            deviceTimeIncorrectDialog.show { vm.userHasAcknowledgedIncorrectDeviceTime() }
        }

        vm.observeTestResultToSchedulePositiveTestResultReminder()
    }

    override fun onResume() {
        super.onResume()
        vm.refreshRequiredData()
        binding.container.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }

    private fun showRemoveTestDialog() {
        val removeTestDialog = DialogHelper.DialogInstance(
            requireActivity(),
            R.string.submission_test_result_dialog_remove_test_title,
            R.string.submission_test_result_dialog_remove_test_message,
            R.string.submission_test_result_dialog_remove_test_button_positive,
            R.string.submission_test_result_dialog_remove_test_button_negative,
            positiveButtonFunction = {
                vm.deregisterWarningAccepted()
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
            positiveButtonFunction = { vm.userHasAcknowledgedTheLoweredRiskLevel() }
        )

        DialogHelper.showDialog(riskLevelLoweredDialog).apply {
            getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(context.getColorCompat(R.color.colorTextTint))
        }
    }
}
