package de.rki.coronawarnapp.ui.submission.qrcode.consent

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
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
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import javax.inject.Inject

class SubmissionConsentFragment : Fragment(R.layout.fragment_submission_consent), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val navArgs by navArgs<SubmissionConsentFragmentArgs>()
    private val viewModel: SubmissionConsentViewModel by cwaViewModelsAssisted(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, _ ->
            factory as SubmissionConsentViewModel.Factory
            factory.create(navArgs.coronaTestQrCode, navArgs.allowTestReplacement)
        }
    )
    private val binding: FragmentSubmissionConsentBinding by viewBinding()
    private val navOptions = NavOptions.Builder().setPopUpTo(R.id.submissionConsentFragment, true).build()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel
        binding.submissionConsentHeader.setNavigationOnClickListener { popBackStack() }
        viewModel.routeToScreen.observe2(this) {
            when (it) {
                is SubmissionNavigationEvents.NavigateToDataPrivacy -> doNavigate(
                    SubmissionConsentFragmentDirections.actionSubmissionConsentFragmentToInformationPrivacyFragment()
                )
                is SubmissionNavigationEvents.ResolvePlayServicesException ->
                    it.exception.status.startResolutionForResult(
                        requireActivity(),
                        REQUEST_USER_RESOLUTION
                    )

                is SubmissionNavigationEvents.NavigateToRequestDccFragment -> findNavController().navigate(
                    NavGraphDirections.actionRequestCovidCertificateFragment(
                        testRegistrationRequest = it.coronaTestQRCode,
                        coronaTestConsent = it.consentGiven,
                        allowTestReplacement = it.allowReplacement
                    ),
                    navOptions
                )
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
                    val dialog = state.getDialogBuilder(requireContext())
                    dialog.setPositiveButton(android.R.string.ok) { _, _ -> popBackStack() }
                    dialog.show()
                }
                is State.TestRegistered -> when {
                    state.test.isPositive ->
                        NavGraphDirections.actionToSubmissionTestResultAvailableFragment(testType = state.test.type)
                            .run { findNavController().navigate(this, navOptions) }

                    else ->
                        NavGraphDirections.actionSubmissionTestResultPendingFragment(
                            testType = state.test.type,
                            testIdentifier = state.test.identifier
                        )
                            .run { findNavController().navigate(this, navOptions) }
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
