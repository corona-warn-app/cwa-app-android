package de.rki.coronawarnapp.ui.submission.qrcode.info

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSubmissionQrCodeInfoBinding
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

class SubmissionQRCodeInfoFragment : Fragment(R.layout.fragment_submission_qr_code_info), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: SubmissionQRCodeInfoFragmentViewModel by cwaViewModels { viewModelFactory }
    private val binding: FragmentSubmissionQrCodeInfoBinding by viewBindingLazy()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.submissionQrCodeInfoHeader.headerButtonBack.buttonIcon.setOnClickListener {
            viewModel.onBackPressed()
        }

        binding.submissionQrInfoButtonNext.setOnClickListener {
            viewModel.onNextPressed()
        }

        viewModel.navigateToDispatcher.observe2(this) {
            findNavController().popBackStack()
        }

        viewModel.navigateToQRScan.observe2(this) {
            doNavigate(
                SubmissionQRCodeInfoFragmentDirections.actionSubmissionQRCodeInfoFragmentToSubmissionQRCodeScanFragment()
            )
        }
    }
}
