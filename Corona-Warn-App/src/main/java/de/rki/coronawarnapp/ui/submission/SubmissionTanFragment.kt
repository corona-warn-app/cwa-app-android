package de.rki.coronawarnapp.ui.submission

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import de.rki.coronawarnapp.databinding.FragmentSubmissionTanBinding
import de.rki.coronawarnapp.ui.BaseFragment

/**
 * Fragment for TAN entry
 */
class SubmissionTanFragment : BaseFragment() {

    private val viewModel: SubmissionTanViewModel by activityViewModels()
    private var _binding: FragmentSubmissionTanBinding? = null
    private val binding: FragmentSubmissionTanBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // get the binding reference by inflating it with the current layout
        _binding = FragmentSubmissionTanBinding.inflate(inflater)
        binding.viewmodel = viewModel
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.submissionTanInput.listener = { tan -> viewModel.tan.value = tan }
        binding.submissionTanButtonEnter.setOnClickListener { storeTanAndContinue() }
        binding.submissionTanHeader.headerButtonBack.buttonIcon.setOnClickListener { close() }
    }

    private fun close() {
        doNavigate(SubmissionTanFragmentDirections.actionSubmissionTanFragmentToMainFragment())
    }

    private fun storeTanAndContinue() {
        // verify input format
        if (viewModel.isValidTanFormat.value != true)
            return

        // store locally
        viewModel.storeTeletan()

        doNavigate(SubmissionTanFragmentDirections.actionSubmissionTanFragmentToSubmissionRegisterDeviceFragment())
    }
}
