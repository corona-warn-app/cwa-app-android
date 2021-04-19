package de.rki.coronawarnapp.ui.submission.deletionwarning

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.databinding.FragmentSubmissionDeletionWarningBinding
import de.rki.coronawarnapp.exception.http.BadRequestException
import de.rki.coronawarnapp.exception.http.CwaClientError
import de.rki.coronawarnapp.exception.http.CwaServerError
import de.rki.coronawarnapp.exception.http.CwaWebException
import de.rki.coronawarnapp.ui.submission.ApiRequestState
import de.rki.coronawarnapp.util.DialogHelper
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import javax.inject.Inject

class SubmissionDeletionWarningFragment : Fragment(R.layout.fragment_submission_deletion_warning), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory

    private val args by navArgs<SubmissionDeletionWarningFragmentArgs>()

    private val viewModel: SubmissionDeletionWarningViewModel by cwaViewModelsAssisted(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, _ ->
            factory as SubmissionDeletionWarningViewModel.Factory
            factory.create(args.coronaTestQrCode, args.isConsentGiven)
        }
    )
    private val binding: FragmentSubmissionDeletionWarningBinding by viewBindingLazy()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            val testType = args.coronaTestQrCode.type
            if (testType == CoronaTest.Type.PCR) {
                headline.text = getString(R.string.submission_deletion_warning_headline_pcr_test)
                body.text = getString(R.string.submission_deletion_warning_body_pcr_test)
            } else if (testType == CoronaTest.Type.RAPID_ANTIGEN) {
                headline.text = getString(R.string.submission_deletion_warning_headline_antigen_test)
                body.text = getString(R.string.submission_deletion_warning_body_antigen_test)
            }

            continueButton.setOnClickListener {
                viewModel.deleteExistingAndRegisterNewTest()
            }

            toolbar.apply {
                setNavigationOnClickListener {
                    doNavigate(
                        SubmissionDeletionWarningFragmentDirections
                            .actionSubmissionDeletionWarningFragmentToSubmissionConsentFragment()
                    )
                }
            }
        }

        viewModel.showRedeemedTokenWarning.observe2(this) {
            val dialog = DialogHelper.DialogInstance(
                requireActivity(),
                R.string.submission_error_dialog_web_tan_redeemed_title,
                R.string.submission_error_dialog_web_tan_redeemed_body,
                R.string.submission_error_dialog_web_tan_redeemed_button_positive
            )

            DialogHelper.showDialog(dialog)

            navigateToDispatchScreen()
        }

        viewModel.registrationState.observe2(this) { state ->
            binding.submissionQrCodeScanSpinner.visibility = when (state.apiRequestState) {
                ApiRequestState.STARTED -> View.VISIBLE
                else -> View.GONE
            }

            if (ApiRequestState.SUCCESS == state.apiRequestState) {
                if (state.testResult == CoronaTestResult.PCR_POSITIVE) {
                    doNavigate(
                        SubmissionDeletionWarningFragmentDirections
                            .actionSubmissionQRCodeScanFragmentToSubmissionTestResultAvailableFragment(
                                isConsentGiven = args.isConsentGiven
                            )
                    )
                } else {
                    doNavigate(
                        SubmissionDeletionWarningFragmentDirections
                            .actionSubmissionQRCodeScanFragmentToSubmissionTestResultPendingFragment(
                                isConsentGiven = args.isConsentGiven
                            )
                    )
                }
            }
        }

        viewModel.registrationError.observe2(this) {
            DialogHelper.showDialog(buildErrorDialog(it))
        }
    }

    private fun navigateToDispatchScreen() =
        doNavigate(
            SubmissionDeletionWarningFragmentDirections
                .actionSubmissionQRCodeScanFragmentToSubmissionDispatcherFragment()
        )

    private fun buildErrorDialog(exception: CwaWebException): DialogHelper.DialogInstance {
        return when (exception) {
            is BadRequestException -> DialogHelper.DialogInstance(
                requireActivity(),
                R.string.submission_qr_code_scan_invalid_dialog_headline,
                R.string.submission_qr_code_scan_invalid_dialog_body,
                R.string.submission_qr_code_scan_invalid_dialog_button_positive,
                R.string.submission_qr_code_scan_invalid_dialog_button_negative,
                true,
                { /* startDecode() */ },
                ::navigateToDispatchScreen
            )
            is CwaClientError, is CwaServerError -> DialogHelper.DialogInstance(
                requireActivity(),
                R.string.submission_error_dialog_web_generic_error_title,
                R.string.submission_error_dialog_web_generic_network_error_body,
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

    override fun onResume() {
        super.onResume()
        binding.contentContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }
}
