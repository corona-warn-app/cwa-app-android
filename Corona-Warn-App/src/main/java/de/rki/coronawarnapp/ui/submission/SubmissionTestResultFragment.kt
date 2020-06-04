package de.rki.coronawarnapp.ui.submission

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSubmissionTestResultBinding
import de.rki.coronawarnapp.ui.BaseFragment
import de.rki.coronawarnapp.ui.viewmodel.SubmissionViewModel
import de.rki.coronawarnapp.ui.viewmodel.TracingViewModel
import de.rki.coronawarnapp.util.DialogHelper
import java.net.SocketTimeoutException

/**
 * A simple [BaseFragment] subclass.
 */
class SubmissionTestResultFragment : BaseFragment() {
    companion object {
        private val TAG: String? = SubmissionTanFragment::class.simpleName
    }

    private val submissionViewModel: SubmissionViewModel by activityViewModels()
    private val tracingViewModel: TracingViewModel by activityViewModels()

    private lateinit var binding: FragmentSubmissionTestResultBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // get the binding reference by inflating it with the current layout
        binding = FragmentSubmissionTestResultBinding.inflate(inflater)
        binding.submissionViewModel = submissionViewModel
        binding.lifecycleOwner = this
        // Inflate the layout for this fragment
        return binding.root
    }

    private fun navigateToMainScreen() =
        doNavigate(SubmissionTestResultFragmentDirections.actionSubmissionResultFragmentToMainFragment())

    private fun buildErrorDialog(exception: Exception): DialogHelper.DialogInstance {
        return when (exception) {
            is SocketTimeoutException -> DialogHelper.DialogInstance(
                requireActivity(),
                R.string.submission_error_dialog_web_generic_timeout_title,
                R.string.submission_error_dialog_web_generic_timeout_body,
                R.string.submission_error_dialog_web_generic_timeout_button_positive,
                R.string.submission_error_dialog_web_generic_timeout_button_negative,
                true,
                submissionViewModel::refreshDeviceUIState,
                ::navigateToMainScreen
            )
            else -> DialogHelper.DialogInstance(
                requireActivity(),
                R.string.submission_error_dialog_web_generic_error_title,
                R.string.submission_error_dialog_web_generic_error_body,
                R.string.submission_error_dialog_web_generic_error_button_positive,
                null,
                true,
                ::navigateToMainScreen
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setButtonOnClickListener()

        submissionViewModel.testResultError.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                DialogHelper.showDialog(buildErrorDialog(it))
            }
        })
    }

    override fun onResume() {
        super.onResume()
        submissionViewModel.refreshDeviceUIState()
        tracingViewModel.refreshIsTracingEnabled()
    }

    private fun setButtonOnClickListener() {
        binding.submissionTestResultButtonPendingRefresh.setOnClickListener {
            submissionViewModel.refreshDeviceUIState()
        }

        binding.submissionTestResultButtonPendingRemoveTest.setOnClickListener {
            submissionViewModel.deregisterTestFromDevice()
            doNavigate(
                SubmissionTestResultFragmentDirections.actionSubmissionResultFragmentToMainFragment()
            )
        }

        binding.submissionTestResultButtonNegativeRemoveTest.setOnClickListener {
            submissionViewModel.deregisterTestFromDevice()
            doNavigate(
                SubmissionTestResultFragmentDirections.actionSubmissionResultFragmentToMainFragment()
            )
        }

        binding.submissionTestResultButtonPositiveContinue.setOnClickListener {
            continueIfTracingEnabled()
        }

        binding.submissionTestResultButtonInvalidRemoveTest.setOnClickListener {
            submissionViewModel.deregisterTestFromDevice()
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

    private fun continueIfTracingEnabled() {
        if (tracingViewModel.isTracingEnabled.value != true) {
            val tracingRequiredDialog = DialogHelper.DialogInstance(
                requireActivity(),
                R.string.submission_test_result_dialog_tracing_required_title,
                R.string.submission_test_result_dialog_tracing_required_message,
                R.string.submission_test_result_dialog_tracing_required_button
            )
            DialogHelper.showDialog(tracingRequiredDialog)
            return
        }

        doNavigate(
            SubmissionTestResultFragmentDirections
                .actionSubmissionResultFragmentToSubmissionResultPositiveOtherWarningFragment()
        )
    }
}
