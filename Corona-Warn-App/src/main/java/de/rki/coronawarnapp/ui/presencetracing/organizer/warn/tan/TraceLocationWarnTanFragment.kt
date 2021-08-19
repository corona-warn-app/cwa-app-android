package de.rki.coronawarnapp.ui.presencetracing.organizer.warn.tan

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.TraceLocationOrganizerWarnTanFragmentBinding
import de.rki.coronawarnapp.exception.http.BadRequestException
import de.rki.coronawarnapp.exception.http.CwaClientError
import de.rki.coronawarnapp.exception.http.CwaServerError
import de.rki.coronawarnapp.exception.http.CwaWebException
import de.rki.coronawarnapp.ui.submission.ApiRequestState
import de.rki.coronawarnapp.util.DialogHelper
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.setGone
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import javax.inject.Inject

class TraceLocationWarnTanFragment : Fragment(R.layout.trace_location_organizer_warn_tan_fragment), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val navArgs by navArgs<TraceLocationWarnTanFragmentArgs>()
    private val viewModel: TraceLocationWarnTanViewModel by cwaViewModelsAssisted(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, _ ->
            factory as TraceLocationWarnTanViewModel.Factory
            factory.create(navArgs.traceLocationWarnDuration)
        }
    )

    private val binding: TraceLocationOrganizerWarnTanFragmentBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.state.observe2(this) {
            binding.apply {
                uiState = it

                tanContent.submissionTanCharacterError.setGone(it.areCharactersCorrect)
                if (it.isCorrectLength) {
                    tanContent.submissionTanError.setGone(it.isTanValid)
                } else {
                    tanContent.submissionTanError.setGone(true)
                }
            }
        }

        viewModel.routeToScreen.observe2(this) {
            when (it) {
//                is SubmissionNavigationEvents.NavigateToDeletionWarningFragmentFromTan ->
//                    doNavigate(
//                        SubmissionTanFragmentDirections.actionSubmissionTanFragmentToSubmissionDeletionWarningFragment(
//                            testRegistrationRequest = it.coronaTestTan,
//                            isConsentGiven = it.consentGiven,
//                        )
//                    )
            }
        }

        binding.apply {
            tanContent.submissionTanInput.listener = { tan ->
                tanContent.submissionTanCharacterError.visibility = View.GONE
                tanContent.submissionTanError.visibility = View.GONE

                viewModel.onTanChanged(tan)
            }

            tanButtonEnter.setOnClickListener {
                viewModel.startTanSubmission()
            }
            tanHeader.headerButtonBack.buttonIcon.setOnClickListener { goBack() }
        }

        viewModel.registrationState.observe2(this) {
            binding.tanSpinner.visibility = when (it) {
                ApiRequestState.STARTED -> View.VISIBLE
                else -> View.GONE
            }

            if (ApiRequestState.SUCCESS == it) {
                // TODO What about negative tests and consent?
//                doNavigate(
//                    SubmissionTanFragmentDirections.actionSubmissionTanFragmentToSubmissionTestResultNoConsentFragment(
//                        CoronaTest.Type.PCR
//                    )
//                )
            }
        }

        viewModel.registrationError.observe2(this) {
            DialogHelper.showDialog(buildErrorDialog(it))
        }
    }

    override fun onResume() {
        super.onResume()
        binding.tanRoot.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }

    private fun goBack() {
        popBackStack()
    }

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
                R.string.submission_error_dialog_web_generic_network_error_body,
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
