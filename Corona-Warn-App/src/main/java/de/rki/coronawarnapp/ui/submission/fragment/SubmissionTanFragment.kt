package de.rki.coronawarnapp.ui.submission.fragment

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSubmissionTanBinding
import de.rki.coronawarnapp.exception.http.BadRequestException
import de.rki.coronawarnapp.exception.http.CwaClientError
import de.rki.coronawarnapp.exception.http.CwaServerError
import de.rki.coronawarnapp.exception.http.CwaWebException
import de.rki.coronawarnapp.ui.doNavigate
import de.rki.coronawarnapp.ui.main.MainActivity
import de.rki.coronawarnapp.ui.submission.ApiRequestState
import de.rki.coronawarnapp.ui.submission.TanConstants
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionTanViewModel
import de.rki.coronawarnapp.ui.viewmodel.SubmissionViewModel
import de.rki.coronawarnapp.util.DialogHelper
import de.rki.coronawarnapp.util.TanHelper
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.observeEvent
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import kotlinx.android.synthetic.main.include_submission_tan.*
import javax.inject.Inject

/**
 * Fragment for TAN entry
 */
class SubmissionTanFragment : Fragment(R.layout.fragment_submission_tan), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val submissionViewModel: SubmissionViewModel by viewModels()
    private val viewModel: SubmissionTanViewModel by cwaViewModels { viewModelFactory }
    private val binding: FragmentSubmissionTanBinding by viewBindingLazy()

    private fun buildErrorDialog(exception: CwaWebException): DialogHelper.DialogInstance {
        return when (exception) {
            is BadRequestException -> DialogHelper.DialogInstance(
                requireActivity(),
                R.string.submission_error_dialog_web_test_paired_title_tan,
                R.string.submission_error_dialog_web_test_paired_body_tan,
                R.string.submission_error_dialog_web_test_paired_button_positive,
                null,
                true,
                ::goBack
            )
            is CwaClientError, is CwaServerError -> DialogHelper.DialogInstance(
                requireActivity(),
                R.string.submission_error_dialog_web_generic_error_title,
                getString(
                    R.string.submission_error_dialog_web_generic_network_error_body,
                    exception.statusCode
                ),
                R.string.submission_error_dialog_web_generic_error_button_positive,
                null,
                true,
                ::goBack
            )
            else -> DialogHelper.DialogInstance(
                requireActivity(),
                R.string.submission_error_dialog_web_generic_error_title,
                R.string.submission_error_dialog_web_generic_error_body,
                R.string.submission_error_dialog_web_generic_error_button_positive,
                null,
                true,
                ::goBack
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewmodel = viewModel

        binding.submissionTanContent.submissionTanInput.listener = { tan ->
            resetError()

            viewModel.tan.value = tan

            if (tan != null) {
                if (!TanHelper.allCharactersValid(tan))
                    showCharacterError()

                if (tan.length == TanConstants.MAX_LENGTH && !TanHelper.isChecksumValid(tan))
                    showTanError()
            }
        }

        binding.submissionTanButtonEnter.setOnClickListener { storeTanAndContinue() }
        binding.submissionTanHeader.headerButtonBack.buttonIcon.setOnClickListener { goBack() }

        submissionViewModel.registrationState.observeEvent(viewLifecycleOwner) {
            binding.submissionTanSpinner.visibility = when (it) {
                ApiRequestState.STARTED -> View.VISIBLE
                else -> View.GONE
            }

            if (ApiRequestState.SUCCESS == it) {
                findNavController().doNavigate(
                    SubmissionTanFragmentDirections.actionSubmissionTanFragmentToSubmissionResultFragment()
                )
            }
        }

        submissionViewModel.registrationError.observeEvent(viewLifecycleOwner) {
            DialogHelper.showDialog(buildErrorDialog(it))
        }
    }

    private fun resetError() {
        submission_tan_character_error.visibility = View.GONE
        submission_tan_error.visibility = View.GONE
    }

    private fun showCharacterError() {
        submission_tan_character_error.visibility = View.VISIBLE
    }

    private fun showTanError() {
        submission_tan_error.visibility = View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        binding.submissionTanRoot.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }

    private fun goBack() = (activity as MainActivity).goBack()

    private fun storeTanAndContinue() {
        // verify input format
        if (viewModel.isValidTanFormat.value != true)
            return

        // store locally
        viewModel.storeTeletan()

        submissionViewModel.doDeviceRegistration()
    }
}
