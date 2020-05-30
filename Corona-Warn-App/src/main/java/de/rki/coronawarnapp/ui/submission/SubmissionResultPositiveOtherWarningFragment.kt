package de.rki.coronawarnapp.ui.submission

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSubmissionPositiveOtherWarningBinding
import de.rki.coronawarnapp.ui.BaseFragment
import de.rki.coronawarnapp.ui.register.ApiRequestState
import de.rki.coronawarnapp.ui.viewmodel.SubmissionViewModel

class SubmissionResultPositiveOtherWarningFragment : BaseFragment() {

    companion object {
        private val TAG: String? = SubmissionResultPositiveOtherWarningFragment::class.simpleName
    }

    private val viewModel: SubmissionViewModel by activityViewModels()
    private lateinit var binding: FragmentSubmissionPositiveOtherWarningBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSubmissionPositiveOtherWarningBinding.inflate(inflater)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setButtonOnClickListener()

        // TODO Maybe move this to a discrete transaction containing both steps
        viewModel.authCodeState.observe(viewLifecycleOwner, Observer {
            if (it == ApiRequestState.SUCCESS) {
                viewModel.submitDiagnosisKeys()
            }
        })

        viewModel.submissionState.observe(viewLifecycleOwner, Observer {
            if (it == ApiRequestState.SUCCESS) {
                doNavigate(
                    SubmissionResultPositiveOtherWarningFragmentDirections
                        .actionSubmissionResultPositiveOtherWarningFragmentToMainFragment()
                )
            }
        })
    }

    private fun setButtonOnClickListener() {
        binding.submissionPositiveOtherWarningButton.setOnClickListener {
            showShareIDConfirmationDialog()
        }
        binding.submissionPositiveOtherWarningHeader
            .informationHeader.headerButtonBack.buttonIcon.setOnClickListener {
                doNavigate(
                    SubmissionResultPositiveOtherWarningFragmentDirections
                        .actionSubmissionResultPositiveOtherWarningFragmentToSubmissionResultFragment()
                )
            }
    }

    private fun showShareIDConfirmationDialog() {
        val alertDialog: AlertDialog = requireActivity().let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setTitle(R.string.submission_positive_dialog_confirmation_title)
                setMessage(R.string.submission_positive_dialog_confirmation_body)
                setPositiveButton(
                    R.string.submission_positive_dialog_confirmation_positive
                ) { _, _ ->
                    viewModel.requestAuthCode()
                }
                setNegativeButton(
                    R.string.submission_positive_dialog_confirmation_negative
                ) { _, _ -> }
            }
            builder.create()
        }
        alertDialog.show()
    }
}
