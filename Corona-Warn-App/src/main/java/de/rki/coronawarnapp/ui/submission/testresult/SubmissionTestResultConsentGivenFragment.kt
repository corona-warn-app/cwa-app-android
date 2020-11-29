package de.rki.coronawarnapp.ui.submission.testresult

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSubmissionTestResultConsentGivenBinding
import de.rki.coronawarnapp.exception.http.CwaClientError
import de.rki.coronawarnapp.exception.http.CwaServerError
import de.rki.coronawarnapp.exception.http.CwaWebException
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionNavigationEvents
import de.rki.coronawarnapp.util.DialogHelper
import de.rki.coronawarnapp.util.NetworkRequestWrapper.Companion.withFailure
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

class SubmissionTestResultConsentGivenFragment : Fragment(R.layout.fragment_submission_test_result_consent_given),
    AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: SubmissionTestResultConsentGivenViewModel by cwaViewModels { viewModelFactory }

    private val binding: FragmentSubmissionTestResultConsentGivenBinding by viewBindingLazy()

    private fun navigateToMainScreen() =
        doNavigate(
            SubmissionTestResultConsentGivenFragmentDirections.actionSubmissionTestResultConsentGivenFragmentToHomeFragment()
        )

    private fun buildErrorDialog(exception: CwaWebException): DialogHelper.DialogInstance {
        return when (exception) {
            is CwaClientError, is CwaServerError -> DialogHelper.DialogInstance(
                requireActivity(),
                R.string.submission_error_dialog_web_generic_error_title,
                getString(
                    R.string.submission_error_dialog_web_generic_network_error_body,
                    exception.statusCode
                ),
                R.string.submission_error_dialog_web_generic_error_button_positive,
                null,
                true,
                ::navigateToMainScreen
            )
            else -> DialogHelper.DialogInstance(
                requireActivity(),
                R.string.submission_error_dialog_web_generic_error_title,
                R.string.submission_error_dialog_web_generic_error_body,
                R.string.submission_error_dialog_web_generic_error_button_positive,
                null,
                true,
                ::navigateToMainScreen
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.uiState.observe2(this) {
            binding.uiState = it
            with(binding) {
                submissionTestResultSection
                    .setTestResultSection(uiState?.deviceUiState, uiState?.testResultReceivedDate)
            }
            it.deviceUiState.withFailure {
                if (it is CwaWebException) {
                    DialogHelper.showDialog(buildErrorDialog(it))
                }
            }
        }

        setButtonOnClickListener()

        viewModel.showCancelDialog.observe2(this) {
            showCancelDialog()
        }

        viewModel.routeToScreen.observe2(this) {
            when (it) {
                is SubmissionNavigationEvents.NavigateToSymptomIntroduction ->
                    doNavigate(
                        SubmissionTestResultConsentGivenFragmentDirections
                            .actionSubmissionTestResultConsentGivenFragmentToSubmissionSymptomIntroductionFragment()
                    )
                is SubmissionNavigationEvents.NavigateToMainActivity ->
                    doNavigate(
                        SubmissionTestResultConsentGivenFragmentDirections
                            .actionSubmissionTestResultConsentGivenFragmentToHomeFragment()
                    )
            }

        }
    }

    override fun onResume() {
        super.onResume()
        binding.submissionTestResultContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }

    private fun setButtonOnClickListener() {
        binding.submissionTestResultButtonConsentGivenContinue.setOnClickListener {
            viewModel.onContinuePressed()

        }

        binding.submissionTestResultButtonConsentGivenContinueWithoutSymptoms.setOnClickListener {
            viewModel.onShowCancelDialog()
        }
    }

    private fun showCancelDialog() {
        AlertDialog.Builder(requireContext()).apply {
            setTitle(R.string.submission_error_dialog_confirm_cancellation_title)
            setMessage(R.string.submission_error_dialog_confirm_cancellation_body)
            setPositiveButton(R.string.submission_error_dialog_confirm_cancellation_button_positive) { _, _ ->
                viewModel.cancelTestSubmission()
            }
            setNegativeButton(R.string.submission_error_dialog_confirm_cancellation_button_negative) { _, _ ->
                // NOOP
            }
        }.show()
    }
}
