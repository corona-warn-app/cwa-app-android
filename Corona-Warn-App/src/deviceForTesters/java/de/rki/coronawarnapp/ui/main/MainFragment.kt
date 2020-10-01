package de.rki.coronawarnapp.ui.main

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentMainBinding
import de.rki.coronawarnapp.risk.TimeVariables
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.timer.TimerHelper
import de.rki.coronawarnapp.ui.doNavigate
import de.rki.coronawarnapp.ui.viewmodel.SettingsViewModel
import de.rki.coronawarnapp.ui.viewmodel.SubmissionViewModel
import de.rki.coronawarnapp.ui.viewmodel.TracingViewModel
import de.rki.coronawarnapp.util.DialogHelper
import de.rki.coronawarnapp.util.ExternalActionHelper
import de.rki.coronawarnapp.util.di.AppInjector
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.errors.RecoveryByResetDialogFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
class MainFragment : Fragment(R.layout.fragment_main) {

    private val tracingViewModel: TracingViewModel by activityViewModels()
    private val settingsViewModel: SettingsViewModel by activityViewModels()
    private val submissionViewModel: SubmissionViewModel by activityViewModels()
    private val binding: FragmentMainBinding by viewBindingLazy()

    private val errorResetTool by lazy {
        AppInjector.component.errorResetTool
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tracingViewModel = tracingViewModel
        binding.settingsViewModel = settingsViewModel
        binding.submissionViewModel = submissionViewModel

        setButtonOnClickListener()
        setContentDescription()
        checkShouldInteroperabilityBeOpened()
        showOneTimeTracingExplanationDialog()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if (errorResetTool.isResetNoticeToBeShown) {
            RecoveryByResetDialogFactory(this).showDialog(
                detailsLink = R.string.errors_generic_text_catastrophic_error_encryption_failure,
                onDismiss = {
                    errorResetTool.isResetNoticeToBeShown = false
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

    private fun setContentDescription() {
        binding.mainHeaderShare.buttonIcon.contentDescription = getString(R.string.button_share)
        binding.mainHeaderOptionsMenu.buttonIcon.contentDescription = getString(R.string.button_menu)
        binding.mainAbout.mainCard.contentDescription = getString(R.string.hint_external_webpage)
    }

    private fun setButtonOnClickListener() {
        binding.mainTestUnregistered.submissionStatusCardUnregistered.setOnClickListener {
            toSubmissionIntro()
        }
        binding.mainTestUnregistered.submissionStatusCardUnregisteredButton.setOnClickListener {
            toSubmissionIntro()
        }
        binding.mainTestDone.submissionStatusCardDone.setOnClickListener {
            findNavController().doNavigate(
                MainFragmentDirections.actionMainFragmentToSubmissionDoneFragment()
            )
        }
        binding.mainTestResult.submissionStatusCardContent.setOnClickListener {
            toSubmissionResult()
        }
        binding.mainTestResult.submissionStatusCardContentButton.setOnClickListener {
            toSubmissionResult()
        }
        binding.mainTestPositive.submissionStatusCardPositive.setOnClickListener {
            toSubmissionResult()
        }
        binding.mainTestPositive.submissionStatusCardPositiveButton.setOnClickListener {
            toSubmissionResult()
        }
        binding.mainTracing.setOnClickListener {
            findNavController().doNavigate(MainFragmentDirections.actionMainFragmentToSettingsTracingFragment())
        }
        binding.mainRisk.riskCard.setOnClickListener {
            findNavController().doNavigate(MainFragmentDirections.actionMainFragmentToRiskDetailsFragment())
        }
        binding.mainRisk.riskCardButtonUpdate.setOnClickListener {
            tracingViewModel.refreshDiagnosisKeys()
            settingsViewModel.updateManualKeyRetrievalEnabled(false)
        }
        binding.mainRisk.riskCardButtonEnableTracing.setOnClickListener {
            findNavController().doNavigate(MainFragmentDirections.actionMainFragmentToSettingsTracingFragment())
        }
        binding.mainAbout.mainCard.setOnClickListener {
            ExternalActionHelper.openUrl(this, requireContext().getString(R.string.main_about_link))
        }
        binding.mainHeaderShare.buttonIcon.setOnClickListener {
            findNavController().doNavigate(MainFragmentDirections.actionMainFragmentToMainSharingFragment())
        }
        binding.mainHeaderOptionsMenu.buttonIcon.setOnClickListener {
            showPopup(it)
        }
    }

    private fun toSubmissionResult() {
        findNavController().doNavigate(
            MainFragmentDirections.actionMainFragmentToSubmissionResultFragment()
        )
    }

    private fun toSubmissionIntro() {
        findNavController().doNavigate(
            MainFragmentDirections.actionMainFragmentToSubmissionIntroFragment()
        )
    }

    private fun showPopup(view: View) {
        val popup = PopupMenu(requireContext(), view)
        popup.inflate(R.menu.menu_main)
        // TODO we shouldn't have to duplicate the whole fragment to add these items
        // In the future we'd like a DI'ed class that changes the navigation for this MainFragment?
        popup.setOnMenuItemClickListener {
            return@setOnMenuItemClickListener when (it.itemId) {
                R.id.menu_help -> {
                    findNavController().doNavigate(MainFragmentDirections.actionMainFragmentToMainOverviewFragment())
                    true
                }
                R.id.menu_information -> {
                    findNavController().doNavigate(MainFragmentDirections.actionMainFragmentToInformationFragment())
                    true
                }
                R.id.menu_settings -> {
                    findNavController().doNavigate(MainFragmentDirections.actionMainFragmentToSettingsFragment())
                    true
                }
                R.id.menu_test_api -> {
                    findNavController().doNavigate(MainFragmentDirections.actionMainFragmentToTestForAPIFragment())
                    true
                }
                R.id.menu_test_risk_level -> {
                    findNavController().doNavigate(
                        MainFragmentDirections.actionMainFragmentToTestRiskLevelCalculation(
                            exampleArgument = null
                        )
                    )
                    true
                }
                else -> super.onOptionsItemSelected(it)
            }
        }
        popup.show()
    }

    private fun checkShouldInteroperabilityBeOpened() {
        if (!LocalData.isInteroperabilityShownAtLeastOnce) {
            navigateToInteroperabilityFeature()
        }
    }

    private fun navigateToInteroperabilityFeature() {
        findNavController().doNavigate(
            MainFragmentDirections.actionMainFragmentToOnboardingDeltaInteroperabilityFragment()
        )
    }

    private fun showOneTimeTracingExplanationDialog() {

        // check if the dialog explaining the tracing time was already shown
        if (!LocalData.tracingExplanationDialogWasShown()) {

            val activity = this.requireActivity()

            lifecycleScope.launch {

                // get all text strings and the current active tracing time
                val infoPeriodLogged =
                    getString(R.string.risk_details_information_body_period_logged)
                val infoPeriodLoggedAssessment =
                    getString(
                        R.string.risk_details_information_body_period_logged_assessment,
                        (TimeVariables.getActiveTracingDaysInRetentionPeriod()).toString()
                    )
                val infoFAQ = getString(R.string.risk_details_explanation_dialog_faq_body)

                withContext(Dispatchers.Main) {

                    // display the dialog
                    DialogHelper.showDialog(
                        DialogHelper.DialogInstance(
                            activity,
                            getString(R.string.risk_details_explanation_dialog_title),
                            "$infoPeriodLogged\n\n$infoPeriodLoggedAssessment\n\n$infoFAQ",
                            getString(R.string.errors_generic_button_positive),
                            null,
                            null,
                            {
                                LocalData.tracingExplanationDialogWasShown(true)
                            },
                            {}
                        ))
                }
            }
        }
    }
}
