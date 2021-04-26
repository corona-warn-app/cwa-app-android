package de.rki.coronawarnapp.test.coronatest.ui

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.DefaultDecoderFactory
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentTestCoronatestBinding
import de.rki.coronawarnapp.test.menu.ui.TestMenuItem
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.permission.CameraPermissionHelper
import de.rki.coronawarnapp.util.tryHumanReadableError
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

@SuppressLint("SetTextI18n")
class CoronaTestTestFragment : Fragment(R.layout.fragment_test_coronatest), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val vm: CoronaTestTestFragmentViewModel by cwaViewModels { viewModelFactory }

    private val binding: FragmentTestCoronatestBinding by viewBindingLazy()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            qrcodeScanAction.setOnClickListener {
                if (!CameraPermissionHelper.hasCameraPermission(requireActivity())) {
                    requestPermissions(arrayOf(Manifest.permission.CAMERA), 99)
                    return@setOnClickListener
                }

                val stop = {
                    qrcodeScanPreview.stopDecoding()
                    qrcodeScanPreview.pause()
                    qrcodeScanContainer.isGone = true
                }

                val start = {
                    qrcodeScanContainer.isVisible = true
                    qrcodeScanPreview.resume()
                    qrcodeScanPreview.decodeSingle { result ->
                        vm.onQRCodeScanner(result)
                        stop()
                    }
                }

                if (qrcodeScanContainer.isVisible) {
                    stop()
                } else {
                    start()
                }
            }
            qrcodeScanPreview.decoderFactory = DefaultDecoderFactory(listOf(BarcodeFormat.QR_CODE))
            qrcodeScanViewfinder.setCameraPreview(binding.qrcodeScanPreview)
        }

        vm.pcrtState.observe2(this) {
            binding.pcrtData.text = it.getNiceTextForHumans(requireContext())
        }
        binding.apply {
            pcrtDeleteAction.setOnClickListener { vm.deletePCRT() }
            pcrtRefreshAction.setOnClickListener { vm.refreshPCRT() }
        }

        vm.ratState.observe2(this) {
            binding.ratData.text = it.getNiceTextForHumans(requireContext())
        }
        binding.apply {
            ratDeleteAction.setOnClickListener { vm.deleteRAT() }
            ratRefreshAction.setOnClickListener { vm.refreshRAT() }
        }

        vm.errorEvents.observe2(this) {
            val error = it.tryHumanReadableError(requireContext())
            Toast.makeText(requireContext(), error.description, Toast.LENGTH_LONG).show()
        }
    }

    companion object {
        val MENU_ITEM = TestMenuItem(
            title = "Corona Tests",
            description = "PCR / RapidAntigen Test Options",
            targetId = R.id.coronaTestTestFragment
        )
    }
}
