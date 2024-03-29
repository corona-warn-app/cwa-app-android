package de.rki.coronawarnapp.ui.submission.tan

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
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
import de.rki.coronawarnapp.util.ui.popBackStack
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

        viewModel.state.observe(viewLifecycleOwner) {
            binding.apply {
                submissionTanButtonEnter.isEnabled = it.isTanValid

                submissionTanContent.submissionTanCharacterError.isGone = it.areCharactersCorrect
                if (it.isCorrectLength) {
                    submissionTanContent.submissionTanError.isGone = it.isTanValid
                } else {
                    submissionTanContent.submissionTanError.isGone = true
                }
            }
        }

        viewModel.routeToScreen.observe(viewLifecycleOwner) {
            when (it) {
                is SubmissionNavigationEvents.NavigateToDeletionWarningFragmentFromTan ->
                    findNavController().navigate(
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

        viewModel.registrationState.observe(viewLifecycleOwner) {
            binding.submissionTanSpinner.visibility = when (it) {
                TanApiRequestState.InProgress -> View.VISIBLE
                else -> View.GONE
            }

            when (it) {
                is TanApiRequestState.SuccessPositiveResult ->
                    findNavController().navigate(
                        SubmissionTanFragmentDirections
                            .actionSubmissionTanFragmentToSubmissionTestResultNoConsentFragment(
                                testIdentifier = it.identifier,
                                comesFromDispatcherFragment = navArgs.comesFromDispatcherFragment
                            )
                    )

                is TanApiRequestState.SuccessPendingResult ->
                    findNavController().navigate(
                        SubmissionTanFragmentDirections
                            .actionSubmissionTanFragmentToSubmissionTestResultPendingFragment(
                                testIdentifier = it.identifier,
                                comesFromDispatcherFragment = navArgs.comesFromDispatcherFragment
                            )
                    )

                else -> Unit
            }
        }

        viewModel.registrationError.observe(viewLifecycleOwner) { buildErrorDialog(it) }
    }

    override fun onResume() {
        super.onResume()
        binding.submissionTanRoot.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }

    private fun goBack() {
        binding.root.hideKeyboard()
        popBackStack()
    }

    private fun buildErrorDialog(exception: CwaWebException) {
        when (exception) {
            is BadRequestException -> displayDialog {
                title(R.string.submission_error_dialog_web_test_paired_title_tan)
                message(R.string.submission_error_dialog_web_test_paired_body_tan)
                negativeButton(R.string.submission_error_dialog_web_test_paired_button_positive) { goBack() }
            }

            is CwaClientError, is CwaServerError -> displayDialog {
                title(R.string.submission_error_dialog_web_generic_error_title)
                message(R.string.submission_error_dialog_web_generic_network_error_body)
                negativeButton(R.string.submission_error_dialog_web_generic_error_button_positive) { goBack() }
            }

            else -> displayDialog {
                title(R.string.submission_error_dialog_web_generic_error_title)
                message(R.string.submission_error_dialog_web_generic_error_body)
                negativeButton(R.string.submission_error_dialog_web_generic_error_button_positive) { goBack() }
            }
        }
    }
}
