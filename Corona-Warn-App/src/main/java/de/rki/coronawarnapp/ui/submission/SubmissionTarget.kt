package de.rki.coronawarnapp.ui.submission

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import de.rki.coronawarnapp.databinding.FragmentSubmissionTargetBinding
import de.rki.coronawarnapp.ui.main.MainActivity
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionTargetViewModel

/**
 * Submission interoperability question screen.
 */
class SubmissionTarget : Fragment() {

    private var _binding: FragmentSubmissionTargetBinding? = null
    private val binding: FragmentSubmissionTargetBinding get() = _binding!!
    private val viewModel: SubmissionTargetViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSubmissionTargetBinding.inflate(inflater)
        binding.submissionTargetViewModel = viewModel
        binding.lifecycleOwner = this
        binding.target.verifyState = "verify"
        binding.target.applyState = "apply"
        binding.target.rejectState = "reject"
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setButtonOnClickListener()
    }

    private fun setButtonOnClickListener() {
        binding.target.targetButtonVerify.setOnClickListener {
            onClickButtonVerifyHandler()
        }

        binding.submissionTargetHeader.headerButtonBack.buttonIcon.setOnClickListener {
            (activity as MainActivity).goBack()
        }

        binding.target.targetButtonApply.setOnClickListener {
            onClickButtonApplyHandler()
        }

        binding.target.targetButtonReject.setOnClickListener {
            onClickButtonRejectHandler()
        }
    }

    // change current button selected

    private fun onChangeCurrentButtonSelected(state: String?) {
        if (viewModel.currentButtonSelected.value.toString() !== state) {
            viewModel.setCurrentButtonSelected(state.toString())
        } else {
            viewModel.setCurrentButtonSelected("")
        }
    }

    private fun onClickButtonVerifyHandler() =
        onChangeCurrentButtonSelected(binding.target.verifyState)

    private fun onClickButtonApplyHandler() =
        onChangeCurrentButtonSelected(binding.target.applyState)

    private fun onClickButtonRejectHandler() =
        onChangeCurrentButtonSelected(binding.target.rejectState)

    override fun onResume() {
        super.onResume()
        binding.submissionTargetContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }
}
