package de.rki.coronawarnapp.test.eventregistration.ui.qrcode

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentTestQrcodeCreationBinding
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

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        viewModel.sharingIntent.observe2(this) {
            startActivity(it.get(requireActivity()))
        }

        viewModel.qrCodeBitmap.observe2(this) {
            binding.qrCodeImage.setImageBitmap(it)
            binding.printPDF.isVisible = it != null
        }

        viewModel.errorMessage.observe2(this) {
            Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
        }

        binding.qrCodeText.setText(
            "HTTPS://E.CORONAWARN.APP/C1/BIYAUEDBZY6EIWF7QX6JOKSRPAGEB3H7CIIEGV2BEBGGC5LOMNUCAUD" +
                "BOJ2HSGGTQ6SACIHXQ6SACKA6CJEDARQCEEAPHGEZ5JI2K2T422L5U3SMZY5DGCPUZ2RQACAYEJ3HQYMAFF" +
                "BU2SQCEEAJAUCJSQJ7WDM675MCMOD3L2UL7ECJU7TYERH23B746RQTABO3CTI="
        )
        binding.generateQrCode.setOnClickListener {
            viewModel.createQrCode(binding.qrCodeText.text.toString())
        }

        binding.printPDF.setOnClickListener {
            viewModel.createPDF(binding.pdfPage)
        }
    }
}
