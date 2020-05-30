package de.rki.coronawarnapp.ui.submission

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DefaultDecoderFactory
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSubmissionQrCodeScanBinding
import de.rki.coronawarnapp.ui.BaseFragment
import de.rki.coronawarnapp.ui.viewmodel.SubmissionViewModel

/**
 * A simple [BaseFragment] subclass.
 */
class SubmissionQRCodeScanFragment : BaseFragment() {

    companion object {
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
        val alertDialog: AlertDialog = requireActivity().let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setTitle(R.string.submission_qr_code_scan_successful_dialog_headline)
                setMessage(R.string.submission_qr_code_scan_successful_dialog_body)
                setPositiveButton(
                    R.string.submission_qr_code_scan_successful_dialog_button_positive
                ) { _, _ ->
                    doNavigate(
                        SubmissionQRCodeScanFragmentDirections
                            .actionSubmissionQRCodeScanFragmentToSubmissionRegisterDeviceFragment()
                    )
                }
                setNegativeButton(
                    R.string.submission_qr_code_scan_successful_dialog_button_negative
                ) { _, _ ->
                    viewModel.deleteTestGUID()
                    navigateToDispatchScreen()
                }
            }
            builder.create()
        }
        alertDialog.show()
    }

    private fun showInvalidScanDialog() {
        val alertDialog: AlertDialog = requireActivity().let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setTitle(R.string.submission_qr_code_scan_invalid_dialog_headline)
                setMessage(R.string.submission_qr_code_scan_invalid_dialog_body)
                setPositiveButton(
                    R.string.submission_qr_code_scan_invalid_dialog_button_positive
                ) { _, _ ->
                    startDecode()
                }
                setNegativeButton(
                    R.string.submission_qr_code_scan_invalid_dialog_button_negative
                ) { _, _ ->
                    navigateToDispatchScreen()
                }
            }
            builder.create()
        }
        alertDialog.show()
    }

    override fun onResume() {
        super.onResume()
        binding.submissionQrCodeScanPreview.resume()
        startDecode()
    }

    override fun onPause() {
        super.onPause()
        binding.submissionQrCodeScanPreview.pause()
    }
}
