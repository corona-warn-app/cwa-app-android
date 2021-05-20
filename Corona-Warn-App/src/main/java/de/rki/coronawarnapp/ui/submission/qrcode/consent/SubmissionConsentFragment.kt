package de.rki.coronawarnapp.ui.submission.qrcode.consent

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import de.rki.coronawarnapp.NavGraphDirections
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.databinding.FragmentSubmissionConsentBinding
import de.rki.coronawarnapp.exception.http.BadRequestException
import de.rki.coronawarnapp.exception.http.CwaClientError
import de.rki.coronawarnapp.exception.http.CwaServerError
import de.rki.coronawarnapp.exception.http.CwaWebException
import de.rki.coronawarnapp.ui.submission.ApiRequestState
import de.rki.coronawarnapp.ui.submission.qrcode.QrCodeRegistrationStateProcessor.ValidationState
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionNavigationEvents
import de.rki.coronawarnapp.util.DialogHelper
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

class SubmissionConsentFragment : Fragment(R.layout.fragment_submission_consent), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: SubmissionConsentViewModel by cwaViewModels { viewModelFactory }
    private val binding: FragmentSubmissionConsentBinding by viewBinding()
    private val navArgs by navArgs<SubmissionConsentFragmentArgs>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel
        navArgs.qrCode?.let {
            viewModel.qrCode = it
        }
        binding.submissionConsentHeader.headerButtonBack.buttonIcon.setOnClickListener {
            viewModel.onBackButtonClick()
        }
        viewModel.routeToScreen.observe2(this) {
            when (it) {
                is SubmissionNavigationEvents.NavigateToQRCodeScan ->
                    doNavigate(
                        SubmissionConsentFragmentDirections
                            .actionSubmissionConsentFragmentToSubmissionQRCodeScanFragment(isConsentGiven = true)
                    )
                is SubmissionNavigationEvents.NavigateToDispatcher -> popBackStack()
                is SubmissionNavigationEvents.NavigateToDataPrivacy -> doNavigate(
                    SubmissionConsentFragmentDirections.actionSubmissionConsentFragmentToInformationPrivacyFragment()
                )
                is SubmissionNavigationEvents.ResolvePlayServicesException ->
                    it.exception.status.startResolutionForResult(
                        requireActivity(),
                        REQUEST_USER_RESOLUTION
                    )
                is SubmissionNavigationEvents.NavigateToDeletionWarningFragmentFromQrCode -> {
                    doNavigate(
                        NavGraphDirections
                            .actionToSubmissionDeletionWarningFragment(
                                it.consentGiven,
                                it.coronaTestQRCode
                            )
                    )
                }
            }
        }
        viewModel.countries.observe2(this) {
            binding.countries = it
        }

        viewModel.showRedeemedTokenWarning.observe2(this) {
            val dialog = DialogHelper.DialogInstance(
                requireActivity(),
                R.string.submission_error_dialog_web_tan_redeemed_title,
                R.string.submission_error_dialog_web_tan_redeemed_body,
                R.string.submission_error_dialog_web_tan_redeemed_button_positive
            )

            DialogHelper.showDialog(dialog)
            popBackStack()
        }

        viewModel.qrCodeValidationState.observe2(this) {
            if (ValidationState.INVALID == it) {
                showInvalidQrCodeDialog()
            }
        }

        viewModel.registrationState.observe2(this) { state ->
            binding.progressSpinner.isVisible = state.apiRequestState == ApiRequestState.STARTED
            binding.submissionConsentButton.isEnabled = when (state.apiRequestState) {
                ApiRequestState.STARTED -> false
                else -> true
            }

            if (ApiRequestState.SUCCESS == state.apiRequestState) {
                when (state.test?.type) {
                    CoronaTest.Type.PCR -> throw UnsupportedOperationException()
                    CoronaTest.Type.RAPID_ANTIGEN -> {
                        when {
                            state.test.isPositive ->
                                doNavigate(
                                    NavGraphDirections.actionToSubmissionTestResultAvailableFragment(
                                        CoronaTest.Type.RAPID_ANTIGEN
                                    )
                                )
                            else -> doNavigate(
                                NavGraphDirections.actionSubmissionTestResultPendingFragment(
                                    testType = CoronaTest.Type.RAPID_ANTIGEN
                                )
                            )
                        }
                    }
                }
            }
        }

        viewModel.registrationError.observe2(this) {
            DialogHelper.showDialog(buildErrorDialog(it))
        }
    }

    override fun onResume() {
        super.onResume()
        binding.contentContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_USER_RESOLUTION) {
            viewModel.giveGoogleConsentResult(resultCode == Activity.RESULT_OK)
        }
    }

    private fun showInvalidQrCodeDialog() {
        val invalidScanDialogInstance = DialogHelper.DialogInstance(
            requireActivity(),
            R.string.submission_qr_code_scan_invalid_dialog_headline,
            R.string.submission_qr_code_scan_invalid_dialog_body,
            R.string.submission_qr_code_scan_invalid_dialog_button_positive,
            R.string.submission_qr_code_scan_invalid_dialog_button_negative,
            true,
            positiveButtonFunction = {},
            negativeButtonFunction = ::navigateHome
        )

        DialogHelper.showDialog(invalidScanDialogInstance)
    }

    private fun buildErrorDialog(exception: CwaWebException): DialogHelper.DialogInstance {
        return when (exception) {
            is BadRequestException -> DialogHelper.DialogInstance(
                requireActivity(),
                R.string.submission_qr_code_scan_invalid_dialog_headline,
                R.string.submission_qr_code_scan_invalid_dialog_body,
                R.string.submission_qr_code_scan_invalid_dialog_button_positive,
                R.string.submission_qr_code_scan_invalid_dialog_button_negative,
                true,
                { },
                ::navigateHome
            )
            is CwaClientError, is CwaServerError -> DialogHelper.DialogInstance(
                requireActivity(),
                R.string.submission_error_dialog_web_generic_error_title,
                R.string.submission_error_dialog_web_generic_network_error_body,
                R.string.submission_error_dialog_web_generic_error_button_positive,
                null,
                true,
                ::navigateHome
            )
            else -> DialogHelper.DialogInstance(
                requireActivity(),
                R.string.submission_error_dialog_web_generic_error_title,
                R.string.submission_error_dialog_web_generic_error_body,
                R.string.submission_error_dialog_web_generic_error_button_positive,
                null,
                true,
                ::navigateHome
            )
        }
    }

    private fun navigateHome() {
        popBackStack()
    }

    companion object {
        private const val REQUEST_USER_RESOLUTION = 3000
        fun canHandle(rootUri: String): Boolean = rootUri.startsWith("https://s.coronawarn.app")
    }
}
