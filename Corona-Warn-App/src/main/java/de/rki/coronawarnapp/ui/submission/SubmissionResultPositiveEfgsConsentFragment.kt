package de.rki.coronawarnapp.ui.submission

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.gms.nearby.exposurenotification.TemporaryExposureKey
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSubmissionPositiveEfgsConsentBinding
import de.rki.coronawarnapp.exception.http.BadRequestException
import de.rki.coronawarnapp.exception.http.CwaClientError
import de.rki.coronawarnapp.exception.http.CwaServerError
import de.rki.coronawarnapp.exception.http.ForbiddenException
import de.rki.coronawarnapp.nearby.InternalExposureNotificationPermissionHelper
import de.rki.coronawarnapp.ui.doNavigate
import de.rki.coronawarnapp.ui.viewmodel.EfgsConsentViewModel
import de.rki.coronawarnapp.ui.viewmodel.SubmissionViewModel
import de.rki.coronawarnapp.ui.viewmodel.TracingViewModel
import de.rki.coronawarnapp.util.DialogHelper
import de.rki.coronawarnapp.util.IGNORE_CHANGE_TAG
import de.rki.coronawarnapp.util.observeEvent

class SubmissionResultPositiveEfgsConsentFragment : Fragment(),
    InternalExposureNotificationPermissionHelper.Callback {

    companion object {
        private val TAG: String? = SubmissionResultPositiveEfgsConsentFragment::class.simpleName
    }

    private val efgsViewModel: EfgsConsentViewModel by activityViewModels()
    private val submissionViewModel: SubmissionViewModel by activityViewModels()
    private val tracingViewModel: TracingViewModel by activityViewModels()

    private var _binding: FragmentSubmissionPositiveEfgsConsentBinding? = null
    private val binding: FragmentSubmissionPositiveEfgsConsentBinding get() = _binding!!
    private lateinit var internalExposureNotificationPermissionHelper:
            InternalExposureNotificationPermissionHelper

    override fun onResume() {
        super.onResume()
        binding.submissionPositiveEfgsConsentPrivacyContainer.sendAccessibilityEvent(
            AccessibilityEvent.TYPE_ANNOUNCEMENT
        )
        tracingViewModel.refreshIsTracingEnabled()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        internalExposureNotificationPermissionHelper =
            InternalExposureNotificationPermissionHelper(this, this)
        _binding = FragmentSubmissionPositiveEfgsConsentBinding.inflate(inflater)
        binding.submissionViewModel = submissionViewModel
        binding.efgsViewModel = efgsViewModel
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

        submissionViewModel.submissionError.observeEvent(viewLifecycleOwner) {
            DialogHelper.showDialog(buildErrorDialog(it))
        }

        submissionViewModel.submissionState.observe(viewLifecycleOwner, Observer {
            if (it == ApiRequestState.SUCCESS) {
                navigateToSubmissionDoneFragment()
            }
        })
    }

    private fun setButtonOnClickListener() {
        val switch = binding.settingsEfgsConsentRow.settingsSwitchRowSwitch
        binding.submissionPositiveEfgsConsentHeader.headerButtonBack.buttonIcon.setOnClickListener {
            navigateToSubmissionResultPositiveOtherWarningFragment()
        }
        switch.setOnCheckedChangeListener { _, isEnabled ->
            if (switch.tag != IGNORE_CHANGE_TAG) {
                efgsViewModel.isEfgsConsentGranted.postValue(isEnabled)
            }
        }
        binding.submissionPositiveEfgsConsentButtonNext.setOnClickListener {
            if (efgsViewModel.isEfgsConsentGranted.value!!) {
                navigateToTargetGermanyFragment()
            } else {
                initiateWarningOthers()
            }
        }
    }

    private fun navigateToSubmissionResultPositiveOtherWarningFragment() {
        findNavController().doNavigate(
            SubmissionResultPositiveEfgsConsentFragmentDirections
                .actionEFGSConsentFragmentToPositiveOtherWarningFragment()
        )
    }

    private fun navigateToSubmissionDoneFragment() {
        findNavController().doNavigate(
            SubmissionResultPositiveEfgsConsentFragmentDirections
                .actionSubmissionResultPositiveEfgsConsentWarningFragmentToSubmissionDoneFragment()
        )
    }

    private fun navigateToTargetGermanyFragment() {
        // TODO: Place here the route to the next fragment
    }

    private fun navigateToSubmissionResultFragment() {
        findNavController().doNavigate(
            SubmissionResultPositiveEfgsConsentFragmentDirections
                .actionSubmissionResultPositiveEfgsConsentWarningFragmentToSubmissionResultFragment()
        )
    }

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
    }
}
