package de.rki.coronawarnapp.ui.submission.fragment

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSubmissionDispatcherBinding
import de.rki.coronawarnapp.ui.doNavigate
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionDispatcherViewModel
import de.rki.coronawarnapp.util.DialogHelper
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

class SubmissionDispatcherFragment : Fragment(R.layout.fragment_submission_dispatcher), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: SubmissionDispatcherViewModel by cwaViewModels { viewModelFactory }
    private val binding: FragmentSubmissionDispatcherBinding by viewBindingLazy()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setButtonOnClickListener()

        viewModel.navigateQRScan.observe2(this) {
            findNavController().doNavigate(
                SubmissionDispatcherFragmentDirections
                    .actionSubmissionDispatcherFragmentToSubmissionQRCodeScanFragment()
            )
        }
        viewModel.navigateTAN.observe2(this) {
            findNavController().doNavigate(
                SubmissionDispatcherFragmentDirections
                    .actionSubmissionDispatcherFragmentToSubmissionTanFragment()
            )
        }
        viewModel.navigateTeleTAN.observe2(this) {
            findNavController().doNavigate(
                SubmissionDispatcherFragmentDirections
                    .actionSubmissionDispatcherFragmentToSubmissionContactFragment()
            )
        }
        viewModel.navigateBack.observe2(this) {
            findNavController().popBackStack()
        }
    }

    override fun onResume() {
        super.onResume()
        binding.submissionDispatcherRoot.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }

    private fun setButtonOnClickListener() {
        binding.submissionDispatcherHeader.headerButtonBack.buttonIcon.setOnClickListener {
            viewModel.onBackPressed()
        }
        binding.submissionDispatcherContent.submissionDispatcherQr.dispatcherCard.setOnClickListener {
            checkForDataPrivacyPermission()
        }
        binding.submissionDispatcherContent.submissionDispatcherTanCode.dispatcherCard.setOnClickListener {
            viewModel.onTanPressed()
        }
        binding.submissionDispatcherContent.submissionDispatcherTanTele.dispatcherCard.setOnClickListener {
            viewModel.onTeleTanPressed()
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
        viewModel.onQRScanPressed()
    }
}
