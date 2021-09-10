package de.rki.coronawarnapp.ui.submission.qrcode.scan

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.DefaultDecoderFactory
import de.rki.coronawarnapp.NavGraphDirections
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.coronatest.errors.AlreadyRedeemedException
import de.rki.coronawarnapp.databinding.FragmentScanQrCodeBinding
import de.rki.coronawarnapp.exception.http.BadRequestException
import de.rki.coronawarnapp.submission.TestRegistrationStateProcessor.State
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionNavigationEvents
import de.rki.coronawarnapp.util.DialogHelper
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.permission.CameraPermissionHelper
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import javax.inject.Inject

/**
 * A simple [Fragment] subclass.
 */
class SubmissionQRCodeScanFragment : Fragment(R.layout.fragment_scan_qr_code), AutoInject {
    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory

    private val args by navArgs<SubmissionQRCodeScanFragmentArgs>()

    private val viewModel: SubmissionQRCodeScanViewModel by cwaViewModelsAssisted(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, _ ->
            factory as SubmissionQRCodeScanViewModel.Factory
            factory.create(args.isConsentGiven)
        }
    )

    private val binding: FragmentScanQrCodeBinding by viewBinding()
    private var showsPermissionDialog = false

    @Suppress("ComplexMethod")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            qrCodeScanTorch.setOnCheckedChangeListener { _, isChecked ->
                qrCodeScanPreview.setTorch(isChecked)
            }

            qrCodeScanToolbar.setNavigationOnClickListener { viewModel.onClosePressed() }
            qrCodeScanPreview.decoderFactory = DefaultDecoderFactory(listOf(BarcodeFormat.QR_CODE))
        }

        viewModel.events.observe2(this) {
            when (it) {
                is SubmissionNavigationEvents.NavigateToDeletionWarningFragmentFromQrCode -> {
                    NavGraphDirections
                        .actionToSubmissionDeletionWarningFragment(
                            testRegistrationRequest = it.coronaTestQRCode,
                            isConsentGiven = it.consentGiven,
                        )
                        .run { doNavigate(this) }
                }
                is SubmissionNavigationEvents.NavigateToDispatcher -> navigateToDispatchScreen()
                is SubmissionNavigationEvents.NavigateToConsent -> goBack()
                is SubmissionNavigationEvents.NavigateToRequestDccFragment -> doNavigate(
                    NavGraphDirections.actionRequestCovidCertificateFragment(it.coronaTestQRCode, it.consentGiven)
                )
            }
        }

        viewModel.qrCodeErrorEvent.observe2(this) {
            showInvalidQrCodeDialog()
        }

        viewModel.registrationState.observe2(this) { state ->
            if (state is State.Working) {
                binding.qrCodeScanSpinner.show()
            } else {
                binding.qrCodeScanSpinner.hide()
            }
            when (state) {
                State.Idle,
                State.Working -> {
                    // Handled above
                }
                is State.Error -> {
                    when (state.exception) {
                        is BadRequestException -> showInvalidQrCodeDialog()
                        else -> {
                            state.getDialogBuilder(requireContext()).apply {
                                when (state.exception) {
                                    is AlreadyRedeemedException -> setOnDismissListener { goBack() }
                                    else -> setOnDismissListener { navigateToDispatchScreen() }
                                }
                            }.show()
                        }
                    }
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

    private fun startDecode() {
        binding.qrCodeScanPreview.decodeSingle {
            viewModel.registerCoronaTest(it.text)
        }
    }

    private fun navigateToDispatchScreen() = doNavigate(
        SubmissionQRCodeScanFragmentDirections.actionSubmissionQRCodeScanFragmentToSubmissionDispatcherFragment()
    )

    private fun showInvalidQrCodeDialog() {
        MaterialAlertDialogBuilder(requireContext()).apply {
            setTitle(R.string.submission_qr_code_scan_invalid_dialog_headline)
            setMessage(R.string.submission_qr_code_scan_invalid_dialog_body)
            setPositiveButton(R.string.submission_qr_code_scan_invalid_dialog_button_positive) { _, _ ->
                startDecode()
            }
            setNegativeButton(R.string.submission_qr_code_scan_invalid_dialog_button_negative) { _, _ ->
                viewModel.onBackPressed()
            }
            setOnCancelListener { viewModel.onBackPressed() }
        }.show()
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
        binding.qrCodeScanContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)

        if (CameraPermissionHelper.hasCameraPermission(requireActivity())) {
            binding.qrCodeScanPreview.resume()
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
        binding.qrCodeScanPreview.pause()
    }

    companion object {
        private const val REQUEST_CAMERA_PERMISSION_CODE = 1
    }
}
