package de.rki.coronawarnapp.ui.submission.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import de.rki.coronawarnapp.databinding.FragmentSubmissionQrCodeInfoBinding
import de.rki.coronawarnapp.ui.doNavigate
import de.rki.coronawarnapp.ui.main.MainActivity
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionQRCodeInfoFragmentViewModel
import de.rki.coronawarnapp.util.ui.observe2

class SubmissionQRCodeInfoFragment : Fragment() {

    private val viewModel: SubmissionQRCodeInfoFragmentViewModel by viewModels()
    private var _binding: FragmentSubmissionQrCodeInfoBinding? = null
    private val binding: FragmentSubmissionQrCodeInfoBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSubmissionQrCodeInfoBinding.inflate(inflater)
        binding.submissionCountrySelectViewModel = viewModel
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.submissionQrCodeInfoHeader.headerButtonBack.buttonIcon.setOnClickListener() {
            viewModel.onBackPressed()
        }

        binding.submissionQrInfoButtonNext.setOnClickListener() {
            viewModel.onNextPressed()
        }

        viewModel.navigateBack.observe2(this) {
            if (it) {
                (requireActivity() as MainActivity).goBack()
            }
        }

        viewModel.navigateForward.observe2(this) {
            if (it) {
                findNavController().doNavigate(
                    SubmissionQRCodeInfoFragmentDirections
                        .actionSubmissionQRCodeInfoFragmentToSubmissionQRCodeScanFragment()
                )
            }
        }
    }
}
