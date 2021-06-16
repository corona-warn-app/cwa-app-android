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
import de.rki.coronawarnapp.databinding.FragmentSubmissionConsentBinding
import de.rki.coronawarnapp.submission.TestRegistrationStateProcessor.State
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
                                testRegistrationRequest = it.coronaTestQRCode,
                                isConsentGiven = it.consentGiven,
                            )
                    )
                }
            }
        }
        viewModel.countries.observe2(this) {
            binding.countries = it
        }

        viewModel.qrCodeError.observe2(this) {
            showInvalidQrCodeDialog()
        }

        viewModel.registrationState.observe2(this) { state ->
            val isWorking = state is State.Working
            binding.apply {
                progressSpinner.isVisible = isWorking
                submissionConsentButton.isEnabled = !isWorking
            }
            when (state) {
                State.Idle,
                State.Working -> {
                    // Handled above
                }
                is State.Error -> {
                    state.getDialogBuilder(requireContext()).show()
                    popBackStack()
                }
                is State.TestRegistered -> when {
                    state.test.isPositive ->
                        NavGraphDirections.actionToSubmissionTestResultAvailableFragment(testType = state.test.type)
                            .run { doNavigate(this) }

                    else ->
                        NavGraphDirections.actionSubmissionTestResultPendingFragment(testType = state.test.type)
                            .run { doNavigate(this) }
                }
            }
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
            negativeButtonFunction = {
                popBackStack()
                Unit
            }
        )

        DialogHelper.showDialog(invalidScanDialogInstance)
    }

    companion object {
        private const val REQUEST_USER_RESOLUTION = 3000
        fun canHandle(rootUri: String): Boolean = rootUri.startsWith("https://s.coronawarn.app")
    }
}
