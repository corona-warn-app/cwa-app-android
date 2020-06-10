package de.rki.coronawarnapp.ui.submission

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.activityViewModels
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSubmissionTanBinding
import de.rki.coronawarnapp.exception.http.BadRequestException
import de.rki.coronawarnapp.exception.http.CwaClientError
import de.rki.coronawarnapp.exception.http.CwaServerError
import de.rki.coronawarnapp.exception.http.CwaWebException
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import de.rki.coronawarnapp.ui.doNavigate
import de.rki.coronawarnapp.ui.viewmodel.SubmissionViewModel
import de.rki.coronawarnapp.util.DialogHelper
import de.rki.coronawarnapp.util.observeEvent

/**
 * Fragment for TAN entry
 */
class SubmissionTanFragment : Fragment() {

    private val viewModel: SubmissionTanViewModel by activityViewModels()
    private val submissionViewModel: SubmissionViewModel by activityViewModels()
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

    private fun buildErrorDialog(exception: CwaWebException): DialogHelper.DialogInstance {
        return when (exception) {
            is BadRequestException -> DialogHelper.DialogInstance(
                requireActivity(),
                R.string.submission_error_dialog_web_test_paired_title,
                R.string.submission_error_dialog_web_test_paired_body,
                R.string.submission_error_dialog_web_test_paired_button_positive,
                null,
                true,
                ::navigateToDispatchScreen
            )
            is CwaServerError -> DialogHelper.DialogInstance(
                requireActivity(),
                R.string.submission_error_dialog_web_generic_error_title,
                getString(
                    R.string.submission_error_dialog_web_generic_network_error_body,
                    exception.statusCode
                ),
                R.string.submission_error_dialog_web_generic_error_button_positive,
                null,
                true,
                ::navigateToDispatchScreen
            )
            is CwaClientError -> DialogHelper.DialogInstance(
                requireActivity(),
                R.string.submission_error_dialog_web_generic_error_title,
                getString(
                    R.string.submission_error_dialog_web_generic_network_error_body,
                    exception.statusCode
                ),
                R.string.submission_error_dialog_web_generic_error_button_positive,
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

        binding.submissionTanContent.submissionTanInput.listener = { tan -> viewModel.tan.value = tan }
        binding.submissionTanButtonEnter.setOnClickListener { storeTanAndContinue() }
        binding.submissionTanHeader.headerButtonBack.buttonIcon.setOnClickListener { navigateToDispatchScreen() }

        submissionViewModel.registrationState.observeEvent(viewLifecycleOwner, {
            if (ApiRequestState.SUCCESS == it) {
                findNavController().doNavigate(
                    SubmissionTanFragmentDirections.actionSubmissionTanFragmentToSubmissionResultFragment()
                )
            }
        })

        submissionViewModel.registrationError.observeEvent(viewLifecycleOwner, {
            DialogHelper.showDialog(buildErrorDialog(it))
        })
    }

    override fun onStart() {
        super.onStart()
        binding.submissionTanRoot.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
    }

    override fun onResume() {
        super.onResume()
        binding.submissionTanRoot.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
    }

    private fun navigateToDispatchScreen() =
        findNavController().doNavigate(
            SubmissionTanFragmentDirections.actionSubmissionTanFragmentToSubmissionDispatcherFragment()
        )

    private fun storeTanAndContinue() {
        // verify input format
        if (viewModel.isValidTanFormat.value != true)
            return

        // store locally
        viewModel.storeTeletan()

        submissionViewModel.doDeviceRegistration()
    }
}
