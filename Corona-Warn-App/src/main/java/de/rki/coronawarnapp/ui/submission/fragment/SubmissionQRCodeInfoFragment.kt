package de.rki.coronawarnapp.ui.submission.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSubmissionQrCodeInfoBinding
import de.rki.coronawarnapp.ui.doNavigate
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionQRCodeInfoFragmentViewModel
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBindingLazy

class SubmissionQRCodeInfoFragment : Fragment(R.layout.fragment_submission_qr_code_info) {

    private val viewModel: SubmissionQRCodeInfoFragmentViewModel by viewModels()
    private val binding: FragmentSubmissionQrCodeInfoBinding by viewBindingLazy()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.submissionQrCodeInfoHeader.headerButtonBack.buttonIcon.setOnClickListener() {
            viewModel.onBackPressed()
        }

        binding.submissionQrInfoButtonNext.setOnClickListener() {
            viewModel.onNextPressed()
        }

        viewModel.navigateToDispatcher.observe2(this) {
            findNavController().popBackStack()
        }

        viewModel.navigateToQRScan.observe2(this) {
            findNavController().doNavigate(
                    SubmissionQRCodeInfoFragmentDirections
                        .actionSubmissionQRCodeInfoFragmentToSubmissionQRCodeScanFragment())
        }
    }
}
