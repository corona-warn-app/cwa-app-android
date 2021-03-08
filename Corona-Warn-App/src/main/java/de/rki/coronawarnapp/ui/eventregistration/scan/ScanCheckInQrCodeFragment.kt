package de.rki.coronawarnapp.ui.eventregistration.scan

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.view.accessibility.AccessibilityEvent.TYPE_ANNOUNCEMENT
import androidx.core.net.toUri
import androidx.navigation.fragment.findNavController
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.DefaultDecoderFactory
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentScanCheckInQrCodeBinding
import de.rki.coronawarnapp.util.CameraPermissionHelper
import de.rki.coronawarnapp.util.DialogHelper
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.navUri
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

class ScanCheckInQrCodeFragment :
    Fragment(R.layout.fragment_scan_check_in_qr_code),
    AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: ScanCheckInQrCodeViewModel by cwaViewModels { viewModelFactory }

    private val binding: FragmentScanCheckInQrCodeBinding by viewBindingLazy()
    private var showsPermissionDialog = false

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        with(binding) {
            checkInQrCodeScanTorch.setOnCheckedChangeListener { _, isChecked ->
                binding.checkInQrCodeScanPreview.setTorch(isChecked)
            }
            checkInQrCodeScanClose.setOnClickListener { viewModel.onNavigateUp() }
            checkInQrCodeScanPreview.decoderFactory = DefaultDecoderFactory(listOf(BarcodeFormat.QR_CODE))
            checkInQrCodeScanViewfinderView.setCameraPreview(binding.checkInQrCodeScanPreview)
        }

        viewModel.navigationEvents.observe2(this) { navEvent ->
            when (navEvent) {
                is ScanCheckInQrCodeEvent.BackEvent -> popBackStack()
                is ScanCheckInQrCodeEvent.ConfirmCheckInEvent -> findNavController().navigate(
                    navEvent.url.toUri().navUri
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.checkInQrCodeScanContainer.sendAccessibilityEvent(TYPE_ANNOUNCEMENT)
        if (CameraPermissionHelper.hasCameraPermission(requireActivity())) {
            binding.checkInQrCodeScanPreview.resume()
            startDecode()
            return
        }
        if (showsPermissionDialog) return

        requestCameraPermission()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CAMERA_PERMISSION_CODE &&
            grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_DENIED
        ) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                showCameraPermissionRationaleDialog()
            } else {
                // User permanently denied access to the camera
                showCameraPermissionDeniedDialog()
            }
        }
    }

    private fun startDecode() = binding.checkInQrCodeScanPreview
        .decodeSingle { barcodeResult ->
            viewModel.onScanResult(barcodeResult)
        }

    private fun showCameraPermissionDeniedDialog() {
        val permissionDeniedDialog = DialogHelper.DialogInstance(
            requireActivity(),
            // TODO use strings for this screen
            R.string.submission_qr_code_scan_permission_denied_dialog_headline,
            R.string.submission_qr_code_scan_permission_denied_dialog_body,
            R.string.submission_qr_code_scan_permission_denied_dialog_button,
            cancelable = false,
            positiveButtonFunction = {
                showsPermissionDialog = false
                viewModel.onNavigateUp()
            }
        )
        showsPermissionDialog = true
        DialogHelper.showDialog(permissionDeniedDialog)
    }

    private fun showCameraPermissionRationaleDialog() {
        val cameraPermissionRationaleDialogInstance = DialogHelper.DialogInstance(
            requireActivity(),
            // TODO use strings for this screen
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
                viewModel.onNavigateUp()
            }
        )

        showsPermissionDialog = true
        DialogHelper.showDialog(cameraPermissionRationaleDialogInstance)
    }

    private fun requestCameraPermission() = requestPermissions(
        arrayOf(Manifest.permission.CAMERA),
        REQUEST_CAMERA_PERMISSION_CODE
    )

    override fun onPause() {
        super.onPause()
        binding.checkInQrCodeScanPreview.pause()
    }

    companion object {
        private const val REQUEST_CAMERA_PERMISSION_CODE = 4000
    }
}
