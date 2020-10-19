package de.rki.coronawarnapp.ui.submission.fragment

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.nearby.exposurenotification.TemporaryExposureKey
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSubmissionPositiveOtherWarningBinding
import de.rki.coronawarnapp.exception.http.BadRequestException
import de.rki.coronawarnapp.exception.http.CwaClientError
import de.rki.coronawarnapp.exception.http.CwaServerError
import de.rki.coronawarnapp.exception.http.CwaWebException
import de.rki.coronawarnapp.exception.http.ForbiddenException
import de.rki.coronawarnapp.nearby.InternalExposureNotificationPermissionHelper
import de.rki.coronawarnapp.ui.submission.ApiRequestState
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionNavigationEvents
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionResultPositiveOtherWarningViewModel
import de.rki.coronawarnapp.ui.viewmodel.SubmissionViewModel
import de.rki.coronawarnapp.util.DialogHelper
import de.rki.coronawarnapp.util.di.AppInjector
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.observeEvent
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SubmissionResultPositiveOtherWarningFragment :
    Fragment(R.layout.fragment_submission_positive_other_warning),
    InternalExposureNotificationPermissionHelper.Callback, AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: SubmissionResultPositiveOtherWarningViewModel by cwaViewModels { viewModelFactory }
    private val submissionViewModel: SubmissionViewModel by activityViewModels()

    private val binding: FragmentSubmissionPositiveOtherWarningBinding by viewBindingLazy()
    private lateinit var internalExposureNotificationPermissionHelper:
        InternalExposureNotificationPermissionHelper

    override fun onResume() {
        super.onResume()
        binding.submissionPositiveOtherPrivacyContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }

    private fun buildErrorDialog(exception: CwaWebException): DialogHelper.DialogInstance {
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
            is CwaServerError, is CwaClientError -> DialogHelper.DialogInstance(
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

        setButtonOnClickListener()

        submissionViewModel.submissionError.observeEvent(viewLifecycleOwner) {
            DialogHelper.showDialog(buildErrorDialog(it))
        }

        submissionViewModel.submissionState.observe2(this) {
            if (it == ApiRequestState.SUCCESS) {
                viewModel.onSubmissionComplete()
            }
        }
    }

    private fun setButtonOnClickListener() {
        binding.submissionPositiveOtherWarningButtonNext.setOnClickListener {
            initiateWarningOthers()
            viewModel.onWarnOthersPressed()
        }
        binding.submissionPositiveOtherWarningHeader.headerButtonBack.buttonIcon.setOnClickListener {
            findNavController().popBackStack()
            viewModel.onBackPressed()
        }

        viewModel.routeToScreen.observe2(this) {
            when (it) {
                is SubmissionNavigationEvents.NavigateToSubmissionIntro ->
                    initiateWarningOthers()
                is SubmissionNavigationEvents.NavigateToSubmissionDone ->
                    navigateToSubmissionDoneFragment()
                is SubmissionNavigationEvents.NavigateToTestResult ->
                    findNavController().popBackStack()
            }
        }
    }

    private fun navigateToSubmissionResultFragment() =
        doNavigate(
            SubmissionResultPositiveOtherWarningFragmentDirections
                .actionSubmissionResultPositiveOtherWarningFragmentToSubmissionResultFragment()
        )

    /**
     * Navigate to submission done Fragment
     * @see SubmissionDoneFragment
     */
    private fun navigateToSubmissionDoneFragment() =
        doNavigate(
            SubmissionResultPositiveOtherWarningFragmentDirections
                .actionSubmissionResultPositiveOtherWarningFragmentToSubmissionDoneFragment()
        )

    private fun initiateWarningOthers() {
        // TODO remove after VM Injection, workaround, should not happen in the fragment
        submissionViewModel.launch {
            val isTracingEnabled = AppInjector.component.enfClient.isTracingEnabled.first()
            withContext(Dispatchers.Main) {
                if (!isTracingEnabled) {
                    val tracingRequiredDialog = DialogHelper.DialogInstance(
                        requireActivity(),
                        R.string.submission_test_result_dialog_tracing_required_title,
                        R.string.submission_test_result_dialog_tracing_required_message,
                        R.string.submission_test_result_dialog_tracing_required_button
                    )
                    DialogHelper.showDialog(tracingRequiredDialog)
                } else {
                    internalExposureNotificationPermissionHelper.requestPermissionToShareKeys()
                }
            }
        }
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
            viewModel.onSubmissionComplete()
        }
    }

    override fun onFailure(exception: Exception?) {
        // NOOP
    }
}
