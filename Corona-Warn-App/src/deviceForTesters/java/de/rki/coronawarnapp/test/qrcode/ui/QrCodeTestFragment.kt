package de.rki.coronawarnapp.test.qrcode.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentTestQrcodeBinding
import de.rki.coronawarnapp.test.menu.ui.TestMenuItem
import de.rki.coronawarnapp.util.ui.viewBinding

class QrCodeTestFragment : Fragment(R.layout.fragment_test_qrcode) {

    private val binding: FragmentTestQrcodeBinding by viewBinding()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.testQrcodeFileSelect.setOnClickListener {
            findNavController().navigate(R.id.qrCodeScannerFragment)
        }
    }

    companion object {
        val MENU_ITEM = TestMenuItem(
            title = "QR Code Image Scanning",
            description = "Test QR Code Scanning from image files.",
            targetId = R.id.test_qrcode_fragment
        )
    }
}
