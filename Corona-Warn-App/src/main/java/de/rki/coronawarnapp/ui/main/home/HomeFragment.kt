package de.rki.coronawarnapp.ui.main.home

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentHomeBinding
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.timer.TimerHelper
import de.rki.coronawarnapp.ui.doNavigate
import de.rki.coronawarnapp.ui.viewmodel.SettingsViewModel
import de.rki.coronawarnapp.ui.viewmodel.SubmissionViewModel
import de.rki.coronawarnapp.ui.viewmodel.TracingViewModel
import de.rki.coronawarnapp.util.CWADebug
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
 *
 * @see tracingViewModel
 * @see settingsViewModel
 * @see submissionViewModel
 * @see PopupMenu
 */
class HomeFragment : Fragment(R.layout.fragment_home), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val vm: HomeFragmentViewModel by cwaViewModels { viewModelFactory }

    private val tracingViewModel: TracingViewModel by activityViewModels()
    private val settingsViewModel: SettingsViewModel by activityViewModels()
    private val submissionViewModel: SubmissionViewModel by activityViewModels()
    private val binding: FragmentHomeBinding by viewBindingLazy()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tracingViewModel = tracingViewModel
        binding.settingsViewModel = settingsViewModel
        binding.submissionViewModel = submissionViewModel

        binding.apply {
            mainTestUnregistered.submissionStatusCardUnregistered.setOnClickListener {
                toSubmissionIntro()
            }
            mainTestUnregistered.submissionStatusCardUnregisteredButton.setOnClickListener {
                toSubmissionIntro()
            }
            mainTestDone.submissionStatusCardDone.setOnClickListener {
                findNavController().doNavigate(
                    HomeFragmentDirections.actionMainFragmentToSubmissionDoneFragment()
                )
            }
            mainTestResult.submissionStatusCardContent.setOnClickListener {
                toSubmissionResult()
            }
            mainTestResult.submissionStatusCardContentButton.setOnClickListener {
                toSubmissionResult()
            }
            mainTestPositive.submissionStatusCardPositive.setOnClickListener {
                toSubmissionResult()
            }
            mainTestPositive.submissionStatusCardPositiveButton.setOnClickListener {
                toSubmissionResult()
            }
            mainTracing.setOnClickListener {
                findNavController().doNavigate(HomeFragmentDirections.actionMainFragmentToSettingsTracingFragment())
            }
            mainRisk.riskCard.setOnClickListener {
                findNavController().doNavigate(HomeFragmentDirections.actionMainFragmentToRiskDetailsFragment())
            }
            mainRisk.riskCardButtonUpdate.setOnClickListener {
                this@HomeFragment.tracingViewModel.refreshDiagnosisKeys()
                this@HomeFragment.settingsViewModel.updateManualKeyRetrievalEnabled(false)
            }
            mainRisk.riskCardButtonEnableTracing.setOnClickListener {
                findNavController().doNavigate(HomeFragmentDirections.actionMainFragmentToSettingsTracingFragment())
            }
            mainAbout.mainCard.setOnClickListener {
                ExternalActionHelper.openUrl(
                    this@HomeFragment,
                    requireContext().getString(R.string.main_about_link)
                )
            }
            mainHeaderShare.buttonIcon.setOnClickListener {
                findNavController().doNavigate(HomeFragmentDirections.actionMainFragmentToMainSharingFragment())
            }
            mainHeaderOptionsMenu.buttonIcon.setOnClickListener {
                showPopup(it)
            }
        }

        binding.apply {
            mainHeaderShare.buttonIcon.contentDescription = getString(R.string.button_share)
            mainHeaderOptionsMenu.buttonIcon.contentDescription = getString(R.string.button_menu)
            mainAbout.mainCard.contentDescription = getString(R.string.hint_external_webpage)
        }

        if (!LocalData.isInteroperabilityShownAtLeastOnce) {
            findNavController().doNavigate(
                HomeFragmentDirections.actionMainFragmentToOnboardingDeltaInteroperabilityFragment()
            )
        }

        vm.showTracingExplanationDialog.observe2(this) { activeTracingDaysInRetentionPeriod ->
            TracingExplanationDialog.create(requireContext(), activeTracingDaysInRetentionPeriod)
        }

        vm.showErrorResetDialog.observe2(this) {
            RecoveryByResetDialogFactory(this).showDialog(
                detailsLink = R.string.errors_generic_text_catastrophic_error_encryption_failure,
                onDismiss = {
                    vm.errorResetDialogDismissed()
                }
            )
        }
    }

    override fun onResume() {
        super.onResume()
        // refresh required data
        tracingViewModel.refreshRiskLevel()
        tracingViewModel.refreshExposureSummary()
        tracingViewModel.refreshLastTimeDiagnosisKeysFetchedDate()
        tracingViewModel.refreshIsTracingEnabled()
        tracingViewModel.refreshActiveTracingDaysInRetentionPeriod()
        TimerHelper.checkManualKeyRetrievalTimer()
        submissionViewModel.refreshDeviceUIState()
        tracingViewModel.refreshLastSuccessfullyCalculatedScore()
        binding.mainScrollview.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }

    private fun toSubmissionResult() {
        findNavController().doNavigate(
            HomeFragmentDirections.actionMainFragmentToSubmissionResultFragment()
        )
    }

    private fun toSubmissionIntro() {
        findNavController().doNavigate(
            HomeFragmentDirections.actionMainFragmentToSubmissionIntroFragment()
        )
    }

    private fun showPopup(view: View) = PopupMenu(requireContext(), view).apply {
        inflate(R.menu.menu_main)
        menu.findItem(R.id.menu_test).isVisible = CWADebug.isDeviceForTestersBuild
        setOnMenuItemClickListener {
            return@setOnMenuItemClickListener when (it.itemId) {
                R.id.menu_help -> {
                    findNavController().doNavigate(HomeFragmentDirections.actionMainFragmentToMainOverviewFragment())
                    true
                }
                R.id.menu_information -> {
                    findNavController().doNavigate(HomeFragmentDirections.actionMainFragmentToInformationFragment())
                    true
                }
                R.id.menu_settings -> {
                    findNavController().doNavigate(HomeFragmentDirections.actionMainFragmentToSettingsFragment())
                    true
                }
                R.id.menu_test -> {
                    findNavController().doNavigate(HomeFragmentDirections.actionMainFragmentToTestNavGraph())
                    true
                }
                else -> super.onOptionsItemSelected(it)
            }
        }
    }.show()
}
