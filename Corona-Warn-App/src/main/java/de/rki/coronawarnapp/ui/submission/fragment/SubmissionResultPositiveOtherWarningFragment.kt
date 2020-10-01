package de.rki.coronawarnapp.ui.submission.fragment

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.nearby.exposurenotification.TemporaryExposureKey
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSubmissionPositiveOtherWarningBinding
import de.rki.coronawarnapp.exception.http.BadRequestException
import de.rki.coronawarnapp.exception.http.CwaClientError
import de.rki.coronawarnapp.exception.http.CwaServerError
import de.rki.coronawarnapp.exception.http.ForbiddenException
import de.rki.coronawarnapp.nearby.InternalExposureNotificationPermissionHelper
import de.rki.coronawarnapp.ui.doNavigate
import de.rki.coronawarnapp.ui.submission.ApiRequestState
import de.rki.coronawarnapp.ui.viewmodel.SubmissionViewModel
import de.rki.coronawarnapp.ui.viewmodel.TracingViewModel
import de.rki.coronawarnapp.util.DialogHelper
import de.rki.coronawarnapp.util.observeEvent
import de.rki.coronawarnapp.util.ui.viewBindingLazy

class SubmissionResultPositiveOtherWarningFragment :
    Fragment(R.layout.fragment_submission_positive_other_warning),
    InternalExposureNotificationPermissionHelper.Callback {

    private val submissionViewModel: SubmissionViewModel by activityViewModels()
    private val tracingViewModel: TracingViewModel by activityViewModels()

    private val binding: FragmentSubmissionPositiveOtherWarningBinding by viewBindingLazy()
    private lateinit var internalExposureNotificationPermissionHelper:
            InternalExposureNotificationPermissionHelper

    // Overrides default back behaviour
    private val backCallback: OnBackPressedCallback =
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                handleSubmissionCancellation()
            }
        }

    override fun onResume() {
        super.onResume()
        binding.submissionPositiveOtherPrivacyContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
        tracingViewModel.refreshIsTracingEnabled()
    }

    private fun buildErrorDialog(exception: Exception): DialogHelper.DialogInstance {
        return when (exception) {
            is BadRequestException -> DialogHelper.DialogInstance(
                requireActivity(),
                R.string.submission_error_dialog_web_paring_invalid_title,
                R.string.submission_error_dialog_web_paring_invalid_body,
                R.string.submission_error_dialog_web_paring_invalid_button_positive,
                null,
                true,
                ::navigateToSubmissionResultFragment
            )
            is ForbiddenException -> DialogHelper.DialogInstance(
                requireActivity(),
                R.string.submission_error_dialog_web_tan_invalid_title,
                R.string.submission_error_dialog_web_tan_invalid_body,
                R.string.submission_error_dialog_web_tan_invalid_button_positive,
                null,
                true,
                ::navigateToSubmissionResultFragment
            )
            is CwaServerError -> DialogHelper.DialogInstance(
                requireActivity(),
                R.string.submission_error_dialog_web_generic_error_title,
                getString(
                    R.string.submission_error_dialog_web_generic_network_error_body,
                    exception.statusCode
                ),
                R.string.submission_error_dialog_web_generic_error_button_positive,
                null,
                true,
                ::navigateToSubmissionResultFragment
            )
            is CwaClientError -> DialogHelper.DialogInstance(
                requireActivity(),
                R.string.submission_error_dialog_web_generic_error_title,
                getString(
                    R.string.submission_error_dialog_web_generic_network_error_body,
                    exception.statusCode
                ),
                R.string.submission_error_dialog_web_generic_error_button_positive,
                null,
                true,
                ::navigateToSubmissionResultFragment
            )
            else -> DialogHelper.DialogInstance(
                requireActivity(),
                R.string.submission_error_dialog_web_generic_error_title,
                R.string.submission_error_dialog_web_generic_error_body,
                R.string.submission_error_dialog_web_generic_error_button_positive,
                null,
                true,
                ::navigateToSubmissionResultFragment
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.submissionViewModel = submissionViewModel

        internalExposureNotificationPermissionHelper =
            InternalExposureNotificationPermissionHelper(this, this)
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backCallback)

        setButtonOnClickListener()

        submissionViewModel.submissionError.observeEvent(viewLifecycleOwner) {
            DialogHelper.showDialog(buildErrorDialog(it))
        }

        submissionViewModel.submissionState.observe(viewLifecycleOwner, {
            if (it == ApiRequestState.SUCCESS) {
                navigateToSubmissionDoneFragment()
            }
        })
    }

    private fun setButtonOnClickListener() {
        binding.submissionPositiveOtherWarningButtonNext.setOnClickListener {
            initiateWarningOthers()
        }
        binding.submissionPositiveOtherWarningHeader.headerButtonBack.buttonIcon.setOnClickListener {
            handleSubmissionCancellation()
        }
    }

    /**
     * Opens a Dialog that warns user
     * when they're about to cancel the submission flow
     * @see DialogHelper
     * @see navigateToSubmissionResultFragment
     */
    fun handleSubmissionCancellation() {
        DialogHelper.showDialog(
            DialogHelper.DialogInstance(
                requireActivity(),
                R.string.submission_error_dialog_confirm_cancellation_title,
                R.string.submission_error_dialog_confirm_cancellation_body,
                R.string.submission_error_dialog_confirm_cancellation_button_positive,
                R.string.submission_error_dialog_confirm_cancellation_button_negative,
                true,
                ::navigateToSubmissionResultFragment
            )
        )
    }

    private fun navigateToSubmissionResultFragment() =
        findNavController().doNavigate(
            SubmissionResultPositiveOtherWarningFragmentDirections
                .actionSubmissionResultPositiveOtherWarningFragmentToSubmissionResultFragment()
        )

    /**
     * Navigate to submission done Fragment
     * @see SubmissionDoneFragment
     */
    private fun navigateToSubmissionDoneFragment() =
        findNavController().doNavigate(
            SubmissionResultPositiveOtherWarningFragmentDirections
                .actionSubmissionResultPositiveOtherWarningFragmentToSubmissionDoneFragment()
        )

    private fun initiateWarningOthers() {
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
        internalExposureNotificationPermissionHelper.requestPermissionToShareKeys()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        this.internalExposureNotificationPermissionHelper.onResolutionComplete(
            requestCode,
            resultCode
        )
    }

    // InternalExposureNotificationPermissionHelper - callbacks
    override fun onKeySharePermissionGranted(keys: List<TemporaryExposureKey>) {
        super.onKeySharePermissionGranted(keys)
        if (keys.isNotEmpty()) {
            submissionViewModel.submitDiagnosisKeys(keys)
        } else {
            submissionViewModel.submitWithNoDiagnosisKeys()
            navigateToSubmissionDoneFragment()
        }
    }

    override fun onFailure(exception: Exception?) {
        // NOOP
    }
}
