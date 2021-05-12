package de.rki.coronawarnapp.vaccination.ui.scan

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent.TYPE_ANNOUNCEMENT
import androidx.fragment.app.Fragment
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.DefaultDecoderFactory
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.bugreporting.ui.toErrorDialogBuilder
import de.rki.coronawarnapp.databinding.FragmentScanQrCodeBinding
import de.rki.coronawarnapp.util.DialogHelper
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.permission.CameraPermissionHelper
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

class VaccinationQrCodeScanFragment :
    Fragment(R.layout.fragment_scan_qr_code),
    AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: VaccinationQrCodeScanViewModel by cwaViewModels { viewModelFactory }

    private val binding: FragmentScanQrCodeBinding by viewBindingLazy()
    private var showsPermissionDialog = false

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        with(binding) {
            qrCodeScanTorch.setOnCheckedChangeListener { _, isChecked ->
                binding.qrCodeScanPreview.setTorch(isChecked)
            }

            qrCodeScanToolbar.setNavigationOnClickListener { popBackStack() }
            qrCodeScanPreview.decoderFactory = DefaultDecoderFactory(listOf(BarcodeFormat.QR_CODE))
            qrCodeScanViewfinderView.setCameraPreview(binding.qrCodeScanPreview)
            qrCodeScanSpinner.hide()
        }

        viewModel.event.observe(viewLifecycleOwner) { event ->
            when (event) {
                is VaccinationQrCodeScanViewModel.Event.QrCodeScanSucceeded -> {
                    binding.qrCodeScanSpinner.hide()
                    doNavigate(
                        VaccinationQrCodeScanFragmentDirections
                            .actionVaccinationQrCodeScanFragmentToVaccinationDetailsFragment(event.certificateId)
                    )
                }
                VaccinationQrCodeScanViewModel.Event.QrCodeScanInProgress -> {
                    binding.qrCodeScanSpinner.show()
                }
            }
        }

        viewModel.errorEvent.observe(viewLifecycleOwner) {
            binding.qrCodeScanSpinner.hide()
            it.toErrorDialogBuilder(requireContext()).apply {
                setOnDismissListener { popBackStack() }
            }.show()
        }
    }

    override fun onResume() {
        super.onResume()
        binding.checkInQrCodeScanContainer.sendAccessibilityEvent(TYPE_ANNOUNCEMENT)
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
                leave()
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
            positiveButtonFunction = {
                showsPermissionDialog = false
                requestCameraPermission()
            },
            negativeButtonFunction = {
                leave()
            }
        )

        showsPermissionDialog = true
        DialogHelper.showDialog(cameraPermissionRationaleDialogInstance)
    }

    private fun requestCameraPermission() = requestPermissions(
        arrayOf(Manifest.permission.CAMERA),
        REQUEST_CAMERA_PERMISSION_CODE
    )

    private fun leave() {
        showsPermissionDialog = false
        popBackStack()
    }

    override fun onPause() {
        super.onPause()
        binding.qrCodeScanPreview.pause()
    }

    companion object {
        private const val REQUEST_CAMERA_PERMISSION_CODE = 4000
    }
}
