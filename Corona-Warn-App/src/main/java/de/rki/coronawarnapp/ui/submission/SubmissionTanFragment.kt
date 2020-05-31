package de.rki.coronawarnapp.ui.submission

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.activityViewModels
import de.rki.coronawarnapp.databinding.FragmentSubmissionTanBinding
import de.rki.coronawarnapp.ui.BaseFragment

/**
 * Fragment for TAN entry
 */
class SubmissionTanFragment : BaseFragment() {

    private val viewModel: SubmissionTanViewModel by activityViewModels()
    private lateinit var binding: FragmentSubmissionTanBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // get the binding reference by inflating it with the current layout
        binding = FragmentSubmissionTanBinding.inflate(inflater)
        binding.viewmodel = viewModel
        binding.lifecycleOwner = this
        activity?.window?.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        return binding.root
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

        doNavigate(SubmissionTanFragmentDirections.actionSubmissionTanFragmentToDeviceRegistrationFragment())
    }
}
