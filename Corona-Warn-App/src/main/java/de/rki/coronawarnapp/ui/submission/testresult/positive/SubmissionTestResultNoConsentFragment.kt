package de.rki.coronawarnapp.ui.submission.testresult.positive

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSubmissionTestResultPositiveNoConsentBinding
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

/**
 * [SubmissionTestResultNoConsentFragment], the test result screen that is shown to the user if they have not provided
 * consent
 */
class SubmissionTestResultNoConsentFragment : Fragment(R.layout.fragment_submission_test_result_positive_no_consent),
    AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: SubmissionTestResultNoConsentViewModel by cwaViewModels { viewModelFactory }
    private val binding: FragmentSubmissionTestResultPositiveNoConsentBinding by viewBindingLazy()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.onTestOpened()

        val backCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showCancelDialog()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backCallback)

        viewModel.uiState.observe2(this) {
            binding.submissionTestResultSection
                .setTestResultSection(it.deviceUiState, it.testResultReceivedDate)
        }

        binding.apply {
            submissionTestResultConsentGivenHeader.headerButtonBack.buttonIcon.setOnClickListener {
                showCancelDialog()
            }
            submissionTestResultPositiveNoConsentButtonAbort.setOnClickListener {
                showCancelDialog()
            }
            submissionTestResultPositiveNoConsentButtonWarnOthers.setOnClickListener {
                navigateToWarnOthers()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.submissionTestResultContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }

    private fun showCancelDialog() {
        AlertDialog.Builder(requireContext()).apply {
            setTitle(R.string.submission_test_result_positive_no_consent_dialog_title)
            setMessage(R.string.submission_test_result_positive_no_consent_dialog_message)
            setNegativeButton(R.string.submission_test_result_positive_no_consent_dialog_positive_button) { _, _ ->
            }
            setPositiveButton(R.string.submission_test_result_positive_no_consent_dialog_negative_button) { _, _ ->
                navigateToHome()
            }
        }.show()
    }

    private fun navigateToHome() {
        doNavigate(
            SubmissionTestResultNoConsentFragmentDirections.actionSubmissionTestResultNoConsentFragmentToHomeFragment()
        )
    }

    private fun navigateToWarnOthers() {
        doNavigate(
            SubmissionTestResultNoConsentFragmentDirections
                .actionSubmissionTestResultNoConsentFragmentToSubmissionResultPositiveOtherWarningNoConsentFragment()
        )
    }
}
