package de.rki.coronawarnapp.ui.submission.qrcode.scan

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.DefaultDecoderFactory
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSubmissionQrCodeScanBinding
import de.rki.coronawarnapp.exception.http.BadRequestException
import de.rki.coronawarnapp.exception.http.CwaClientError
import de.rki.coronawarnapp.exception.http.CwaServerError
import de.rki.coronawarnapp.exception.http.CwaWebException
import de.rki.coronawarnapp.ui.main.MainActivity
import de.rki.coronawarnapp.ui.submission.ApiRequestState
import de.rki.coronawarnapp.ui.submission.ScanStatus
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionNavigationEvents
import de.rki.coronawarnapp.util.CameraPermissionHelper
import de.rki.coronawarnapp.util.DialogHelper
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

/**
 * A simple [Fragment] subclass.
 */
class SubmissionQRCodeScanFragment : Fragment(R.layout.fragment_submission_qr_code_scan),
    AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: SubmissionQRCodeScanViewModel by cwaViewModels { viewModelFactory }

    private val binding: FragmentSubmissionQrCodeScanBinding by viewBindingLazy()
    private var showsPermissionDialog = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.submissionQrCodeScanTorch.setOnCheckedChangeListener { _, isChecked ->
            binding.submissionQrCodeScanPreview.setTorch(
                isChecked
            )
        }

        binding.submissionQrCodeScanClose.setOnClickListener {
            viewModel.onClosePressed()
        }

        binding.submissionQrCodeScanPreview.decoderFactory =
            DefaultDecoderFactory(listOf(BarcodeFormat.QR_CODE))

        binding.submissionQrCodeScanViewfinderView.setCameraPreview(binding.submissionQrCodeScanPreview)

        viewModel.scanStatusValue.observe2(this) {
            if (ScanStatus.INVALID == it) {
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

        viewModel.registrationState.observe2(this) {
            binding.submissionQrCodeScanSpinner.visibility = when (it) {
                ApiRequestState.STARTED -> View.VISIBLE
                else -> View.GONE
            }
            if (ApiRequestState.SUCCESS == it) {
                doNavigate(
                    SubmissionQRCodeScanFragmentDirections
                        .actionSubmissionQRCodeScanFragmentToSubmissionResultFragment()
                )
            }
        }

        viewModel.registrationError.observe2(this) {
            DialogHelper.showDialog(buildErrorDialog(it))
        }

        viewModel.routeToScreen.observe2(this) {
            when (it) {
                is SubmissionNavigationEvents.NavigateToDispatcher ->
                    navigateToDispatchScreen()
                is SubmissionNavigationEvents.NavigateToConsent ->
                    goBack()
            }
        }
    }

    private fun startDecode() {
        binding.submissionQrCodeScanPreview.decodeSingle {
            viewModel.validateTestGUID(it.text)
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
                getString(
                    R.string.submission_error_dialog_web_generic_network_error_body,
                    exception.statusCode
                ),
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
            } else {
                // user permanently denied access to the camera
                showCameraPermissionDeniedDialog()
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

    private fun goBack() = (activity as MainActivity).goBack()

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
