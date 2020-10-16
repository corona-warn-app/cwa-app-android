package de.rki.coronawarnapp.ui.main.home

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentHomeBinding
import de.rki.coronawarnapp.util.ExternalActionHelper
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.errors.RecoveryByResetDialogFactory
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
class HomeFragment : Fragment(R.layout.fragment_home), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val vm: HomeFragmentViewModel by cwaViewModels { viewModelFactory }

    val binding: FragmentHomeBinding by viewBindingLazy()

    @Inject lateinit var homeMenu: HomeMenu
    @Inject lateinit var tracingExplanationDialog: TracingExplanationDialog

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vm.tracingHeaderState.observe2(this) {
            binding.tracingHeader = it
        }
        vm.tracingCardState.observe2(this) {
            binding.tracingCard = it
        }

        binding.settingsViewModel = vm.settingsViewModel
        binding.submissionViewModel = vm.submissionViewModel

        setupToolbar()

        setupTestResultCard()

        binding.mainTracing.setOnClickListener {
            doNavigate(HomeFragmentDirections.actionMainFragmentToSettingsTracingFragment())
        }

        setupRiskCard()

        binding.mainAbout.mainCard.apply {
            setOnClickListener {
                ExternalActionHelper.openUrl(this@HomeFragment, getString(R.string.main_about_link))
            }
            contentDescription = getString(R.string.hint_external_webpage)
        }

        vm.popupEvents.observe2(this) {
            when (it) {
                HomeFragmentEvents.ShowInteropDeltaOnboarding -> {
                    doNavigate(
                        HomeFragmentDirections.actionMainFragmentToOnboardingDeltaInteroperabilityFragment()
                    )
                }
                is HomeFragmentEvents.ShowTracingExplanation -> {
                    tracingExplanationDialog.show(it.activeTracingDaysInRetentionPeriod) {
                        vm.tracingExplanationWasShown()
                    }
                }
                HomeFragmentEvents.ShowErrorResetDialog -> {
                    RecoveryByResetDialogFactory(this).showDialog(
                        detailsLink = R.string.errors_generic_text_catastrophic_error_encryption_failure,
                        onDismiss = { vm.errorResetDialogDismissed() }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        vm.refreshRequiredData()
        binding.mainScrollview.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }

    private fun setupRiskCard() {
        binding.mainRisk.apply {
            riskCard.setOnClickListener {
                doNavigate(HomeFragmentDirections.actionMainFragmentToRiskDetailsFragment())
            }
            riskCardButtonUpdate.setOnClickListener {
                vm.refreshDiagnosisKeys()
                vm.settingsViewModel.updateManualKeyRetrievalEnabled(false)
            }
            riskCardButtonEnableTracing.setOnClickListener {
                doNavigate(HomeFragmentDirections.actionMainFragmentToSettingsTracingFragment())
            }
        }
    }

    private fun setupTestResultCard() {
        binding.apply {
            val toSubmissionResult = {
                doNavigate(HomeFragmentDirections.actionMainFragmentToSubmissionResultFragment())
            }
            mainTestUnregistered.apply {
                val toSubmissionIntro = {
                    doNavigate(HomeFragmentDirections.actionMainFragmentToSubmissionIntroFragment())
                }
                submissionStatusCardUnregistered.setOnClickListener { toSubmissionIntro() }
                submissionStatusCardUnregisteredButton.setOnClickListener { toSubmissionIntro() }
            }

            mainTestDone.submissionStatusCardDone.setOnClickListener {
                doNavigate(HomeFragmentDirections.actionMainFragmentToSubmissionDoneFragment())
            }
            mainTestResult.apply {
                submissionStatusCardContent.setOnClickListener { toSubmissionResult() }
                submissionStatusCardContentButton.setOnClickListener { toSubmissionResult() }
            }

            mainTestPositive.apply {
                submissionStatusCardPositive.setOnClickListener { toSubmissionResult() }
                submissionStatusCardPositiveButton.setOnClickListener { toSubmissionResult() }
            }
        }
    }

    private fun setupToolbar() {
        binding.mainHeaderShare.buttonIcon.apply {
            contentDescription = getString(R.string.button_share)
            setOnClickListener {
                doNavigate(HomeFragmentDirections.actionMainFragmentToMainSharingFragment())
            }
        }

        binding.mainHeaderOptionsMenu.buttonIcon.apply {
            contentDescription = getString(R.string.button_menu)
            setOnClickListener { homeMenu.showMenuFor(it) }
        }
    }
}
