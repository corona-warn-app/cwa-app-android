package de.rki.coronawarnapp.covidcertificate.common.scan

import android.Manifest
import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent.TYPE_ANNOUNCEMENT
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.transition.MaterialContainerTransform
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.DefaultDecoderFactory
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.bugreporting.ui.toErrorDialogBuilder
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException
import de.rki.coronawarnapp.covidcertificate.recovery.ui.details.RecoveryCertificateDetailsFragment
import de.rki.coronawarnapp.covidcertificate.test.ui.details.TestCertificateDetailsFragment
import de.rki.coronawarnapp.covidcertificate.vaccination.ui.details.VaccinationDetailsFragment
import de.rki.coronawarnapp.databinding.FragmentQrcodeScannerBinding
import de.rki.coronawarnapp.util.DialogHelper
import de.rki.coronawarnapp.util.ExternalActionHelper.openUrl
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.permission.CameraPermissionHelper
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

class DccQrCodeScanFragment :
    Fragment(R.layout.fragment_qrcode_scanner),
    AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: DccQrCodeScanViewModel by cwaViewModels { viewModelFactory }

    private val binding: FragmentQrcodeScannerBinding by viewBinding()
    private var showsPermissionDialog = false

    private val requestPermissionLauncher =
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val materialContainerTransform = MaterialContainerTransform()
        sharedElementEnterTransition = materialContainerTransform
        sharedElementReturnTransition = materialContainerTransform
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
            qrCodeScanSpinner.hide()
        }

        viewModel.event.observe(viewLifecycleOwner) { event ->
            when (event) {
                DccQrCodeScanViewModel.Event.QrCodeScanInProgress -> binding.qrCodeScanSpinner.show()
                is DccQrCodeScanViewModel.Event.RecoveryCertScreen ->
                    findNavController().navigate(RecoveryCertificateDetailsFragment.uri(event.containerId.identifier))
                is DccQrCodeScanViewModel.Event.TestCertScreen ->
                    findNavController().navigate(TestCertificateDetailsFragment.uri(event.containerId.identifier))
                is DccQrCodeScanViewModel.Event.VaccinationCertScreen ->
                    findNavController().navigate(VaccinationDetailsFragment.uri(event.containerId.identifier))
            }
        }

        viewModel.errorEvent.observe(viewLifecycleOwner) {
            binding.qrCodeScanSpinner.hide()
            it.toErrorDialogBuilder(requireContext()).apply {
                setOnDismissListener { popBackStack() }
                if (it is InvalidHealthCertificateException) {
                    when {
                        it.isCertificateInvalid ->
                            setNeutralButton(R.string.error_button_dcc_faq) { _, _ ->
                                openUrl(R.string.error_button_dcc_faq_link)
                            }

                        it.isSignatureInvalid -> {
                            setTitle(R.string.dcc_signature_validation_dialog_title)
                            setNeutralButton(R.string.dcc_signature_validation_dialog_faq_button) { _, _ ->
                                openUrl(R.string.dcc_signature_validation_dialog_faq_link)
                            }
                        }
                        it.errorCode == InvalidHealthCertificateException.ErrorCode.ALREADY_REGISTERED -> {
                            setNeutralButton(R.string.error_button_dcc_faq) { _, _ ->
                                openUrl(R.string.error_dcc_already_registered_faq_link)
                            }
                        }
                    }
                }
            }.show()
        }
    }

    override fun onResume() {
        super.onResume()
        binding.qrcodeScanContainer.sendAccessibilityEvent(TYPE_ANNOUNCEMENT)
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

//    companion object {
//        val uri = "coronawarnapp://universal-scanner".toUri()
//    }
}
