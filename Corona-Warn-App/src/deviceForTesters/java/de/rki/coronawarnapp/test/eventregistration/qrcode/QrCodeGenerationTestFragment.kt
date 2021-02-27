package de.rki.coronawarnapp.test.eventregistration.qrcode

import android.content.Context.PRINT_SERVICE
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.Environment
import android.print.PrintAttributes
import android.print.PrintAttributes.Margins
import android.print.pdf.PrintedPdfDocument
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentTestQrcodeGenerationBinding
import de.rki.coronawarnapp.test.menu.ui.TestMenuItem
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class QrCodeGenerationTestFragment : Fragment(R.layout.fragment_test_qrcode_generation), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: QrCodeGenerationTestFragmentViewModel by cwaViewModels { viewModelFactory }

    private val binding: FragmentTestQrcodeGenerationBinding by viewBindingLazy()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.generateQrCode.setOnClickListener {
            viewModel.generateQrCode(getString(R.string.qr_code_input))
        }

        viewModel.bitmapLiveDate.observe2(this) {
            binding.qrCodeImage.setImageBitmap(it)
            binding.printPDF.isVisible = it != null
        }

        binding.printPDF.setOnClickListener { generatePDF() }
    }

    private fun generatePDF() {
        val pdfPage = binding.pdfPage
        lifecycleScope.launch {
            try {
                val file = File(Environment.getExternalStorageDirectory().path + "/cwa_event.pdf")
                val document = PdfDocument()
                val pageInfo = PdfDocument.PageInfo.Builder(pdfPage.width, pdfPage.height, 1).create()
                val page = document.startPage(pageInfo)
                pdfPage.draw(page.canvas)
                document.finishPage(page)
                document.writeTo(FileOutputStream(file))
                document.close()
            } catch (e: Exception) {
                Timber.d(e, "Generating PDF Failed")
            }
        }
    }

    companion object {
        val MENU_ITEM = TestMenuItem(
            iconRes = R.drawable.ic_qr_code,
            title = "QR Code generation",
            description = "QR Code generation from App",
            targetId = R.id.test_qr_code_generation_fragment
        )
    }
}
