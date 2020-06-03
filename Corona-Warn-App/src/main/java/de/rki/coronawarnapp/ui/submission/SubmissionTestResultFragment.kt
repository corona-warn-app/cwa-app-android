package de.rki.coronawarnapp.ui.submission

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import de.rki.coronawarnapp.databinding.FragmentSubmissionTestResultBinding
import de.rki.coronawarnapp.ui.BaseFragment
import de.rki.coronawarnapp.ui.viewmodel.SubmissionViewModel

/**
 * A simple [BaseFragment] subclass.
 */
class SubmissionTestResultFragment : BaseFragment() {
    companion object {
        private val TAG: String? = SubmissionTanFragment::class.simpleName
    }

    private val viewModel: SubmissionViewModel by activityViewModels()
    private lateinit var binding: FragmentSubmissionTestResultBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // get the binding reference by inflating it with the current layout
        binding = FragmentSubmissionTestResultBinding.inflate(inflater)
        binding.submissionViewModel = viewModel
        binding.lifecycleOwner = this
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setButtonOnClickListener()
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshDeviceUIState()
    }

    private fun setButtonOnClickListener() {
        binding.submissionTestResultButtonPendingRefresh.setOnClickListener {
            viewModel.refreshDeviceUIState()
        }

        binding.submissionTestResultButtonPendingRemoveTest.setOnClickListener {
            viewModel.deregisterTestFromDevice()
            doNavigate(
                SubmissionTestResultFragmentDirections.actionSubmissionResultFragmentToMainFragment()
            )
        }

        binding.submissionTestResultButtonNegativeRemoveTest.setOnClickListener {
            viewModel.deregisterTestFromDevice()
            doNavigate(
                SubmissionTestResultFragmentDirections.actionSubmissionResultFragmentToMainFragment()
            )
        }

        binding.submissionTestResultButtonPositiveContinue.setOnClickListener {
            doNavigate(
                SubmissionTestResultFragmentDirections
                    .actionSubmissionResultFragmentToSubmissionResultPositiveOtherWarningFragment()
            )
        }

        binding.submissionTestResultButtonInvalidRemoveTest.setOnClickListener {
            viewModel.deregisterTestFromDevice()
            doNavigate(
                SubmissionTestResultFragmentDirections.actionSubmissionResultFragmentToMainFragment()
            )
        }

        binding.submissionTestResultHeader.informationHeader.headerButtonBack.buttonIcon.setOnClickListener {
            doNavigate(
                SubmissionTestResultFragmentDirections.actionSubmissionResultFragmentToMainFragment()
            )
        }
    }
}
