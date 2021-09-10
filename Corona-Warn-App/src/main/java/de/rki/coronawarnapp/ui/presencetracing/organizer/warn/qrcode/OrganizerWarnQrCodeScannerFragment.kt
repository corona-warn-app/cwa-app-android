package de.rki.coronawarnapp.ui.presencetracing.organizer.warn.qrcode

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.DefaultDecoderFactory
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentScanQrCodeBinding
import de.rki.coronawarnapp.util.DialogHelper
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.permission.CameraPermissionHelper
import de.rki.coronawarnapp.util.ui.LazyString
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

class OrganizerWarnQrCodeScannerFragment :
    Fragment(R.layout.fragment_scan_qr_code),
    AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: OrganizerWarnQrCodeScannerViewModel by cwaViewModels { viewModelFactory }

    private val binding: FragmentScanQrCodeBinding by viewBinding()
    private var showsPermissionDialog = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        with(binding) {
            qrCodeScanTorch.setOnCheckedChangeListener { _, isChecked ->
                binding.qrCodeScanPreview.setTorch(isChecked)
            }

            qrCodeScanToolbar.setNavigationOnClickListener { viewModel.onNavigateUp() }
            qrCodeScanPreview.decoderFactory = DefaultDecoderFactory(listOf(BarcodeFormat.QR_CODE))
            qrCodeScanSubtitle.setText(R.string.qr_code_scan_body_subtitle_vertretung_warnen)
        }

        viewModel.events.observe2(this) { navEvent ->
            when (navEvent) {
                is OrganizerWarnQrCodeNavigation.BackNavigation -> popBackStack()
                is OrganizerWarnQrCodeNavigation.InvalidQrCode -> showInvalidQrCodeInformation(navEvent.errorText)
                is OrganizerWarnQrCodeNavigation.DurationSelectionScreen -> {
                    doNavigate(
                        OrganizerWarnQrCodeScannerFragmentDirections
                            .actionTraceLocationQrScannerFragmentToTraceLocationWarnDurationFragment(
                                navEvent.traceLocation
                            )
                    )
                }
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
                viewModel.setCameraDeniedPermanently(false)
            } else {
                // User permanently denied access to the camera
                showCameraPermissionDeniedDialog()
                viewModel.setCameraDeniedPermanently(true)
            }
        }
    }

    private fun startDecode() = binding.qrCodeScanPreview
        .decodeSingle { barcodeResult ->
            viewModel.onScanResult(barcodeResult)
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
                viewModel.onNavigateUp()
            }
        )
        showsPermissionDialog = true
        DialogHelper.showDialog(permissionDeniedDialog)
    }

    private fun showInvalidQrCodeInformation(lazyErrorText: LazyString) {
        MaterialAlertDialogBuilder(requireContext()).apply {
            val errorText = lazyErrorText.get(context)
            setTitle(R.string.trace_location_attendee_invalid_qr_code_dialog_title)
            setMessage(getString(R.string.trace_location_attendee_invalid_qr_code_dialog_message, errorText))
            setPositiveButton(R.string.trace_location_attendee_invalid_qr_code_dialog_positive_button) { _, _ ->
                startDecode()
            }
            setNegativeButton(R.string.trace_location_attendee_invalid_qr_code_dialog_negative_button) { _, _ ->
                popBackStack()
            }
        }.show()
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
        binding.qrCodeScanPreview.pause()
    }

    companion object {
        private const val REQUEST_CAMERA_PERMISSION_CODE = 4000
    }
}
