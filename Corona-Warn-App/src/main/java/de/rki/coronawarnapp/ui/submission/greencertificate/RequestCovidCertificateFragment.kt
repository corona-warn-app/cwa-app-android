package de.rki.coronawarnapp.ui.submission.greencertificate

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.rki.coronawarnapp.NavGraphDirections
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.bugreporting.ui.toErrorDialogBuilder
import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.coronatest.type.CoronaTest.Type.PCR
import de.rki.coronawarnapp.coronatest.type.CoronaTest.Type.RAPID_ANTIGEN
import de.rki.coronawarnapp.databinding.FragmentRequestCovidCertificateBinding
import de.rki.coronawarnapp.exception.http.BadRequestException
import de.rki.coronawarnapp.exception.http.CwaClientError
import de.rki.coronawarnapp.exception.http.CwaServerError
import de.rki.coronawarnapp.exception.http.CwaWebException
import de.rki.coronawarnapp.ui.submission.ApiRequestState
import de.rki.coronawarnapp.ui.submission.qrcode.QrCodeRegistrationStateProcessor
import de.rki.coronawarnapp.util.DialogHelper
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toDayFormat
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import org.joda.time.LocalDate
import timber.log.Timber
import javax.inject.Inject

class RequestCovidCertificateFragment : Fragment(R.layout.fragment_request_covid_certificate), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel by cwaViewModelsAssisted<RequestCovidCertificateViewModel>(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, _ ->
            factory as RequestCovidCertificateViewModel.Factory
            factory.create(args.coronaTestQrCode, args.coronaTestConsent, args.deleteOldTest)
        }
    )
    private val binding by viewBinding<FragmentRequestCovidCertificateBinding>()
    private val args by navArgs<RequestCovidCertificateFragmentArgs>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) =
        with(binding) {
            val isPCR = args.coronaTestQrCode is CoronaTestQRCode.PCR
            birthDateGroup.isVisible = isPCR
            privacyCard.pcrExtraBullet.isVisible = isPCR

            dateInputEdit.doOnTextChanged { text, _, _, _ ->
                if (text.toString().isEmpty()) viewModel.birthDateChanged(null)
            }

            toolbar.setNavigationOnClickListener { showCloseDialog() }
            agreeButton.setOnClickListener { viewModel.onAgreeGC() }
            disagreeButton.setOnClickListener { viewModel.onDisagreeGC() }
            dateInputEdit.setOnClickListener { openDatePicker() }
            privacyInformation.setOnClickListener { findNavController().navigate(R.id.informationPrivacyFragment) }

            viewModel.events.observe(viewLifecycleOwner) { event ->
                when (event) {
                    Back -> popBackStack()
                    ToDispatcherScreen -> doNavigate(
                        RequestCovidCertificateFragmentDirections
                            .actionRequestCovidCertificateFragmentToDispatcherFragment()
                    )
                    ToHomeScreen -> doNavigate(
                        RequestCovidCertificateFragmentDirections.actionRequestCovidCertificateFragmentToHomeFragment()
                    )
                }
            }
            viewModel.birthDate.observe(viewLifecycleOwner) { date -> agreeButton.isEnabled = !isPCR || date != null }
            viewModel.registrationError.observe(viewLifecycleOwner) { DialogHelper.showDialog(buildErrorDialog(it)) }
            viewModel.registrationState.observe(viewLifecycleOwner) { state -> handleRegistrationState(state) }
            viewModel.showRedeemedTokenWarning.observe(viewLifecycleOwner) { DialogHelper.showDialog(redeemDialog()) }
            viewModel.removalError.observe(viewLifecycleOwner) { it.toErrorDialogBuilder(requireContext()).show() }
        }

    private fun handleRegistrationState(state: QrCodeRegistrationStateProcessor.RegistrationState) {
        when (state.apiRequestState) {
            ApiRequestState.STARTED -> binding.apply {
                progressBar.show()
                agreeButton.isInvisible = true
                disagreeButton.isInvisible = true
            }
            else -> binding.apply {
                progressBar.hide()
                agreeButton.isInvisible = false
                disagreeButton.isInvisible = false
            }
        }

        when (state.test?.testResult) {
            CoronaTestResult.PCR_POSITIVE ->
                NavGraphDirections.actionToSubmissionTestResultAvailableFragment(testType = PCR)

            CoronaTestResult.PCR_OR_RAT_PENDING ->
                NavGraphDirections.actionSubmissionTestResultPendingFragment(testType = state.test.type)

            CoronaTestResult.PCR_NEGATIVE,
            CoronaTestResult.PCR_INVALID,
            CoronaTestResult.PCR_REDEEMED ->
                NavGraphDirections.actionSubmissionTestResultPendingFragment(testType = PCR)

            CoronaTestResult.RAT_POSITIVE ->
                NavGraphDirections.actionToSubmissionTestResultAvailableFragment(testType = RAPID_ANTIGEN)

            CoronaTestResult.RAT_NEGATIVE,
            CoronaTestResult.RAT_INVALID,
            CoronaTestResult.RAT_PENDING,
            CoronaTestResult.RAT_REDEEMED ->
                NavGraphDirections.actionSubmissionTestResultPendingFragment(testType = RAPID_ANTIGEN)
            null -> {
                Timber.w("Successful API request, but test was null?")
                return
            }
        }.run { doNavigate(this) }
    }

    private fun redeemDialog(): DialogHelper.DialogInstance = DialogHelper.DialogInstance(
        requireActivity(),
        R.string.submission_error_dialog_web_tan_redeemed_title,
        R.string.submission_error_dialog_web_tan_redeemed_body,
        R.string.submission_error_dialog_web_tan_redeemed_button_positive
    )

    private fun showCloseDialog() = MaterialAlertDialogBuilder(requireContext())
        .setTitle(R.string.request_gc_dialog_title)
        .setMessage(R.string.request_gc_dialog_message)
        .setNegativeButton(R.string.request_gc_dialog_negative_button) { _, _ -> viewModel.navigateBack() }
        .setPositiveButton(R.string.request_gc_dialog_positive_button) { _, _ -> viewModel.navigateToHomeScreen() }
        .create()
        .show()

    private fun openDatePicker() = MaterialDatePicker.Builder
        .datePicker()
        .build()
        .apply {
            addOnPositiveButtonClickListener { timestamp ->
                val localDate = LocalDate(timestamp)
                binding.dateInputEdit.setText(localDate.toDayFormat())
                viewModel.birthDateChanged(localDate)
            }
        }
        .show(childFragmentManager, "RequestGreenCertificateFragment.MaterialDatePicker")

    private fun buildErrorDialog(exception: CwaWebException): DialogHelper.DialogInstance =
        when (exception) {
            is BadRequestException -> createInvalidScanDialog()
            is CwaClientError, is CwaServerError -> DialogHelper.DialogInstance(
                requireActivity(),
                R.string.submission_error_dialog_web_generic_error_title,
                R.string.submission_error_dialog_web_generic_network_error_body,
                R.string.submission_error_dialog_web_generic_error_button_positive,
                null,
                true,
                { viewModel.navigateToDispatcherScreen() }
            )
            else -> DialogHelper.DialogInstance(
                requireActivity(),
                R.string.submission_error_dialog_web_generic_error_title,
                R.string.submission_error_dialog_web_generic_error_body,
                R.string.submission_error_dialog_web_generic_error_button_positive,
                null,
                true,
                { viewModel.navigateToDispatcherScreen() }
            )
        }

    private fun createInvalidScanDialog() = DialogHelper.DialogInstance(
        requireActivity(),
        R.string.submission_qr_code_scan_invalid_dialog_headline,
        R.string.submission_qr_code_scan_invalid_dialog_body,
        R.string.submission_qr_code_scan_invalid_dialog_button_positive,
        R.string.submission_qr_code_scan_invalid_dialog_button_negative,
        true,
        { viewModel.navigateBack() },
        { viewModel.navigateToDispatcherScreen() },
        { viewModel.navigateToDispatcherScreen() }
    )
}
