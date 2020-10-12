package de.rki.coronawarnapp.ui.submission.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSubmissionTestResultBinding
import de.rki.coronawarnapp.exception.http.CwaClientError
import de.rki.coronawarnapp.exception.http.CwaServerError
import de.rki.coronawarnapp.exception.http.CwaWebException
import de.rki.coronawarnapp.ui.doNavigate
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionTestResultViewModel
import de.rki.coronawarnapp.ui.viewmodel.SubmissionViewModel
import de.rki.coronawarnapp.ui.viewmodel.TracingViewModel
import de.rki.coronawarnapp.util.DeviceUIState
import de.rki.coronawarnapp.util.DialogHelper
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.observeEvent
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

/**
 * A simple [Fragment] subclass.
 */
class SubmissionTestResultFragment : Fragment(R.layout.fragment_submission_test_result), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: SubmissionTestResultViewModel by cwaViewModels { viewModelFactory }
    private val submissionViewModel: SubmissionViewModel by activityViewModels()
    private val tracingViewModel: TracingViewModel by activityViewModels()

    private val binding: FragmentSubmissionTestResultBinding by viewBindingLazy()

    private var skipInitialTestResultRefresh = false

    // Overrides default back behaviour
    private val backCallback: OnBackPressedCallback =
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                viewModel.onBackPressed()
            }
        }

    private fun navigateToMainScreen() =
        findNavController().doNavigate(
            SubmissionTestResultFragmentDirections.actionSubmissionResultFragmentToMainFragment()
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
        binding.submissionViewModel = submissionViewModel
        // registers callback when the os level back is pressed
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backCallback)

        skipInitialTestResultRefresh =
            arguments?.getBoolean("skipInitialTestResultRefresh") ?: false

        setButtonOnClickListener()

        submissionViewModel.uiStateError.observeEvent(viewLifecycleOwner) {
            DialogHelper.showDialog(buildErrorDialog(it))
        }

        submissionViewModel.deviceUiState.observe(viewLifecycleOwner, { uiState ->
            if (uiState == DeviceUIState.PAIRED_REDEEMED) {
                showRedeemedTokenWarningDialog()
            }
        })

        viewModel.navigateWithSymptoms.observe2(this) {
            findNavController().doNavigate(
                SubmissionTestResultFragmentDirections
                    .actionSubmissionResultFragmentToSubmissionSymptomIntroductionFragment()
            )
        }

        viewModel.navigateWithoutSymptoms.observe2(this) {
            findNavController().doNavigate(
                SubmissionTestResultFragmentDirections
                    .actionSubmissionResultFragmentToSubmissionResultPositiveOtherWarningFragment()
            )
        }

        viewModel.navigateTestRemoved.observe2(this) {
            findNavController().doNavigate(
                SubmissionTestResultFragmentDirections.actionSubmissionResultFragmentToMainFragment()
            )
        }

        viewModel.navigateBack.observe2(this) {
            findNavController().doNavigate(
                SubmissionTestResultFragmentDirections.actionSubmissionResultFragmentToMainFragment()
            )
        }
    }

    private fun showRedeemedTokenWarningDialog() {
        val dialog = DialogHelper.DialogInstance(
            requireActivity(),
            R.string.submission_error_dialog_web_tan_redeemed_title,
            R.string.submission_error_dialog_web_tan_redeemed_body,
            R.string.submission_error_dialog_web_tan_redeemed_button_positive
        )

        DialogHelper.showDialog(dialog)
    }

    override fun onResume() {
        super.onResume()
        binding.submissionTestResultContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
        submissionViewModel.refreshDeviceUIState(refreshTestResult = !skipInitialTestResultRefresh)
        tracingViewModel.refreshIsTracingEnabled()

        skipInitialTestResultRefresh = false
    }

    private fun setButtonOnClickListener() {
        binding.submissionTestResultButtonPendingRefresh.setOnClickListener {
            submissionViewModel.refreshDeviceUIState()
            binding.submissionTestResultContent.submissionTestResultCard.testResultCard
                .sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
        }

        binding.submissionTestResultButtonPendingRemoveTest.setOnClickListener {
            removeTestAfterConfirmation()
        }

        binding.submissionTestResultButtonNegativeRemoveTest.setOnClickListener {
            removeTestAfterConfirmation()
        }

        binding.submissionTestResultButtonPositiveContinue.setOnClickListener {
            continueIfTracingEnabled(false)
        }

        binding.submissionTestResultButtonPositiveContinueWithoutSymptoms.setOnClickListener {
            submissionViewModel.onNoInformationSymptomIndication()
            continueIfTracingEnabled(true)
        }

        binding.submissionTestResultButtonInvalidRemoveTest.setOnClickListener {
            removeTestAfterConfirmation()
        }

        binding.submissionTestResultHeader.headerButtonBack.buttonIcon.setOnClickListener {
            viewModel.onBackPressed()
        }
    }

    private fun continueIfTracingEnabled(skipSymptomSubmission: Boolean) {
        if (tracingViewModel.isTracingEnabled.value != true) {
            val tracingRequiredDialog = DialogHelper.DialogInstance(
                requireActivity(),
                R.string.submission_test_result_dialog_tracing_required_title,
                R.string.submission_test_result_dialog_tracing_required_message,
                R.string.submission_test_result_dialog_tracing_required_button
            )
            DialogHelper.showDialog(tracingRequiredDialog)
            return
        }

        if (skipSymptomSubmission) {
            viewModel.onContinueNoSymptomsPressed()
        } else {
            viewModel.onContinuePressed()
        }
    }

    private fun removeTestAfterConfirmation() {
        val removeTestDialog = DialogHelper.DialogInstance(
            requireActivity(),
            R.string.submission_test_result_dialog_remove_test_title,
            R.string.submission_test_result_dialog_remove_test_message,
            R.string.submission_test_result_dialog_remove_test_button_positive,
            R.string.submission_test_result_dialog_remove_test_button_negative,
            positiveButtonFunction = {
                submissionViewModel.deregisterTestFromDevice()
                viewModel.onNavigateTestRemoved()
            }
        )
        DialogHelper.showDialog(removeTestDialog).apply {
            getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(context.getColor(R.color.colorTextSemanticRed))
        }
    }
}
