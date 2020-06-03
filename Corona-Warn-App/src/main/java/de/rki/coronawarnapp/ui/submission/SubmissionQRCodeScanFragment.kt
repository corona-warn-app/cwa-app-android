package de.rki.coronawarnapp.ui.submission

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DefaultDecoderFactory
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSubmissionQrCodeScanBinding
import de.rki.coronawarnapp.ui.BaseFragment
import de.rki.coronawarnapp.ui.main.MainActivity
import de.rki.coronawarnapp.ui.viewmodel.SubmissionViewModel
import de.rki.coronawarnapp.util.CameraPermissionHelper
import de.rki.coronawarnapp.util.DialogHelper

/**
 * A simple [BaseFragment] subclass.
 */
class SubmissionQRCodeScanFragment : BaseFragment() {

    companion object {
        private const val REQUEST_CAMERA_PERMISSION_CODE = 1
        private val TAG: String? = SubmissionQRCodeScanFragment::class.simpleName
    }

    private val viewModel: SubmissionViewModel by viewModels()
    private lateinit var binding: FragmentSubmissionQrCodeScanBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSubmissionQrCodeScanBinding.inflate(inflater)
        binding.lifecycleOwner = this
        return binding.root
    }

    private fun decodeCallback(result: BarcodeResult) {
        viewModel.validateAndStoreTestGUID(result.text)
    }

    private fun startDecode() {
        binding.submissionQrCodeScanPreview.decodeSingle { decodeCallback(it) }
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

        viewModel.scanStatus.observe(viewLifecycleOwner, Observer {
            if (ScanStatus.SUCCESS == it) {
                showSuccessfulScanDialog()
            }

            if (ScanStatus.INVALID == it) {
                showInvalidScanDialog()
            }
        })
    }

    private fun navigateToDispatchScreen() =
        doNavigate(
            SubmissionQRCodeScanFragmentDirections
                .actionSubmissionQRCodeScanFragmentToSubmissionDispatcherFragment()
        )

    private fun showSuccessfulScanDialog() {
        val successfulScanDialogInstance = DialogHelper.DialogInstance(
            R.string.submission_qr_code_scan_successful_dialog_headline,
            R.string.submission_qr_code_scan_successful_dialog_body,
            R.string.submission_qr_code_scan_successful_dialog_button_positive,
            R.string.submission_qr_code_scan_successful_dialog_button_negative,
            true,
            {
                doNavigate(
                    SubmissionQRCodeScanFragmentDirections
                        .actionSubmissionQRCodeScanFragmentToSubmissionRegisterDeviceFragment()
                )
            },
            {
                viewModel.deleteTestGUID()
                navigateToDispatchScreen()
            }
        )

        DialogHelper.showDialog(requireActivity(), successfulScanDialogInstance)
    }

    private fun showInvalidScanDialog() {
        val invalidScanDialogInstance = DialogHelper.DialogInstance(
            R.string.submission_qr_code_scan_invalid_dialog_headline,
            R.string.submission_qr_code_scan_invalid_dialog_body,
            R.string.submission_qr_code_scan_invalid_dialog_button_positive,
            R.string.submission_qr_code_scan_invalid_dialog_button_negative,
            true,
            ::startDecode,
            ::navigateToDispatchScreen
        )

        DialogHelper.showDialog(requireActivity(), invalidScanDialogInstance)
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

        DialogHelper.showDialog(requireActivity(), cameraPermissionRationaleDialogInstance)
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
