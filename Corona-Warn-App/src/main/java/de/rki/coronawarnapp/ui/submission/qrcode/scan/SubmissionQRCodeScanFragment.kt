package de.rki.coronawarnapp.ui.submission.qrcode.scan

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.DefaultDecoderFactory
import de.rki.coronawarnapp.NavGraphDirections
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.databinding.FragmentSubmissionQrCodeScanBinding
import de.rki.coronawarnapp.exception.http.BadRequestException
import de.rki.coronawarnapp.exception.http.CwaClientError
import de.rki.coronawarnapp.exception.http.CwaServerError
import de.rki.coronawarnapp.exception.http.CwaWebException
import de.rki.coronawarnapp.ui.submission.ApiRequestState
import de.rki.coronawarnapp.ui.submission.qrcode.QrCodeRegistrationStateProcessor
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionNavigationEvents
import de.rki.coronawarnapp.util.DialogHelper
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.permission.CameraPermissionHelper
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import javax.inject.Inject

/**
 * A simple [Fragment] subclass.
 */
class SubmissionQRCodeScanFragment : Fragment(R.layout.fragment_submission_qr_code_scan), AutoInject {
    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory

    private val args by navArgs<SubmissionQRCodeScanFragmentArgs>()

    private val viewModel: SubmissionQRCodeScanViewModel by cwaViewModelsAssisted(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, _ ->
            factory as SubmissionQRCodeScanViewModel.Factory
            factory.create(args.isConsentGiven)
        }
    )

    private val binding: FragmentSubmissionQrCodeScanBinding by viewBindingLazy()
    private var showsPermissionDialog = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            submissionQrCodeScanTorch.setOnCheckedChangeListener { _, isChecked ->
                binding.submissionQrCodeScanPreview.setTorch(
                    isChecked
                )
            }

            submissionQrCodeScanToolbar.setNavigationOnClickListener {
                viewModel.onClosePressed()
            }

            submissionQrCodeScanPreview.decoderFactory =
                DefaultDecoderFactory(listOf(BarcodeFormat.QR_CODE))

            submissionQrCodeScanViewfinderView.setCameraPreview(binding.submissionQrCodeScanPreview)
        }

        viewModel.routeToScreen.observe2(this) {
            when (it) {
                is SubmissionNavigationEvents.NavigateToDeletionWarningFragmentFromQrCode -> {
                    doNavigate(
                        NavGraphDirections
                            .actionToSubmissionDeletionWarningFragment(
                                it.consentGiven,
                                it.coronaTestQRCode
                            )
                    )
                }
                is SubmissionNavigationEvents.NavigateToDispatcher ->
                    navigateToDispatchScreen()
                is SubmissionNavigationEvents.NavigateToConsent ->
                    goBack()
            }
        }

        viewModel.qrCodeValidationState.observe2(this) {
            if (QrCodeRegistrationStateProcessor.ValidationState.INVALID == it) {
                showInvalidScanDialog()
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
            goBack()
        }

        viewModel.registrationState.observe2(this) { state ->
            binding.submissionQrCodeScanSpinner.visibility = when (state.apiRequestState) {
                ApiRequestState.STARTED -> View.VISIBLE
                else -> View.GONE
            }
            if (ApiRequestState.SUCCESS == state.apiRequestState) {
                when (state.test?.testResult) {
                    CoronaTestResult.PCR_POSITIVE ->
                        doNavigate(
                            NavGraphDirections
                                .actionToSubmissionTestResultAvailableFragment(testType = CoronaTest.Type.PCR)
                        )
                    CoronaTestResult.PCR_OR_RAT_PENDING -> {
                        if (state.test.type == CoronaTest.Type.RAPID_ANTIGEN) {
                            doNavigate(
                                NavGraphDirections
                                    .actionSubmissionTestResultPendingFragment(
                                        testType = CoronaTest.Type.RAPID_ANTIGEN
                                    )
                            )
                        } else {
                            doNavigate(
                                NavGraphDirections
                                    .actionSubmissionTestResultPendingFragment(
                                        testType = CoronaTest.Type.PCR
                                    )
                            )
                        }
                    }
                    CoronaTestResult.PCR_NEGATIVE,
                    CoronaTestResult.PCR_INVALID,
                    CoronaTestResult.PCR_REDEEMED ->
                        doNavigate(
                            NavGraphDirections
                                .actionSubmissionTestResultPendingFragment(
                                    testType = CoronaTest.Type.PCR
                                )
                        )
                    CoronaTestResult.RAT_POSITIVE ->
                        doNavigate(
                            NavGraphDirections
                                .actionToSubmissionTestResultAvailableFragment(
                                    testType = CoronaTest.Type.RAPID_ANTIGEN
                                )
                        )
                    CoronaTestResult.RAT_NEGATIVE,
                    CoronaTestResult.RAT_INVALID,
                    CoronaTestResult.RAT_PENDING,
                    CoronaTestResult.RAT_REDEEMED ->
                        doNavigate(
                            NavGraphDirections
                                .actionSubmissionTestResultPendingFragment(
                                    testType = CoronaTest.Type.RAPID_ANTIGEN
                                )
                        )
                }
            }
        }

        viewModel.registrationError.observe2(this) {
            DialogHelper.showDialog(buildErrorDialog(it))
        }
    }

    private fun startDecode() {
        binding.submissionQrCodeScanPreview.decodeSingle {
            viewModel.onQrCodeAvailable(it.text)
        }
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
                { startDecode() },
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

    private fun navigateToDispatchScreen() =
        doNavigate(
            SubmissionQRCodeScanFragmentDirections.actionSubmissionQRCodeScanFragmentToSubmissionDispatcherFragment()
        )

    private fun showInvalidScanDialog() {
        val invalidScanDialogInstance = DialogHelper.DialogInstance(
            requireActivity(),
            R.string.submission_qr_code_scan_invalid_dialog_headline,
            R.string.submission_qr_code_scan_invalid_dialog_body,
            R.string.submission_qr_code_scan_invalid_dialog_button_positive,
            R.string.submission_qr_code_scan_invalid_dialog_button_negative,
            true,
            ::startDecode,
            ::navigateToDispatchScreen
        )

        DialogHelper.showDialog(invalidScanDialogInstance)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        // if permission was denied
        if (requestCode == REQUEST_CAMERA_PERMISSION_CODE &&
            (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_DENIED)
        ) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                showCameraPermissionRationaleDialog()
                viewModel.setCameraDeniedPermanently(false)
            } else {
                // user permanently denied access to the camera
                showCameraPermissionDeniedDialog()
                viewModel.setCameraDeniedPermanently(true)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.submissionQrCodeScanContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)

        if (CameraPermissionHelper.hasCameraPermission(requireActivity())) {
            binding.submissionQrCodeScanPreview.resume()
            startDecode()
            return
        }

        // we might already show a rational dialog (e.g. when onRequestPermissionsResult was denied
        // then do nothing
        if (showsPermissionDialog) {
            return
        }

        requestCameraPermission()
    }

    private fun showCameraPermissionDeniedDialog() {
        val permissionDeniedDialog = DialogHelper.DialogInstance(
            requireActivity(),
            R.string.submission_qr_code_scan_permission_denied_dialog_headline,
            R.string.submission_qr_code_scan_permission_denied_dialog_body,
            R.string.submission_qr_code_scan_permission_denied_dialog_button,
            cancelable = false,
            positiveButtonFunction = {
                showsPermissionDialog = false
                viewModel.onBackPressed()
            }
        )
        showsPermissionDialog = true
        DialogHelper.showDialog(permissionDeniedDialog)
    }

    private fun showCameraPermissionRationaleDialog() {
        val cameraPermissionRationaleDialogInstance = DialogHelper.DialogInstance(
            requireActivity(),
            R.string.submission_qr_code_scan_permission_rationale_dialog_headline,
            R.string.submission_qr_code_scan_permission_rationale_dialog_body,
            R.string.submission_qr_code_scan_permission_rationale_dialog_button_positive,
            R.string.submission_qr_code_scan_permission_rationale_dialog_button_negative,
            false,
            {
                showsPermissionDialog = false
                requestCameraPermission()
            },
            {
                showsPermissionDialog = false
                viewModel.onBackPressed()
            }
        )

        showsPermissionDialog = true
        DialogHelper.showDialog(cameraPermissionRationaleDialogInstance)
    }

    private fun goBack() = popBackStack()

    private fun requestCameraPermission() = requestPermissions(
        arrayOf(Manifest.permission.CAMERA),
        REQUEST_CAMERA_PERMISSION_CODE
    )

    override fun onPause() {
        super.onPause()
        binding.submissionQrCodeScanPreview.pause()
    }

    companion object {
        private const val REQUEST_CAMERA_PERMISSION_CODE = 1
    }
}
