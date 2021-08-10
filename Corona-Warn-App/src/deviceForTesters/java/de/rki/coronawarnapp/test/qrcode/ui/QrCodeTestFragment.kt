package de.rki.coronawarnapp.test.qrcode.ui

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentTestQrcodeBinding
import de.rki.coronawarnapp.test.menu.ui.TestMenuItem
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

class QrCodeTestFragment : Fragment(R.layout.fragment_test_qrcode), AutoInject {
    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val vm: QrCodeTestFragmentViewModel by cwaViewModels { viewModelFactory }

    private val binding: FragmentTestQrcodeBinding by viewBinding()

    private val filePickerLauncher =
        registerForActivityResult(
            ActivityResultContracts.OpenDocument()
        ) { contentURI: Uri ->
            vm.onFileSelected(contentURI)
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vm.qrCodeContent.observe2(this) {
            binding.testQrcodeResult.text = it
        }

        binding.testQrcodeFileSelect.setOnClickListener {
            filePickerLauncher.launch(arrayOf("image/*", "application/pdf"))
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
