package de.rki.coronawarnapp.ui.presencetracing.organizer.warn.qrcode

import android.Manifest
import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentQrcodeScannerBinding
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.ui.dialog.displayDialog
import de.rki.coronawarnapp.util.ExternalActionHelper.openAppDetailsSettings
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.permission.CameraPermissionHelper
import de.rki.coronawarnapp.util.ui.LazyString
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import timber.log.Timber
import javax.inject.Inject

class OrganizerWarnQrCodeScannerFragment : Fragment(R.layout.fragment_qrcode_scanner), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: OrganizerWarnQrCodeScannerViewModel by cwaViewModels { viewModelFactory }

    // This type of binding initialization should not be followed elsewhere.
    // Please use lazy initialization wherever possible
    private var _binding: FragmentQrcodeScannerBinding? = null
    private val binding get() = _binding!!
    private var showsPermissionDialog = false

    private val filePickerLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        Timber.tag(TAG).d("Uri=$uri")
        uri?.let { viewModel.onImportFile(uri) }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            Timber.tag(TAG).d("Camera permission granted? %b", isGranted)
            when {
                isGranted -> startDecode()
                shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) ->
                    showCameraPermissionRationaleDialog()
                else -> showCameraPermissionDeniedDialog() // User permanently denied access to the camera
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(
        FragmentQrcodeScannerBinding.bind(view)
    ) {
        _binding = this

        scannerPreview.setupCamera(lifecycleOwner = viewLifecycleOwner, activity = requireActivity())
        qrCodeScanTorch.setOnCheckedChangeListener { _, isChecked -> scannerPreview.enableTorch(enable = isChecked) }

        qrCodeScanToolbar.setNavigationOnClickListener { viewModel.onNavigateUp() }
        qrCodeScanSubtitle.setText(R.string.qr_code_scan_body_subtitle_vertretung_warnen)
        infoButton.isGone = true
        buttonOpenFile.setOnClickListener {
            filePickerLauncher.launch(arrayOf("image/*", "application/pdf"))
        }

        viewModel.events.observe2(this@OrganizerWarnQrCodeScannerFragment) { navEvent ->
            qrCodeProcessingView.isVisible = navEvent == OrganizerWarnQrCodeNavigation.InProgress
            when (navEvent) {
                is OrganizerWarnQrCodeNavigation.BackNavigation -> popBackStack()
                is OrganizerWarnQrCodeNavigation.InvalidQrCode -> showInvalidQrCodeInformation(navEvent.errorText)
                is OrganizerWarnQrCodeNavigation.DurationSelectionScreen -> {
                    findNavController().navigate(
                        OrganizerWarnQrCodeScannerFragmentDirections
                            .actionTraceLocationQrScannerFragmentToTraceLocationWarnDurationFragment(
                                navEvent.traceLocation
                            )
                    )
                }
                is OrganizerWarnQrCodeNavigation.Error -> displayDialog { setError(navEvent.exception) }
                OrganizerWarnQrCodeNavigation.InProgress -> Unit
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.qrcodeScanContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)

        if (!showsPermissionDialog) checkCameraPermission()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun checkCameraPermission() = when (CameraPermissionHelper.hasCameraPermission(requireContext())) {
        true -> startDecode()
        false -> requestCameraPermission()
    }

    private fun startDecode() = binding.scannerPreview.decodeSingle { parseResult ->
        viewModel.onParseResult(parseResult = parseResult)
    }

    private fun showCameraPermissionDeniedDialog() {
        displayDialog {
            title(R.string.camera_permission_dialog_title)
            message(R.string.camera_permission_dialog_message)
            negativeButton(R.string.camera_permission_dialog_settings) {
                showsPermissionDialog = false
                requireContext().openAppDetailsSettings()
            }
            positiveButton(android.R.string.ok) { leave() }
        }
        showsPermissionDialog = true
    }

    private fun showCameraPermissionRationaleDialog() {
        displayDialog {
            title(R.string.camera_permission_rationale_dialog_headline)
            message(R.string.camera_permission_rationale_dialog_body)
            positiveButton(R.string.camera_permission_rationale_dialog_button_positive) {
                showsPermissionDialog = false
                requestCameraPermission()
            }
            negativeButton(R.string.camera_permission_rationale_dialog_button_negative) { leave() }
        }
        showsPermissionDialog = true
    }

    private fun showInvalidQrCodeInformation(lazyErrorText: LazyString) = displayDialog {
        val errorText = lazyErrorText.get(requireContext())
        title(R.string.trace_location_attendee_invalid_qr_code_dialog_title)
        message(getString(R.string.trace_location_attendee_invalid_qr_code_dialog_message, errorText))
        positiveButton(R.string.trace_location_attendee_invalid_qr_code_dialog_positive_button) { startDecode() }
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
