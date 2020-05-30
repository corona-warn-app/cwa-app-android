package de.rki.coronawarnapp.ui.register

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
import de.rki.coronawarnapp.databinding.FragmentRegisterQrCodeScanBinding
import de.rki.coronawarnapp.ui.BaseFragment
import de.rki.coronawarnapp.ui.viewmodel.SubmissionViewModel

/**
 * A simple [BaseFragment] subclass.
 */
class RegisterQRCodeScanFragment : BaseFragment() {

    companion object {
        private val TAG: String? = RegisterQRCodeScanFragment::class.simpleName
    }

    private val viewModel: SubmissionViewModel by viewModels()
    private lateinit var binding: FragmentRegisterQrCodeScanBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRegisterQrCodeScanBinding.inflate(inflater)
        binding.lifecycleOwner = this
        return binding.root
    }

    private fun decodeCallback(result: BarcodeResult) {
        viewModel.validateAndStoreTestGUID(result.text)
    }

    private fun startDecode() {
        binding.registerQrCodeScanPreview.decodeSingle { decodeCallback(it) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.registerQrCodeScanTorch.setOnCheckedChangeListener { _, isChecked ->
            binding.registerQrCodeScanPreview.setTorch(
                isChecked
            )
        }

        binding.registerQrCodeScanClose.buttonIcon.setOnClickListener {
            doNavigate(
                RegisterQRCodeScanFragmentDirections.actionRegisterQRCodeFragmentToSubmissionDispatcherFragment()
            )
        }

        binding.registerQrCodeScanPreview.decoderFactory =
            DefaultDecoderFactory(listOf(BarcodeFormat.QR_CODE))
        binding.registerQrCodeScanViewfinderView.setCameraPreview(binding.registerQrCodeScanPreview)

        viewModel.scanStatus.observe(viewLifecycleOwner, Observer {
            if (ScanStatus.SUCCESS == it) {
                showSuccessfulScanDialog()
            }

            if (ScanStatus.INVALID == it) {
                showInvalidScanDialog()
            }
        })

        startDecode()
    }

    private fun navigateToDispatchScreen() =
        doNavigate(
            RegisterQRCodeScanFragmentDirections.actionRegisterQRCodeFragmentToSubmissionDispatcherFragment()
        )

    private fun showSuccessfulScanDialog() {
        val alertDialog: AlertDialog = requireActivity().let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setTitle(R.string.register_qr_code_scan_successful_dialog_headline)
                setMessage(R.string.register_qr_code_scan_successful_dialog_body)
                setPositiveButton(
                    R.string.register_qr_code_scan_successful_dialog_button_positive
                ) { _, _ ->
                    doNavigate(
                        RegisterQRCodeScanFragmentDirections.actionRegisterQRCodeFragmentToDeviceRegistrationFragment()
                    )
                }
                setNegativeButton(
                    R.string.register_qr_code_scan_successful_dialog_button_negative
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
                setTitle(R.string.register_qr_code_scan_invalid_dialog_headline)
                setMessage(R.string.register_qr_code_scan_invalid_dialog_body)
                setPositiveButton(
                    R.string.register_qr_code_scan_invalid_dialog_button_positive
                ) { _, _ ->
                    startDecode()
                }
                setNegativeButton(
                    R.string.register_qr_code_scan_invalid_dialog_button_negative
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
        binding.registerQrCodeScanPreview.resume()
    }

    override fun onPause() {
        super.onPause()
        binding.registerQrCodeScanPreview.pause()
    }
}
