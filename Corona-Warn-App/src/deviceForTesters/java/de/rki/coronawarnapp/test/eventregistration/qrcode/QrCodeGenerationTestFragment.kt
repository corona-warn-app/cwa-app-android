package de.rki.coronawarnapp.test.eventregistration.qrcode

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentTestQrcodeGenerationBinding
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

class QrCodeGenerationTestFragment : Fragment(R.layout.fragment_test_qrcode_generation), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory

    private val viewModel: QrCodeGenerationTestFragmentViewModel by cwaViewModels { viewModelFactory }
    private val binding: FragmentTestQrcodeGenerationBinding by viewBindingLazy()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        viewModel.intentLiveDate.observe2(this) {
            startActivity(it.get(requireActivity()))
        }

        viewModel.bitmapLiveDate.observe2(this) {
            binding.qrCodeImage.setImageBitmap(it)
            binding.printPDF.isVisible = it != null
        }

        viewModel.errorLiveDate.observe2(this) {
            Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
        }

        binding.generateQrCode.setOnClickListener {
            viewModel.generateQrCode(binding.qrCodeText.text.toString())
        }

        binding.printPDF.setOnClickListener {
            viewModel.generatePDF(requireContext(), binding.pdfPage)
        }
    }
}
