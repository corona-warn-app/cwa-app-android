package de.rki.coronawarnapp.ui.submission

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.android.volley.TimeoutError
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSubmissionTestResultBinding
import de.rki.coronawarnapp.ui.BaseFragment
import de.rki.coronawarnapp.ui.viewmodel.SubmissionViewModel
import de.rki.coronawarnapp.util.DialogHelper

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

    private fun navigateToMainScreen() =
        doNavigate(SubmissionTestResultFragmentDirections.actionSubmissionResultFragmentToMainFragment())

    private fun buildErrorDialog(exception: Exception): DialogHelper.DialogInstance {
        return when (exception) {
            is TimeoutError -> DialogHelper.DialogInstance(
                R.string.submission_error_dialog_web_generic_timeout_title,
                R.string.submission_error_dialog_web_generic_timeout_body,
                R.string.submission_error_dialog_web_generic_timeout_button_positive,
                R.string.submission_error_dialog_web_generic_timeout_button_negative,
                viewModel::refreshTestResult,
                ::navigateToMainScreen
            )
            else -> DialogHelper.DialogInstance(
                R.string.submission_error_dialog_web_generic_error_title,
                R.string.submission_error_dialog_web_generic_error_body,
                R.string.submission_error_dialog_web_generic_error_button_positive,
                null,
                ::navigateToMainScreen
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setButtonOnClickListener()

        viewModel.testResultError.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                DialogHelper.showDialog(requireActivity(), buildErrorDialog(it))
            }
        })
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshTestResult()
    }

    private fun setButtonOnClickListener() {
        binding.submissionTestResultButtonPendingRefresh.setOnClickListener {
            viewModel.refreshTestResult()
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
