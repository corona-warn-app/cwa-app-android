package de.rki.coronawarnapp.ui.submission

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSubmissionDispatcherBinding
import de.rki.coronawarnapp.ui.BaseFragment
import de.rki.coronawarnapp.ui.main.MainActivity
import de.rki.coronawarnapp.util.DialogHelper
import kotlinx.android.synthetic.main.fragment_submission_dispatcher.*
import kotlinx.android.synthetic.main.include_dispatcher_card.*


class SubmissionDispatcherFragment : BaseFragment() {

    companion object {
        private val TAG: String? = SubmissionDispatcherFragment::class.simpleName
    }

    private var _binding: FragmentSubmissionDispatcherBinding? = null
    private val binding: FragmentSubmissionDispatcherBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSubmissionDispatcherBinding.inflate(inflater)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setButtonOnClickListener()
        setContentDescription()
    }

    override fun onStart() {
        super.onStart()
        binding.submissionDispatcherScrollview.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
    }

    override fun onResume() {
        super.onResume()
        binding.submissionDispatcherScrollview.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
    }

    private fun setContentDescription() {
        dispatcher_card_icon.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO)
    }


    private fun setButtonOnClickListener() {
        binding.submissionDispatcherHeader.headerToolbar.setNavigationOnClickListener {
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
