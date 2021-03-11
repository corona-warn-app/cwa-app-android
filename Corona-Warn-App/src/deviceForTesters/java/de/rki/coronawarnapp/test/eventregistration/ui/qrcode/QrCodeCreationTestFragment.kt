package de.rki.coronawarnapp.test.eventregistration.ui.qrcode

import android.os.Bundle
import android.print.PrintAttributes
import android.print.PrintManager
import android.view.View
import android.widget.Toast
import androidx.core.content.getSystemService
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentTestQrcodeCreationBinding
import de.rki.coronawarnapp.test.eventregistration.ui.PrintingAdapter
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

class QrCodeCreationTestFragment : Fragment(R.layout.fragment_test_qrcode_creation), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory

    private val viewModel: QrCodeCreationTestViewModel by cwaViewModels { viewModelFactory }
    private val binding: FragmentTestQrcodeCreationBinding by viewBindingLazy()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        viewModel.sharingIntent.observe2(this) { fileIntent ->

            binding.printPDF.isVisible = true
            binding.printPDF.setOnClickListener {
                // Context must be an Activity context
                context?.getSystemService<PrintManager>()?.apply {
                    print(
                        "CoronaWarnApp",
                        PrintingAdapter(fileIntent.file),
                        PrintAttributes.Builder().build()
                    )
                }
            }
            binding.sharePDF.isVisible = true
            binding.sharePDF.setOnClickListener {
                startActivity(fileIntent.intent(requireActivity()))
            }
        }

        viewModel.qrCodeBitmap.observe2(this) {
            binding.qrCodeImage.setImageBitmap(it)
            if (it != null) {
                viewModel.createPDF(binding.pdfPage)
            }
        }

        viewModel.errorMessage.observe2(this) {
            Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
        }

        binding.generateQrCode.setOnClickListener {
            viewModel.createQrCode(binding.qrCodeText.text.toString())
        }
    }
}
