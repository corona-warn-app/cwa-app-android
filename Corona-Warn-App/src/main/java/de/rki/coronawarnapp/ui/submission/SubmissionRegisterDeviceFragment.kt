package de.rki.coronawarnapp.ui.submission

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSubmissionRegisterDeviceBinding
import de.rki.coronawarnapp.exception.TestAlreadyPairedException
import de.rki.coronawarnapp.ui.BaseFragment
import de.rki.coronawarnapp.ui.viewmodel.SubmissionViewModel
import de.rki.coronawarnapp.util.DialogHelper
import java.net.SocketTimeoutException

class SubmissionRegisterDeviceFragment : BaseFragment() {
    private val viewModel: SubmissionViewModel by activityViewModels()
    private lateinit var binding: FragmentSubmissionRegisterDeviceBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSubmissionRegisterDeviceBinding.inflate(inflater)
        binding.lifecycleOwner = this
        return binding.root
    }

    private fun navigateToDispatchScreen() = doNavigate(
        SubmissionRegisterDeviceFragmentDirections
            .actionSubmissionRegisterDeviceFragmentToSubmissionDispatcherFragment()
    )

    private fun buildErrorDialog(exception: Exception): DialogHelper.DialogInstance {
        return when (exception) {
            is SocketTimeoutException -> DialogHelper.DialogInstance(
                requireActivity(),
                R.string.submission_error_dialog_web_generic_timeout_title,
                R.string.submission_error_dialog_web_generic_timeout_body,
                R.string.submission_error_dialog_web_generic_timeout_button_positive,
                R.string.submission_error_dialog_web_generic_timeout_button_negative,
                true,
                viewModel::doDeviceRegistration,
                ::navigateToDispatchScreen
            )
            is TestAlreadyPairedException -> DialogHelper.DialogInstance(
                requireActivity(),
                R.string.submission_error_dialog_web_test_paired_title,
                R.string.submission_error_dialog_web_test_paired_body,
                R.string.submission_error_dialog_web_test_paired_button_positive,
                null,
                true,
                ::navigateToDispatchScreen
            )
            else -> DialogHelper.DialogInstance(
                requireActivity(),
                R.string.submission_error_dialog_web_generic_error_title,
                R.string.submission_error_dialog_web_generic_error_body,
                R.string.submission_error_dialog_web_generic_error_button_positive,
                null,
                true,
                ::navigateToDispatchScreen
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.registrationState.observe(viewLifecycleOwner, Observer {
            if (ApiRequestState.SUCCESS == it) {
                doNavigate(
                    SubmissionRegisterDeviceFragmentDirections
                        .actionSubmissionRegisterDeviceFragmentToSubmissionResultFragment()
                )
            }
        })

        viewModel.registrationError.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                DialogHelper.showDialog(buildErrorDialog(it))
            }
        })
    }

    override fun onResume() {
        super.onResume()
        viewModel.doDeviceRegistration()
    }
}
