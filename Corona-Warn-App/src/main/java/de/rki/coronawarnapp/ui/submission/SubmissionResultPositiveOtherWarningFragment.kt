package de.rki.coronawarnapp.ui.submission

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.google.android.gms.nearby.exposurenotification.TemporaryExposureKey
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSubmissionPositiveOtherWarningBinding
import de.rki.coronawarnapp.exception.http.BadRequestException
import de.rki.coronawarnapp.exception.http.CwaClientError
import de.rki.coronawarnapp.exception.http.CwaServerError
import de.rki.coronawarnapp.exception.http.ForbiddenException
import de.rki.coronawarnapp.nearby.InternalExposureNotificationPermissionHelper
import de.rki.coronawarnapp.ui.BaseFragment
import de.rki.coronawarnapp.ui.viewmodel.SubmissionViewModel
import de.rki.coronawarnapp.ui.viewmodel.TracingViewModel
import de.rki.coronawarnapp.util.DialogHelper
import de.rki.coronawarnapp.util.observeEvent

class SubmissionResultPositiveOtherWarningFragment : BaseFragment(),
    InternalExposureNotificationPermissionHelper.Callback {

    companion object {
        private val TAG: String? = SubmissionResultPositiveOtherWarningFragment::class.simpleName
    }

    private val submissionViewModel: SubmissionViewModel by activityViewModels()
    private val tracingViewModel: TracingViewModel by activityViewModels()

    private var _binding: FragmentSubmissionPositiveOtherWarningBinding? = null
    private val binding: FragmentSubmissionPositiveOtherWarningBinding get() = _binding!!
    private var submissionRequested = false
    private var submissionFailed = false
    private lateinit var internalExposureNotificationPermissionHelper:
            InternalExposureNotificationPermissionHelper

    override fun onResume() {
        super.onResume()
        tracingViewModel.refreshIsTracingEnabled()
        if (submissionRequested && !submissionFailed) {
            internalExposureNotificationPermissionHelper.requestPermissionToShareKeys()
        }
    }

    override fun onKeySharePermissionGranted(keys: List<TemporaryExposureKey>) {
        super.onKeySharePermissionGranted(keys)
        submissionViewModel.submitDiagnosisKeys()
    }

    override fun onFailure(exception: Exception?) {
        submissionFailed = true
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        internalExposureNotificationPermissionHelper =
            InternalExposureNotificationPermissionHelper(this, this)
        _binding = FragmentSubmissionPositiveOtherWarningBinding.inflate(inflater)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
        setButtonOnClickListener()

        submissionViewModel.submissionError.observeEvent(viewLifecycleOwner, {
            DialogHelper.showDialog(buildErrorDialog(it))
        })

        submissionViewModel.submissionState.observeEvent(viewLifecycleOwner, {
            if (it == ApiRequestState.SUCCESS) {
                doNavigate(
                    SubmissionResultPositiveOtherWarningFragmentDirections
                        .actionSubmissionResultPositiveOtherWarningFragmentToSubmissionDoneFragment()
                )
            }
        })
    }

    private fun setButtonOnClickListener() {
        binding.submissionPositiveOtherWarningButtonNext.setOnClickListener {
            initiateWarningOthers()
        }
        binding.submissionPositiveOtherWarningHeader.headerToolbar.setNavigationOnClickListener {
            navigateToSubmissionResultFragment()
        }
    }

    private fun navigateToSubmissionResultFragment() =
        doNavigate(
            SubmissionResultPositiveOtherWarningFragmentDirections
                .actionSubmissionResultPositiveOtherWarningFragmentToSubmissionResultFragment()
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

        submissionRequested = true
        internalExposureNotificationPermissionHelper.requestPermissionToShareKeys()
    }
}
