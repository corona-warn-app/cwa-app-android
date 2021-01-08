package de.rki.coronawarnapp.ui.submission.tan

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSubmissionTanBinding
import de.rki.coronawarnapp.exception.http.BadRequestException
import de.rki.coronawarnapp.exception.http.CwaClientError
import de.rki.coronawarnapp.exception.http.CwaServerError
import de.rki.coronawarnapp.exception.http.CwaWebException
import de.rki.coronawarnapp.ui.main.MainActivity
import de.rki.coronawarnapp.ui.submission.ApiRequestState
import de.rki.coronawarnapp.util.DialogHelper
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.setGone
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

/**
 * [SubmissionTanFragment] for submission via TAN entry
 */
class SubmissionTanFragment : Fragment(R.layout.fragment_submission_tan), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: SubmissionTanViewModel by cwaViewModels { viewModelFactory }

    private val binding: FragmentSubmissionTanBinding by viewBindingLazy()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.state.observe2(this) {
            binding.apply {
                uiState = it

                submissionTanContent.submissionTanCharacterError.setGone(it.areCharactersCorrect)
                if (it.isCorrectLength) {
                    submissionTanContent.submissionTanError.setGone(it.isTanValid)
                } else {
                    submissionTanContent.submissionTanError.setGone(true)
                }
            }
        }

        binding.apply {
            submissionTanContent.submissionTanInput.listener = { tan ->
                submissionTanContent.submissionTanCharacterError.visibility = View.GONE
                submissionTanContent.submissionTanError.visibility = View.GONE

                viewModel.onTanChanged(tan)
            }

            submissionTanButtonEnter.setOnClickListener {
                viewModel.onTanSubmit()
            }
            submissionTanHeader.headerButtonBack.buttonIcon.setOnClickListener { goBack() }
        }

        viewModel.registrationState.observe2(this) {
            binding.submissionTanSpinner.visibility = when (it) {
                ApiRequestState.STARTED -> View.VISIBLE
                else -> View.GONE
            }

            if (ApiRequestState.SUCCESS == it) {
                // TODO What about negative tests and consent?
                doNavigate(
                    SubmissionTanFragmentDirections.actionSubmissionTanFragmentToSubmissionTestResultNoConsentFragment()
                )
            }
        }

        viewModel.registrationError.observe2(this) {
            DialogHelper.showDialog(buildErrorDialog(it))
        }
    }

    override fun onResume() {
        super.onResume()
        binding.submissionTanRoot.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }

    private fun goBack() = (activity as MainActivity).goBack()

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
}
