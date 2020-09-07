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
import de.rki.coronawarnapp.databinding.FragmentSubmissionEuropeanFederalGatewayServerConsentBinding
import de.rki.coronawarnapp.exception.http.BadRequestException
import de.rki.coronawarnapp.exception.http.CwaClientError
import de.rki.coronawarnapp.exception.http.CwaServerError
import de.rki.coronawarnapp.exception.http.ForbiddenException
import de.rki.coronawarnapp.nearby.InternalExposureNotificationPermissionHelper
import de.rki.coronawarnapp.ui.doNavigate
import de.rki.coronawarnapp.ui.viewmodel.EuropeanFederalGatewayServerConsentViewModel
import de.rki.coronawarnapp.ui.viewmodel.SubmissionViewModel
import de.rki.coronawarnapp.ui.viewmodel.TracingViewModel
import de.rki.coronawarnapp.util.DialogHelper
import de.rki.coronawarnapp.util.IGNORE_CHANGE_TAG
import de.rki.coronawarnapp.util.observeEvent

/**
 *  This fragment is used for showing the user an option to share they're keys with other European
 *  countries if they have been travelling outside of Germany.
 *  If the user leaves the switch deactivated, the system dialog for submitting the keys is triggered
 *  and the keys will only be submitted to the the German server.
 *  Otherwise, Screen 3: Target > Germany and "Screen 4: Target specific countries" are shown, and
 *  the user will be able to select what countries they want to share their keys with.
 */

class SubmissionEuropeanFederalGatewayServerConsentFragment : Fragment(),
    InternalExposureNotificationPermissionHelper.Callback {

    private val europeanFederalGatewayServerViewModel:
            EuropeanFederalGatewayServerConsentViewModel by activityViewModels()
    private val submissionViewModel: SubmissionViewModel by activityViewModels()
    private val tracingViewModel: TracingViewModel by activityViewModels()

    private var _binding: FragmentSubmissionEuropeanFederalGatewayServerConsentBinding? = null
    private val binding: FragmentSubmissionEuropeanFederalGatewayServerConsentBinding get() = _binding!!

    private lateinit var internalExposureNotificationPermissionHelper:
            InternalExposureNotificationPermissionHelper

    override fun onResume() {
        super.onResume()
        binding
            .submissionEuropeanFederalGatewayServerConsentPrivacyContainer
            .sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
        tracingViewModel.refreshIsTracingEnabled()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        internalExposureNotificationPermissionHelper =
            InternalExposureNotificationPermissionHelper(this, this)
        _binding = FragmentSubmissionEuropeanFederalGatewayServerConsentBinding.inflate(inflater)
        binding.submissionViewModel = submissionViewModel
        binding.europeanConsentViewModel = europeanFederalGatewayServerViewModel
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
        // Show error dialog in case submission to server fails
        submissionViewModel.submissionError.observeEvent(viewLifecycleOwner) {
            DialogHelper.showDialog(buildErrorDialog(it))
        }
        // If submission is successful, we navigate to the SubmissionDone fragment
        submissionViewModel.submissionState.observe(viewLifecycleOwner, Observer {
            if (it == ApiRequestState.SUCCESS) {
                navigateToSubmissionDoneFragment()
            }
        })
    }

    private fun setButtonOnClickListener() {
        binding
            .settingsEuropeanFederalGatewayServerConsentRow.settingsSwitchRowSwitch
            .setOnCheckedChangeListener { switch, isEnabled ->
                if (switch.tag != IGNORE_CHANGE_TAG) {
                    europeanFederalGatewayServerViewModel.isEuropeanConsentGranted.postValue(isEnabled)
                }
            }

        binding
            .submissionEuropeanFederalGatewayServerConsentHeader.headerButtonBack.buttonIcon
            .setOnClickListener { navigateToSubmissionResultPositiveOtherWarningFragment() }

        binding
            .submissionEuropeanFederalGatewayServerConsentButtonNext
            .setOnClickListener {
                if (europeanFederalGatewayServerViewModel.isEuropeanConsentGranted.value!!) {
                    navigateToTargetGermanyFragment()
                } else {
                    initiateWarningOthers()
                }
            }
    }

    private fun navigateToSubmissionResultPositiveOtherWarningFragment() {
        findNavController().doNavigate(
            SubmissionEuropeanFederalGatewayServerConsentFragmentDirections
                .actionEFGSConsentFragmentToPositiveOtherWarningFragment()
        )
    }

    private fun navigateToSubmissionDoneFragment() {
        findNavController().doNavigate(
            SubmissionEuropeanFederalGatewayServerConsentFragmentDirections
                .actionSubmissionResultPositiveEfgsConsentWarningFragmentToSubmissionDoneFragment()
        )
    }

    private fun navigateToTargetGermanyFragment() {
        // TODO: Place here the route to the next fragment
    }

    private fun navigateToSubmissionResultFragment() {
        findNavController().doNavigate(
            SubmissionEuropeanFederalGatewayServerConsentFragmentDirections
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
