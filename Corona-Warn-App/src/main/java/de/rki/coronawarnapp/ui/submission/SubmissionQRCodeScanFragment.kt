package de.rki.coronawarnapp.ui.submission

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DefaultDecoderFactory
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSubmissionQrCodeScanBinding
import de.rki.coronawarnapp.exception.http.BadRequestException
import de.rki.coronawarnapp.exception.http.CwaClientError
import de.rki.coronawarnapp.exception.http.CwaServerError
import de.rki.coronawarnapp.exception.http.CwaWebException
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import de.rki.coronawarnapp.ui.doNavigate
import de.rki.coronawarnapp.ui.main.MainActivity
import de.rki.coronawarnapp.ui.viewmodel.SubmissionViewModel
import de.rki.coronawarnapp.util.CameraPermissionHelper
import de.rki.coronawarnapp.util.DialogHelper
import de.rki.coronawarnapp.util.observeEvent

/**
 * A simple [Fragment] subclass.
 */
class SubmissionQRCodeScanFragment : Fragment() {

    companion object {
        private const val REQUEST_CAMERA_PERMISSION_CODE = 1
        private val TAG: String? = SubmissionQRCodeScanFragment::class.simpleName
    }

    private val viewModel: SubmissionViewModel by activityViewModels()
    private var _binding: FragmentSubmissionQrCodeScanBinding? = null
    private val binding: FragmentSubmissionQrCodeScanBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSubmissionQrCodeScanBinding.inflate(inflater)
        binding.lifecycleOwner = this
        return binding.root
    }

    private fun decodeCallback(result: BarcodeResult) {
        viewModel.validateAndStoreTestGUID(result.text)
    }

    private fun startDecode() {
        binding.submissionQrCodeScanPreview.decodeSingle { decodeCallback(it) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun buildErrorDialog(exception: CwaWebException): DialogHelper.DialogInstance {
        return when (exception) {
            is BadRequestException -> DialogHelper.DialogInstance(
                requireActivity(),
                R.string.submission_error_dialog_web_test_paired_title,
                R.string.submission_error_dialog_web_test_paired_body,
                R.string.submission_error_dialog_web_test_paired_button_positive,
                null,
                true,
                ::navigateToDispatchScreen
            )
            is CwaServerError -> DialogHelper.DialogInstance(
                requireActivity(),
                R.string.submission_error_dialog_web_generic_error_title,
                getString(
                    R.string.submission_error_dialog_web_generic_network_error_body,
                    exception.statusCode
                ),
                R.string.submission_error_dialog_web_generic_error_button_positive,
                null,
                true,
                ::navigateToDispatchScreen
            )
            is CwaClientError -> DialogHelper.DialogInstance(
                requireActivity(),
                R.string.submission_error_dialog_web_generic_error_title,
                getString(
                    R.string.submission_error_dialog_web_generic_network_error_body,
                    exception.statusCode
                ),
                R.string.submission_error_dialog_web_generic_error_button_positive,
                null,
                true,
                ::navigateToDispatchScreen
            )
            else -> DialogHelper.DialogInstance(
                requireActivity(),
                R.string.submission_error_dialog_web_generic_error_title,
                R.string.submission_error_dialog_web_generic_error_body,
                R.string.submission_error_dialog_web_generic_error_button_positive,
                null,
                true,
                ::navigateToDispatchScreen
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.submissionQrCodeScanTorch.setOnCheckedChangeListener { _, isChecked ->
            binding.submissionQrCodeScanPreview.setTorch(
                isChecked
            )
        }

        binding.submissionQrCodeScanClose.buttonIcon.setOnClickListener {
            navigateToDispatchScreen()
        }

        binding.submissionQrCodeScanPreview.decoderFactory =
            DefaultDecoderFactory(listOf(BarcodeFormat.QR_CODE))
        binding.submissionQrCodeScanViewfinderView.setCameraPreview(binding.submissionQrCodeScanPreview)

        viewModel.scanStatus.observeEvent(viewLifecycleOwner, {
            if (ScanStatus.SUCCESS == it) {
                viewModel.doDeviceRegistration()
            }

            if (ScanStatus.INVALID == it) {
                showInvalidScanDialog()
            }
        })

        viewModel.registrationState.observeEvent(viewLifecycleOwner, {
            if (ApiRequestState.SUCCESS == it) {
                findNavController().doNavigate(
                    SubmissionQRCodeScanFragmentDirections
                        .actionSubmissionQRCodeScanFragmentToSubmissionResultFragment()
                )
            }
        })

        viewModel.registrationError.observeEvent(viewLifecycleOwner, {
            DialogHelper.showDialog(buildErrorDialog(it))
        })
    }

    private fun navigateToDispatchScreen() =
        findNavController().doNavigate(
            SubmissionQRCodeScanFragmentDirections
                .actionSubmissionQRCodeScanFragmentToSubmissionDispatcherFragment()
        )

    private fun showInvalidScanDialog() {
        val invalidScanDialogInstance = DialogHelper.DialogInstance(
            requireActivity(),
            R.string.submission_qr_code_scan_invalid_dialog_headline,
            R.string.submission_qr_code_scan_invalid_dialog_body,
            R.string.submission_qr_code_scan_invalid_dialog_button_positive,
            R.string.submission_qr_code_scan_invalid_dialog_button_negative,
            true,
            ::startDecode,
            ::navigateToDispatchScreen
        )

        DialogHelper.showDialog(invalidScanDialogInstance)
    }

    override fun onResume() {
        super.onResume()

        if (!CameraPermissionHelper.hasCameraPermission(requireActivity())) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                showCameraPermissionRationaleDialog()
            } else {
                requestCameraPermission()
            }
        } else {
            binding.submissionQrCodeScanPreview.resume()
            startDecode()
        }
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
                requestCameraPermission()
            },
            {
                goBack()
            }
        )

        DialogHelper.showDialog(cameraPermissionRationaleDialogInstance)
    }

    private fun goBack() = (activity as MainActivity).goBack()

    private fun requestCameraPermission() = requestPermissions(
        arrayOf(Manifest.permission.CAMERA),
        REQUEST_CAMERA_PERMISSION_CODE
    )

    override fun onPause() {
        super.onPause()
        binding.submissionQrCodeScanPreview.pause()
    }
}
