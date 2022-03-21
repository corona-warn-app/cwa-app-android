package de.rki.coronawarnapp.qrcode.ui

import android.Manifest
import android.graphics.Color
import android.net.Uri
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
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.covidcertificate.common.repository.CertificateContainerId
import de.rki.coronawarnapp.covidcertificate.ui.onboarding.CovidCertificateOnboardingFragment
import de.rki.coronawarnapp.databinding.FragmentQrcodeScannerBinding
import de.rki.coronawarnapp.dccticketing.ui.consent.one.DccTicketingConsentOneFragment
import de.rki.coronawarnapp.dccticketing.ui.dialog.DccTicketingDialogType
import de.rki.coronawarnapp.dccticketing.ui.dialog.show
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.ui.presencetracing.attendee.confirm.ConfirmCheckInFragment
import de.rki.coronawarnapp.ui.presencetracing.attendee.onboarding.CheckInOnboardingFragment
import de.rki.coronawarnapp.util.ExternalActionHelper.openAppDetailsSettings
import de.rki.coronawarnapp.util.ExternalActionHelper.openGooglePlay
import de.rki.coronawarnapp.util.ExternalActionHelper.openUrl
import de.rki.coronawarnapp.util.HumanReadableError
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.permission.CameraPermissionHelper
import de.rki.coronawarnapp.util.tryHumanReadableError
import de.rki.coronawarnapp.util.ui.LazyString
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import timber.log.Timber
import javax.inject.Inject

@Suppress("TooManyFunctions")
class QrCodeScannerFragment : Fragment(R.layout.fragment_qrcode_scanner), AutoInject {
    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel by cwaViewModels<QrCodeScannerViewModel> { viewModelFactory }
    private val qrcodeSharedViewModel: QrcodeSharedViewModel by navGraphViewModels(R.id.nav_graph)
    private var showsPermissionDialog = false
    private var _binding: FragmentQrcodeScannerBinding? = null
    private val binding get() = _binding!!

    private val requestPermissionLauncher = registerForActivityResult(RequestPermission()) { isGranted ->
        Timber.tag(TAG).d("Camera permission granted? %b", isGranted)
        when {
            isGranted -> startDecode()
            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> showCameraPermissionRationaleDialog()
            else -> showCameraPermissionDeniedDialog() // User permanently denied access to the camera
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(
        FragmentQrcodeScannerBinding.bind(view)
    ) {
        _binding = this

        scannerPreview.setupCamera(lifecycleOwner = viewLifecycleOwner)
        qrCodeScanTorch.setOnCheckedChangeListener { _, isChecked -> scannerPreview.enableTorch(enable = isChecked) }
        qrCodeScanToolbar.setNavigationOnClickListener { popBackStack() }
        buttonOpenFile.setOnClickListener {
            filePickerLauncher.launch(arrayOf("image/*", "application/pdf"))
        }
        infoButton.setOnClickListener { viewModel.onInfoButtonPress() }

        viewModel.result.observe(viewLifecycleOwner) { scannerResult ->
            qrCodeProcessingView.isVisible = scannerResult == InProgress
            when (scannerResult) {
                is CoronaTestResult -> onCoronaTestResult(scannerResult)
                is DccResult -> onDccResult(scannerResult)
                is CheckInResult -> onCheckInResult(scannerResult)
                is DccTicketingResult -> onDccTicketingResult(scannerResult)
                is DccTicketingError -> when {
                    scannerResult.isDccTicketingMinVersionError ->
                        showValidationServiceMinVersionDialog(errorMsg = scannerResult.errorMsg)
                    else -> showDccTicketingErrorDialog(errorMsg = scannerResult.errorMsg)
                }
                is Error -> when {
                    scannerResult.isDccTicketingError || scannerResult.isAllowListError -> showDccTicketingErrorDialog(
                        humanReadableError = scannerResult.error.tryHumanReadableError(requireContext())
                    )
                    else -> showScannerResultErrorDialog(scannerResult.error)
                }
                InfoScreen -> doNavigate(
                    QrCodeScannerFragmentDirections.actionUniversalScannerToUniversalScannerInformationFragment()
                )
                InProgress -> Unit
            }
        }

        setupTransition()
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

    private fun onDccTicketingResult(scannerResult: DccTicketingResult) {
        when (scannerResult) {
            is DccTicketingResult.ConsentI -> {
                val navOptions = NavOptions.Builder()
                    .setPopUpTo(R.id.universalScanner, true)
                    .build()
                qrcodeSharedViewModel.putDccTicketingTransactionContext(scannerResult.transactionContext)
                findNavController().navigate(
                    DccTicketingConsentOneFragment.uri(scannerResult.transactionContext.initializationData.subject),
                    navOptions
                )
            }
        }
    }

    private fun checkCameraPermission() = when (CameraPermissionHelper.hasCameraPermission(requireContext())) {
        true -> startDecode()
        false -> requestCameraPermission()
    }

    private fun startDecode() {
        runCatching {
            binding.scannerPreview.decodeSingle { parseResult ->
                viewModel.onParseResult(parseResult = parseResult)
            }
        }.onFailure {
            Timber.tag(TAG).d(it, "startDecode() failed")
        }
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
        .setOnDismissListener { startDecode() }
        .show()

    private fun showValidationServiceMinVersionDialog(errorMsg: LazyString) {
        val msg = errorMsg.get(requireContext())
        val dialogType = DccTicketingDialogType.ErrorDialog(
            msg = msg,
            negativeButtonRes = R.string.dcc_ticketing_error_min_version_google_play
        )
        dialogType.show(
            this,
            positiveButtonAction = { startDecode() },
            negativeButtonAction = { requireContext().openGooglePlay() }
        )
    }

    private fun requestCameraPermission() = requestPermissionLauncher.launch(Manifest.permission.CAMERA)

    private fun leave() {
        showsPermissionDialog = false
        popBackStack()
    }

    private fun onCoronaTestResult(scannerResult: CoronaTestResult) {
        when (scannerResult) {
            is CoronaTestResult.TestRegistrationSelection ->
                QrCodeScannerFragmentDirections.actionUniversalScannerToTestRegistrationSelectionFragment(
                    scannerResult.coronaTestQrCode
                )

            is CoronaTestResult.InRecycleBin -> {
                showRestoreCoronaTestConfirmation(scannerResult.recycledCoronaTest)
                null
            }
            is CoronaTestResult.RestoreDuplicateTest ->
                QrCodeScannerFragmentDirections.actionUniversalScannerToSubmissionDeletionWarningFragment(
                    scannerResult.restoreRecycledTestRequest
                )
            is CoronaTestResult.TestPending ->
                QrCodeScannerFragmentDirections.actionUniversalScannerToPendingTestResult(
                    testType = scannerResult.test.type,
                    testIdentifier = scannerResult.test.identifier,
                    forceTestResultUpdate = true
                )
            is CoronaTestResult.TestInvalid ->
                QrCodeScannerFragmentDirections.actionUniversalScannerToSubmissionTestResultInvalidFragment(
                    testType = scannerResult.test.type,
                    testIdentifier = scannerResult.test.identifier
                )
            is CoronaTestResult.TestNegative ->
                QrCodeScannerFragmentDirections.actionUniversalScannerToSubmissionTestResultNegativeFragment(
                    testType = scannerResult.test.type,
                    testIdentifier = scannerResult.test.identifier
                )
            is CoronaTestResult.TestPositive ->
                QrCodeScannerFragmentDirections.actionUniversalScannerToSubmissionTestResultKeysSharedFragment(
                    testType = scannerResult.test.type,
                    testIdentifier = scannerResult.test.identifier
                )
            is CoronaTestResult.WarnOthers ->
                QrCodeScannerFragmentDirections
                    .actionUniversalScannerToSubmissionResultPositiveOtherWarningNoConsentFragment(
                        testType = scannerResult.test.type
                    )
        }
            ?.let { doNavigate(it) }
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
                    CovidCertificateOnboardingFragment.uri(scannerResult.dccQrCode.hash),
                    navOptions
                )
            }
            is DccResult.InRecycleBin -> showRestoreDgcConfirmation(scannerResult.recycledContainerId)
            is DccResult.MaxPersonsBlock -> {
                showMaxPersonExceedsMaxResult(scannerResult.max)
            }
            is DccResult.MaxPersonsWarning -> {
                showMaxPersonExceedsThresholdResult(scannerResult.max, scannerResult.uri, navOptions)
            }
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

    private fun showRestoreCoronaTestConfirmation(recycledCoronaTest: CoronaTest) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.recycle_bin_restore_corona_test_dialog_title)
            .setCancelable(false)
            .setMessage(R.string.recycle_bin_restore_corona_test_dialog_message)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                viewModel.restoreCoronaTest(recycledCoronaTest)
            }
            .show()
    }

    private fun showMaxPersonExceedsThresholdResult(max: Int, deeplink: Uri, navOptions: NavOptions) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.qr_code_error_max_person_threshold_title)
            .setCancelable(false)
            .setMessage(getString(R.string.qr_code_error_max_person_threshold_body, max))
            .setOnDismissListener {
                findNavController().navigate(deeplink, navOptions)
            }
            .setPositiveButton(R.string.qr_code_error_max_person_covpasscheck_button) { _, _ ->
                openUrl(R.string.qr_code_error_max_person_covpasscheck_link)
            }
            .setNegativeButton(R.string.qr_code_error_max_person_faq_button) { _, _ ->
                openUrl(R.string.qr_code_error_max_person_faq_link)
            }
            .setNeutralButton(android.R.string.ok) { _, _ -> }
            .show()
    }

    private fun showMaxPersonExceedsMaxResult(max: Int) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.qr_code_error_max_person_max_title)
            .setCancelable(false)
            .setMessage(getString(R.string.qr_code_error_max_person_max_body, max))
            .setOnDismissListener { popBackStack() }
            .setPositiveButton(R.string.qr_code_error_max_person_covpasscheck_button) { _, _ ->
                openUrl(R.string.qr_code_error_max_person_covpasscheck_link)
            }
            .setNegativeButton(R.string.qr_code_error_max_person_faq_button) { _, _ ->
                openUrl(R.string.qr_code_error_max_person_faq_link)
            }
            .setNeutralButton(android.R.string.ok) { _, _ -> }
            .show()
    }

    private fun showDccTicketingErrorDialog(humanReadableError: HumanReadableError) {
        val dialogType = DccTicketingDialogType.ErrorDialog(
            title = humanReadableError.title,
            msg = humanReadableError.description
        )
        showDccTicketingErrorDialog(dialogType = dialogType)
    }

    private fun showDccTicketingErrorDialog(errorMsg: LazyString) {
        val msg = errorMsg.get(requireContext())
        val dialogType = DccTicketingDialogType.ErrorDialog(msg = msg)
        showDccTicketingErrorDialog(dialogType = dialogType)
    }

    private fun showDccTicketingErrorDialog(dialogType: DccTicketingDialogType.ErrorDialog) = dialogType
        .show(this, dismissAction = { startDecode() })

    companion object {
        private val TAG = tag<QrCodeScannerFragment>()
    }
}
