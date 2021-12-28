package de.rki.coronawarnapp.ui.presencetracing.organizer.warn.qrcode

import android.Manifest
import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.bugreporting.ui.toErrorDialogBuilder
import de.rki.coronawarnapp.databinding.FragmentQrcodeScannerBinding
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.util.ExternalActionHelper.openAppDetailsSettings
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.permission.CameraPermissionHelper
import de.rki.coronawarnapp.util.ui.LazyString
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import timber.log.Timber
import javax.inject.Inject

class OrganizerWarnQrCodeScannerFragment : Fragment(R.layout.fragment_qrcode_scanner), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: OrganizerWarnQrCodeScannerViewModel by cwaViewModels { viewModelFactory }

    private val binding: FragmentQrcodeScannerBinding by viewBinding()
    private var showsPermissionDialog = false

    private val filePickerLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        Timber.tag(TAG).d("Uri=$uri")
        uri?.let { viewModel.onImportFile(uri) }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {
        scannerPreview.setupCamera(lifecycleOwner = viewLifecycleOwner) {
            viewModel.onNewImage(image = it)
        }
        qrCodeScanTorch.setOnCheckedChangeListener { _, isChecked -> scannerPreview.enableTorch(enable = isChecked) }

        qrCodeScanToolbar.setNavigationOnClickListener { viewModel.onNavigateUp() }
        qrCodeScanSubtitle.setText(R.string.qr_code_scan_body_subtitle_vertretung_warnen)
        infoButton.isGone = true
        buttonOpenFile.setOnClickListener {
            filePickerLauncher.launch(arrayOf("image/*", "application/pdf"))
        }

        viewModel.events.observe2(this@OrganizerWarnQrCodeScannerFragment) { navEvent ->
            qrCodeProcessingView.isVisible = navEvent == OrganizerWarnQrCodeNavigation.InProgress
            scannerPreview.scanEnabled = navEvent == OrganizerWarnQrCodeNavigation.Scanning
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
                is OrganizerWarnQrCodeNavigation.Error ->
                    navEvent.exception.toErrorDialogBuilder(requireContext()).show()
                OrganizerWarnQrCodeNavigation.InProgress,
                OrganizerWarnQrCodeNavigation.Scanning -> {
                    // NO-OP
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.qrcodeScanContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
        if (CameraPermissionHelper.hasCameraPermission(requireActivity()) || showsPermissionDialog) return

        requestCameraPermission()
    }

    private fun startDecode() = viewModel.startDecode()

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

    private fun showInvalidQrCodeInformation(lazyErrorText: LazyString) {
        MaterialAlertDialogBuilder(requireContext()).apply {
            val errorText = lazyErrorText.get(context)
            setTitle(R.string.trace_location_attendee_invalid_qr_code_dialog_title)
            setMessage(getString(R.string.trace_location_attendee_invalid_qr_code_dialog_message, errorText))
            setPositiveButton(R.string.trace_location_attendee_invalid_qr_code_dialog_positive_button) { _, _ ->
                startDecode()
            }
        }.show()
    }

    private fun requestCameraPermission() = requestPermissionLauncher.launch(Manifest.permission.CAMERA)

    private fun leave() {
        showsPermissionDialog = false
        popBackStack()
    }

    companion object {
        private val TAG = tag<OrganizerWarnQrCodeScannerFragment>()
    }
}
