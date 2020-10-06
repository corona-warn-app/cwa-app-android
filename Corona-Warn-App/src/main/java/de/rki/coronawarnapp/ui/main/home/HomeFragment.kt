package de.rki.coronawarnapp.ui.main.home

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentHomeBinding
import de.rki.coronawarnapp.ui.doNavigate
import de.rki.coronawarnapp.util.ExternalActionHelper
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.errors.RecoveryByResetDialogFactory
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

    private val binding: FragmentHomeBinding by viewBindingLazy()

    @Inject lateinit var homeMenu: HomeMenu
    @Inject lateinit var tracingExplanationDialog: TracingExplanationDialog

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tracingViewModel = vm.tracingViewModel
        binding.settingsViewModel = vm.settingsViewModel
        binding.submissionViewModel = vm.submissionViewModel

        setupToolbar()

        setupTestResultCard()

        binding.mainTracing.setOnClickListener {
            findNavController().doNavigate(HomeFragmentDirections.actionMainFragmentToSettingsTracingFragment())
        }

        setupRiskCard()

        binding.mainAbout.mainCard.apply {
            setOnClickListener {
                ExternalActionHelper.openUrl(this@HomeFragment, getString(R.string.main_about_link))
            }
            contentDescription = getString(R.string.hint_external_webpage)
        }

        vm.showInteropDeltaOnboarding.observe2(this) {
            findNavController().doNavigate(
                HomeFragmentDirections.actionMainFragmentToOnboardingDeltaInteroperabilityFragment()
            )
        }

        vm.showTracingExplanation.observe2(this) { activeTracingDaysInRetentionPeriod ->
            tracingExplanationDialog.show(activeTracingDaysInRetentionPeriod) { vm.tracingExplanationWasShown() }
        }

        vm.showErrorResetDialog.observe2(this) {
            RecoveryByResetDialogFactory(this).showDialog(
                detailsLink = R.string.errors_generic_text_catastrophic_error_encryption_failure,
                onDismiss = { vm.errorResetDialogDismissed() }
            )
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
                findNavController().doNavigate(
                    HomeFragmentDirections.actionMainFragmentToRiskDetailsFragment()
                )
            }
            riskCardButtonUpdate.setOnClickListener {
                vm.tracingViewModel.refreshDiagnosisKeys()
                vm.settingsViewModel.updateManualKeyRetrievalEnabled(false)
            }
            riskCardButtonEnableTracing.setOnClickListener {
                findNavController().doNavigate(
                    HomeFragmentDirections.actionMainFragmentToSettingsTracingFragment()
                )
            }
        }
    }

    private fun setupTestResultCard() {
        binding.apply {
            val toSubmissionResult = {
                findNavController().doNavigate(
                    HomeFragmentDirections.actionMainFragmentToSubmissionResultFragment()
                )
            }
            mainTestUnregistered.apply {
                val toSubmissionIntro = {
                    findNavController().doNavigate(
                        HomeFragmentDirections.actionMainFragmentToSubmissionIntroFragment()
                    )
                }
                submissionStatusCardUnregistered.setOnClickListener { toSubmissionIntro() }
                submissionStatusCardUnregisteredButton.setOnClickListener { toSubmissionIntro() }
            }

            mainTestDone.submissionStatusCardDone.setOnClickListener {
                findNavController().doNavigate(
                    HomeFragmentDirections.actionMainFragmentToSubmissionDoneFragment()
                )
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
                findNavController().doNavigate(
                    HomeFragmentDirections.actionMainFragmentToMainSharingFragment()
                )
            }
        }

        binding.mainHeaderOptionsMenu.buttonIcon.apply {
            contentDescription = getString(R.string.button_menu)
            setOnClickListener { homeMenu.showMenuFor(it) }
        }
    }
}
