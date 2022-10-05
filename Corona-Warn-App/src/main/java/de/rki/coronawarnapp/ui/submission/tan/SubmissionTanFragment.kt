package de.rki.coronawarnapp.ui.submission.tan

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.util.hideKeyboard
import de.rki.coronawarnapp.databinding.FragmentSubmissionTanBinding
import de.rki.coronawarnapp.exception.http.BadRequestException
import de.rki.coronawarnapp.exception.http.CwaClientError
import de.rki.coronawarnapp.exception.http.CwaServerError
import de.rki.coronawarnapp.exception.http.CwaWebException
import de.rki.coronawarnapp.ui.dialog.displayDialog
import de.rki.coronawarnapp.ui.submission.tan.SubmissionTanViewModel.TanApiRequestState
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionNavigationEvents
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.setGone
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

/**
 * [SubmissionTanFragment] for submission via TAN entry
 */
class SubmissionTanFragment : Fragment(R.layout.fragment_submission_tan), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: SubmissionTanViewModel by cwaViewModels { viewModelFactory }

    private val navArgs by navArgs<SubmissionTanFragmentArgs>()

    private val binding: FragmentSubmissionTanBinding by viewBinding()

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

        viewModel.routeToScreen.observe2(this) {
            when (it) {
                is SubmissionNavigationEvents.NavigateToDeletionWarningFragmentFromTan ->
                    doNavigate(
                        SubmissionTanFragmentDirections.actionSubmissionTanFragmentToSubmissionDeletionWarningFragment(
                            testRegistrationRequest = it.coronaTestTan,
                            comesFromDispatcherFragment = navArgs.comesFromDispatcherFragment
                        )
                    )
                else -> Unit
            }
        }

        binding.apply {
            submissionTanContent.submissionTanInput.listener = { tan ->
                submissionTanContent.submissionTanCharacterError.visibility = View.GONE
                submissionTanContent.submissionTanError.visibility = View.GONE

                viewModel.onTanChanged(tan)
            }

            submissionTanButtonEnter.setOnClickListener {
                submissionTanButtonEnter.hideKeyboard()
                viewModel.startTanSubmission()
            }
            toolbar.setNavigationOnClickListener { goBack() }
        }

        viewModel.registrationState.observe2(this) {
            binding.submissionTanSpinner.visibility = when (it) {
                TanApiRequestState.InProgress -> View.VISIBLE
                else -> View.GONE
            }

            when (it) {
                is TanApiRequestState.SuccessPositiveResult ->
                    doNavigate(
                        SubmissionTanFragmentDirections
                            .actionSubmissionTanFragmentToSubmissionTestResultNoConsentFragment(
                                testIdentifier = it.identifier,
                                comesFromDispatcherFragment = navArgs.comesFromDispatcherFragment
                            )
                    )
                is TanApiRequestState.SuccessPendingResult ->
                    doNavigate(
                        SubmissionTanFragmentDirections
                            .actionSubmissionTanFragmentToSubmissionTestResultPendingFragment(
                                testIdentifier = it.identifier,
                                comesFromDispatcherFragment = navArgs.comesFromDispatcherFragment
                            )
                    )
                else -> Unit
            }
        }

        viewModel.registrationError.observe2(this) { buildErrorDialog(it) }
    }

    override fun onResume() {
        super.onResume()
        binding.submissionTanRoot.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }

    private fun goBack() {
        popBackStack()
    }

    private fun buildErrorDialog(exception: CwaWebException) {
        when (exception) {
            is BadRequestException -> displayDialog {
                setTitle(R.string.submission_error_dialog_web_test_paired_title_tan)
                setMessage(R.string.submission_error_dialog_web_test_paired_body_tan)
                setNegativeButton(R.string.submission_error_dialog_web_test_paired_button_positive) { _, _ -> goBack() }
            }
            is CwaClientError, is CwaServerError -> displayDialog {
                setTitle(R.string.submission_error_dialog_web_generic_error_title)
                setMessage(R.string.submission_error_dialog_web_generic_network_error_body)
                setNegativeButton(R.string.submission_error_dialog_web_generic_error_button_positive) { _, _ ->
                    goBack()
                }
            }
            else -> displayDialog {
                setTitle(R.string.submission_error_dialog_web_generic_error_title)
                setMessage(R.string.submission_error_dialog_web_generic_error_body)
                setNegativeButton(R.string.submission_error_dialog_web_generic_error_button_positive) { _, _ ->
                    goBack()
                }
            }
        }
    }
}
