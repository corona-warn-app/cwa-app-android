package de.rki.coronawarnapp.covidcertificate.common.scan

import android.Manifest
import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent.TYPE_ANNOUNCEMENT
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.DefaultDecoderFactory
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.bugreporting.ui.toErrorDialogBuilder
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException
import de.rki.coronawarnapp.databinding.FragmentScanQrCodeBinding
import de.rki.coronawarnapp.util.DialogHelper
import de.rki.coronawarnapp.util.ExternalActionHelper.openUrl
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.permission.CameraPermissionHelper
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

class DccQrCodeScanFragment :
    Fragment(R.layout.fragment_scan_qr_code),
    AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: DccQrCodeScanViewModel by cwaViewModels { viewModelFactory }

    private val binding: FragmentScanQrCodeBinding by viewBinding()
    private var showsPermissionDialog = false

    val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (!isGranted) {
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
                is DccQrCodeScanViewModel.Event.VaccinationQrCodeScanSucceeded -> {
                    binding.qrCodeScanSpinner.hide()
                    doNavigate(
                        DccQrCodeScanFragmentDirections
                            .actionDccQrCodeScanFragmentToVaccinationDetailsFragment(
                                event.certificateId
                            )
                    )
                }
                is DccQrCodeScanViewModel.Event.RecoveryQrCodeScanSucceeded -> { // TODO
                }
                is DccQrCodeScanViewModel.Event.TestQrCodeScanSucceeded -> {
                    binding.qrCodeScanSpinner.hide()
                    doNavigate(
                        DccQrCodeScanFragmentDirections
                            .actionDccQrCodeScanFragmentToTestCertificateDetailsFragment(
                                event.certificateId
                            )
                    )
                }
                DccQrCodeScanViewModel.Event.QrCodeScanInProgress -> {
                    binding.qrCodeScanSpinner.show()
                }
            }
        }

        viewModel.errorEvent.observe(viewLifecycleOwner) {
            binding.qrCodeScanSpinner.hide()
            it.toErrorDialogBuilder(requireContext()).apply {
                setOnDismissListener { popBackStack() }
                if (it is InvalidHealthCertificateException && it.showFaqButton) {
                    setNeutralButton(it.faqButtonText) { _, _ ->
                        openUrl(getString(it.faqLink))
                    }
                }
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

    private fun requestCameraPermission() = requestPermissionLauncher.launch(Manifest.permission.CAMERA)

    private fun leave() {
        showsPermissionDialog = false
        popBackStack()
    }

    override fun onPause() {
        super.onPause()
        binding.qrCodeScanPreview.pause()
    }
}
