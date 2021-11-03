package de.rki.coronawarnapp.qrcode.ui

import android.Manifest
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.activity.result.contract.ActivityResultContracts.OpenDocument
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import androidx.transition.Fade
import androidx.transition.Slide
import androidx.transition.TransitionSet
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.transition.MaterialContainerTransform
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.DefaultDecoderFactory
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.common.repository.CertificateContainerId
import de.rki.coronawarnapp.covidcertificate.ui.onboarding.CovidCertificateOnboardingFragment
import de.rki.coronawarnapp.databinding.FragmentQrcodeScannerBinding
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.ui.presencetracing.attendee.confirm.ConfirmCheckInFragment
import de.rki.coronawarnapp.ui.presencetracing.attendee.onboarding.CheckInOnboardingFragment
import de.rki.coronawarnapp.util.ExternalActionHelper.openAppDetailsSettings
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.permission.CameraPermissionHelper
import de.rki.coronawarnapp.util.ui.LazyString
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import timber.log.Timber
import javax.inject.Inject

class QrCodeScannerFragment : Fragment(R.layout.fragment_qrcode_scanner), AutoInject {
    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel by cwaViewModels<QrCodeScannerViewModel> { viewModelFactory }
    private val qrcodeSharedViewModel: QrcodeSharedViewModel by navGraphViewModels(R.id.nav_graph)

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

                is Error -> showScannerResultErrorDialog(scannerResult.error)
                InProgress -> binding.qrCodeProcessingView.isVisible = true
            }
        }

        setupTransition()
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
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.camera_permission_dialog_title)
            .setMessage(R.string.camera_permission_dialog_message)
            .setNegativeButton(R.string.camera_permission_dialog_settings) { _, _ ->
                showsPermissionDialog = false
                requireContext().openAppDetailsSettings()
            }
            .setPositiveButton(android.R.string.ok) { _, _ -> leave() }
            .show()
        showsPermissionDialog = true
    }

    private fun showCameraPermissionRationaleDialog() {
        MaterialAlertDialogBuilder(requireContext()).apply {
            setTitle(R.string.camera_permission_rationale_dialog_headline)
            setMessage(R.string.camera_permission_rationale_dialog_body)
            setPositiveButton(R.string.camera_permission_rationale_dialog_button_positive) { _, _ ->
                showsPermissionDialog = false
                requestCameraPermission()
            }
            setNegativeButton(R.string.camera_permission_rationale_dialog_button_negative) { _, _ ->
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

    private fun showScannerResultErrorDialog(error: Throwable) = error
        .toQrCodeErrorDialogBuilder(requireContext())
        .setNeutralButton(null, null) // Remove details
        .setOnDismissListener { startDecode() }
        .show()

    private fun requestCameraPermission() = requestPermissionLauncher.launch(Manifest.permission.CAMERA)

    private fun leave() {
        showsPermissionDialog = false
        popBackStack()
    }

    private fun onCoronaTestResult(scannerResult: CoronaTestResult) {
        when (scannerResult) {
            is CoronaTestResult.ConsentTest ->
                QrCodeScannerFragmentDirections.actionUniversalScannerToSubmissionConsentFragment(
                    scannerResult.coronaTestQrCode
                )
            is CoronaTestResult.DuplicateTest ->
                QrCodeScannerFragmentDirections.actionUniversalScannerToSubmissionDeletionWarningFragment(
                    scannerResult.coronaTestQrCode
                )
        }.also {
            doNavigate(it)
        }
    }

    private fun onDccResult(scannerResult: DccResult) {
        Timber.tag(TAG).d(" onDccResult(scannerResult=%s)", scannerResult)
        val navOptions = NavOptions.Builder()
            .setPopUpTo(R.id.universalScanner, true)
            .build()
        when (scannerResult) {
            is DccResult.Details -> findNavController().navigate(scannerResult.uri, navOptions)
            is DccResult.Onboarding -> {
                qrcodeSharedViewModel.putDccQrCode(scannerResult.dccQrCode)
                findNavController().navigate(
                    CovidCertificateOnboardingFragment.uri(scannerResult.dccQrCode.uniqueCertificateIdentifier),
                    navOptions
                )
            }
            is DccResult.InRecycleBin -> showRestoreDgcConfirmation(scannerResult.recycledContainerId)
        }
    }

    private fun onCheckInResult(scannerResult: CheckInResult) {
        when (scannerResult) {
            is CheckInResult.Details -> {
                val locationId = scannerResult.verifiedLocation.locationIdHex
                val uri = when {
                    scannerResult.requireOnboarding -> CheckInOnboardingFragment.uri(locationId)
                    else -> ConfirmCheckInFragment.uri(locationId)
                }
                qrcodeSharedViewModel.putVerifiedTraceLocation(scannerResult.verifiedLocation)
                findNavController().navigate(
                    uri,
                    NavOptions.Builder()
                        .setPopUpTo(R.id.universalScanner, true)
                        .build()
                )
            }
            is CheckInResult.Error -> showCheckInQrCodeError(scannerResult.lazyMessage)
        }
    }

    private fun setupTransition() {
        val animationDuration = resources.getInteger(R.integer.fab_scanner_transition_duration).toLong()
        enterTransition = MaterialContainerTransform().apply {
            startView = requireActivity().findViewById(R.id.scanner_fab)
            endView = binding.root
            duration = animationDuration
            scrimColor = Color.TRANSPARENT
        }
        returnTransition = TransitionSet().apply {
            addTransition(Slide())
            addTransition(Fade())
            addTarget(R.id.qrcode_scan_container)
            duration = animationDuration
        }
    }

    private fun showRestoreDgcConfirmation(containerId: CertificateContainerId) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.recycle_bin_restore_dgc_dialog_title)
            .setCancelable(false)
            .setMessage(R.string.recycle_bin_restore_dgc_dialog_message)
            .setPositiveButton(android.R.string.ok) { _, _ -> viewModel.restoreCertificate(containerId) }
            .show()
    }

    companion object {
        private val TAG = tag<QrCodeScannerFragment>()
    }
}
