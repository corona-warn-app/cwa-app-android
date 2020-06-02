package de.rki.coronawarnapp.ui.submission

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.google.android.gms.nearby.exposurenotification.TemporaryExposureKey
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSubmissionPositiveOtherWarningBinding
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
            viewModel.requestSubmissionPermission(internalExposureNotificationPermissionHelper)
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setButtonOnClickListener()

        viewModel.submissionState.observe(viewLifecycleOwner, Observer {
            if (it == ApiRequestState.SUCCESS) {
                doNavigate(
                    SubmissionResultPositiveOtherWarningFragmentDirections
                        .actionSubmissionResultPositiveOtherWarningFragmentToSubmissionDoneFragment()
                )
            }
        })

        viewModel.permissionState.observe(viewLifecycleOwner, Observer {
            if (it == ApiRequestState.FAILED) {
                val successfulScanDialogInstance = DialogHelper.DialogInstance(
                    requireActivity(),
                    R.string.submission_en_disabled_dialog_headline,
                    R.string.submission_en_disabled_dialog_body,
                    R.string.submission_en_disabled_dialog_button_positive,
                    R.string.submission_en_disabled_dialog_button_negative,
                    {
                        // TODO: Navigate to settings
                    },
                    {
                        submissionFailed = true
                        doNavigate(
                            SubmissionResultPositiveOtherWarningFragmentDirections
                                .actionSubmissionResultPositiveOtherWarningFragmentToSubmissionResultFragment()
                        )
                    }
                )
                DialogHelper.showDialog(successfulScanDialogInstance)
            } else if (it == ApiRequestState.SUCCESS) {
                submissionRequested = true
            }
        })
    }

    private fun setButtonOnClickListener() {
        binding.submissionPositiveOtherWarningButton.setOnClickListener {
            viewModel.requestSubmissionPermission(internalExposureNotificationPermissionHelper)
        }
        binding.submissionPositiveOtherWarningHeader
            .informationHeader.headerButtonBack.buttonIcon.setOnClickListener {
                doNavigate(
                    SubmissionResultPositiveOtherWarningFragmentDirections
                        .actionSubmissionResultPositiveOtherWarningFragmentToSubmissionResultFragment()
                )
            }
    }
}
