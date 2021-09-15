package de.rki.coronawarnapp.qrcode.ui

import android.Manifest
import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.activity.result.contract.ActivityResultContracts.OpenDocument
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.transition.MaterialContainerTransform
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.DefaultDecoderFactory
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentQrcodeScannerBinding
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.ui.presencetracing.attendee.checkins.CheckInsFragment
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.permission.CameraPermissionHelper
import de.rki.coronawarnapp.util.ui.LazyString
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import timber.log.Timber
import javax.inject.Inject

class QrCodeScannerFragment : Fragment(R.layout.fragment_qrcode_scanner), AutoInject {
    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel by cwaViewModels<QrCodeScannerViewModel> { viewModelFactory }

    private val binding by viewBinding<FragmentQrcodeScannerBinding>()
    private var showsPermissionDialog = false

    private val requestPermissionLauncher = registerForActivityResult(RequestPermission()) { isGranted ->
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

    private val filePickerLauncher = registerForActivityResult(OpenDocument()) { uri ->
        Timber.tag(TAG).d("Uri=$uri")
        uri?.let { viewModel.onImportFile(uri) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val materialContainerTransform = MaterialContainerTransform()
        sharedElementEnterTransition = materialContainerTransform
        sharedElementReturnTransition = materialContainerTransform
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        with(binding) {
            qrCodeScanTorch.setOnCheckedChangeListener { _, isChecked -> binding.qrCodeScanPreview.setTorch(isChecked) }
            qrCodeScanToolbar.setNavigationOnClickListener { popBackStack() }
            qrCodeScanPreview.decoderFactory = DefaultDecoderFactory(listOf(BarcodeFormat.QR_CODE))
            buttonOpenFile.setOnClickListener {
                filePickerLauncher.launch(arrayOf("image/*", "application/pdf"))
            }
        }

        viewModel.result.observe(viewLifecycleOwner) { scannerResult ->
            binding.qrCodeProcessingView.isVisible = scannerResult == InProgress
            when (scannerResult) {
                is CoronaTestResult -> onCoronaTestResult(scannerResult)
                is DccResult -> onDccResult(scannerResult)
                is CheckInResult -> onCheckInResult(scannerResult)

                is Error -> scannerResult.error.toQrCodeErrorDialogBuilder(requireContext())
                    .setOnDismissListener { popBackStack() }
                    .show()

                InProgress -> binding.qrCodeProcessingView.isVisible = true
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.qrcodeScanContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
        if (CameraPermissionHelper.hasCameraPermission(requireActivity())) {
            binding.qrCodeScanPreview.resume()
            startDecode()
            return
        }
        if (showsPermissionDialog) return

        requestCameraPermission()
    }

    override fun onPause() {
        super.onPause()
        binding.qrCodeScanPreview.pause()
    }

    private fun startDecode() = binding.qrCodeScanPreview.decodeSingle { barcodeResult ->
        viewModel.onScanResult(barcodeResult.text)
    }

    private fun showCameraPermissionDeniedDialog() {
        MaterialAlertDialogBuilder(requireContext()).apply {
            setTitle(R.string.submission_qr_code_scan_permission_denied_dialog_headline)
            setMessage(R.string.submission_qr_code_scan_permission_denied_dialog_body)
            setPositiveButton(R.string.submission_qr_code_scan_permission_denied_dialog_button) { _, _ -> leave() }
        }.show()
        showsPermissionDialog = true
    }

    private fun showCameraPermissionRationaleDialog() {
        MaterialAlertDialogBuilder(requireContext()).apply {
            setTitle(R.string.submission_qr_code_scan_permission_rationale_dialog_headline)
            setMessage(R.string.submission_qr_code_scan_permission_rationale_dialog_body)
            setPositiveButton(R.string.submission_qr_code_scan_permission_rationale_dialog_button_positive) { _, _ ->
                showsPermissionDialog = false
                requestCameraPermission()
            }
            setNegativeButton(R.string.submission_qr_code_scan_permission_rationale_dialog_button_negative) { _, _ ->
                leave()
            }
        }.show()
        showsPermissionDialog = true
    }

    private fun showCheckInQrCodeError(lazyErrorText: LazyString) =
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

    private fun requestCameraPermission() = requestPermissionLauncher.launch(Manifest.permission.CAMERA)

    private fun leave() {
        showsPermissionDialog = false
        popBackStack()
    }

    private fun onCoronaTestResult(scannerResult: CoronaTestResult) {
        when (scannerResult) {
            is CoronaTestResult.ConsentTest -> findNavController().navigate(
                QrCodeScannerFragmentDirections
                    .actionUniversalScannerToSubmissionConsentFragment(scannerResult.rawQrCode)
            )
            // TODO: DuplicateTest probably should be removed
            is CoronaTestResult.DuplicateTest -> findNavController().navigate(
                QrCodeScannerFragmentDirections
                    .actionUniversalScannerToSubmissionConsentFragment(scannerResult.rawQrCode)
            )
        }
    }

    private fun onDccResult(scannerResult: DccResult) {
        Timber.tag(TAG).d(" onDccResult(scannerResult=%s)", scannerResult)
        val navOptions = NavOptions.Builder()
            .setPopUpTo(R.id.universalScanner, true)
            .build()
        findNavController().navigate(scannerResult.uri, navOptions)
    }

    private fun onCheckInResult(scannerResult: CheckInResult) {
        when (scannerResult) {
            is CheckInResult.Details -> {
                findNavController().navigate(
                    CheckInsFragment.createDeepLink(scannerResult.verifiedLocation.traceLocation.locationUrl),
                    NavOptions.Builder()
                        .setPopUpTo(R.id.checkInsFragment, true)
                        .build()
                )
            }
            is CheckInResult.Error -> showCheckInQrCodeError(scannerResult.stringRes)
        }
    }

    companion object {
        private val TAG = tag<QrCodeScannerFragment>()
        val uri = "coronawarnapp://universal-scanner".toUri()
    }
}
