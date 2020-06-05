package de.rki.coronawarnapp.ui.submission

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSubmissionDispatcherBinding
import de.rki.coronawarnapp.ui.BaseFragment
import de.rki.coronawarnapp.ui.main.MainActivity
import de.rki.coronawarnapp.util.DialogHelper

class SubmissionDispatcherFragment : BaseFragment() {

    companion object {
        private val TAG: String? = SubmissionDispatcherFragment::class.simpleName
    }

    private lateinit var binding: FragmentSubmissionDispatcherBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSubmissionDispatcherBinding.inflate(inflater)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setButtonOnClickListener()
    }

    private fun setButtonOnClickListener() {
        binding.submissionDispatcherHeader.headerButtonBack.buttonIcon.setOnClickListener {
            (activity as MainActivity).goBack()
        }
        binding.submissionDispatcherQr.dispatcherCard.setOnClickListener {
            checkForDataPrivacyPermission()
        }
        binding.submissionDispatcherTanCode.dispatcherCard.setOnClickListener {
            doNavigate(
                SubmissionDispatcherFragmentDirections
                    .actionSubmissionDispatcherFragmentToSubmissionTanFragment()
            )
        }
        binding.submissionDispatcherTanTele.dispatcherCard.setOnClickListener {
            doNavigate(
                SubmissionDispatcherFragmentDirections
                    .actionSubmissionDispatcherFragmentToSubmissionContactFragment()
            )
        }
    }

    private fun checkForDataPrivacyPermission() {
        val cameraPermissionRationaleDialogInstance = DialogHelper.DialogInstance(
            requireActivity(),
            R.string.submission_dispatcher_qr_privacy_dialog_headline,
            R.string.submission_dispatcher_qr_privacy_dialog_body,
            R.string.submission_dispatcher_qr_privacy_dialog_button_positive,
            R.string.submission_dispatcher_qr_privacy_dialog_button_negative,
            true,
            {
                privacyPermissionIsGranted()
            }
        )

        DialogHelper.showDialog(cameraPermissionRationaleDialogInstance)
    }

    private fun privacyPermissionIsGranted() {
        doNavigate(
            SubmissionDispatcherFragmentDirections
                .actionSubmissionDispatcherFragmentToSubmissionQRCodeScanFragment()
        )
    }
}
