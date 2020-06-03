package de.rki.coronawarnapp.ui.submission

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.android.volley.TimeoutError
import com.google.android.gms.nearby.exposurenotification.TemporaryExposureKey
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSubmissionPositiveOtherWarningBinding
import de.rki.coronawarnapp.exception.SubmissionTanInvalidException
import de.rki.coronawarnapp.exception.TestPairingInvalidException
import de.rki.coronawarnapp.nearby.InternalExposureNotificationPermissionHelper
import de.rki.coronawarnapp.ui.BaseFragment
import de.rki.coronawarnapp.ui.viewmodel.SubmissionViewModel
import de.rki.coronawarnapp.util.DialogHelper

class SubmissionResultPositiveOtherWarningFragment : BaseFragment(),
    InternalExposureNotificationPermissionHelper.Callback {

    companion object {
        private val TAG: String? = SubmissionResultPositiveOtherWarningFragment::class.simpleName
    }

    private val viewModel: SubmissionViewModel by activityViewModels()
    private lateinit var binding: FragmentSubmissionPositiveOtherWarningBinding
    private var submissionRequested = false
    private var submissionFailed = false
    private lateinit var internalExposureNotificationPermissionHelper:
        InternalExposureNotificationPermissionHelper

    override fun onResume() {
        super.onResume()
        if (submissionRequested && !submissionFailed) {
            internalExposureNotificationPermissionHelper.requestPermissionToShareKeys()
        }
    }

    override fun onKeySharePermissionGranted(keys: List<TemporaryExposureKey>) {
        super.onKeySharePermissionGranted(keys)
        viewModel.submitDiagnosisKeys()
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
        binding = FragmentSubmissionPositiveOtherWarningBinding.inflate(inflater)
        binding.lifecycleOwner = this
        return binding.root
    }

    private fun buildErrorDialog(exception: Exception): DialogHelper.DialogInstance {
        return when (exception) {
            is TimeoutError -> DialogHelper.DialogInstance(
                R.string.submission_error_dialog_web_generic_timeout_title,
                R.string.submission_error_dialog_web_generic_timeout_body,
                R.string.submission_error_dialog_web_generic_timeout_button_positive,
                R.string.submission_error_dialog_web_generic_timeout_button_negative,
                viewModel::submitDiagnosisKeys,
                ::navigateToSubmissionResultFragment
            )
            is TestPairingInvalidException -> DialogHelper.DialogInstance(
                R.string.submission_error_dialog_web_paring_invalid_title,
                R.string.submission_error_dialog_web_paring_invalid_body,
                R.string.submission_error_dialog_web_paring_invalid_button_positive,
                null,
                ::navigateToSubmissionResultFragment
            )
            is SubmissionTanInvalidException -> DialogHelper.DialogInstance(
                R.string.submission_error_dialog_web_tan_invalid_title,
                R.string.submission_error_dialog_web_tan_invalid_body,
                R.string.submission_error_dialog_web_tan_invalid_button_positive,
                null,
                ::navigateToSubmissionResultFragment
            )
            else -> DialogHelper.DialogInstance(
                R.string.submission_error_dialog_web_generic_error_title,
                R.string.submission_error_dialog_web_generic_error_body,
                R.string.submission_error_dialog_web_generic_error_button_positive,
                null,
                ::navigateToSubmissionResultFragment
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setButtonOnClickListener()

        viewModel.submissionError.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                DialogHelper.showDialog(requireActivity(), buildErrorDialog(it))
            }
        })

        viewModel.submissionState.observe(viewLifecycleOwner, Observer {
            if (it == ApiRequestState.SUCCESS) {
                doNavigate(
                    SubmissionResultPositiveOtherWarningFragmentDirections
                        .actionSubmissionResultPositiveOtherWarningFragmentToSubmissionDoneFragment()
                )
            }
        })
    }

    private fun setButtonOnClickListener() {
        binding.submissionPositiveOtherWarningButton.setOnClickListener {
            submissionRequested = true
            internalExposureNotificationPermissionHelper.requestPermissionToShareKeys()
        }
        binding.submissionPositiveOtherWarningHeader
            .informationHeader.headerButtonBack.buttonIcon.setOnClickListener {
                navigateToSubmissionResultFragment()
            }
    }

    private fun navigateToSubmissionResultFragment() =
        doNavigate(
            SubmissionResultPositiveOtherWarningFragmentDirections
                .actionSubmissionResultPositiveOtherWarningFragmentToSubmissionResultFragment()
        )
}
