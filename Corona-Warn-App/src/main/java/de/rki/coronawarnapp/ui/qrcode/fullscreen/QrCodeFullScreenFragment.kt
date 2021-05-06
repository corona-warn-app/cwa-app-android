package de.rki.coronawarnapp.ui.qrcode.fullscreen

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.google.android.material.transition.MaterialContainerTransform
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentQrCodeFullScreenBinding
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import javax.inject.Inject

class QrCodeFullScreenFragment : Fragment(R.layout.fragment_qr_code_full_screen), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory

    private val binding: FragmentQrCodeFullScreenBinding by viewBindingLazy()
    private val args by navArgs<QrCodeFullScreenFragmentArgs>()
    private val viewModel: QrCodeFullScreenViewModel by cwaViewModelsAssisted(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, _ ->
            factory as QrCodeFullScreenViewModel.Factory
            factory.create(
                qrcodeText = args.qrCodeText
            )
        }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedElementEnterTransition = MaterialContainerTransform()
        sharedElementReturnTransition = MaterialContainerTransform()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {
        toolbar.setNavigationOnClickListener {
            popBackStack()
        }

        qrCodeImage.setOnClickListener {
            // if (inImmersiveMode) exitImmersiveMode() else enterImmersiveMode()
        }

        postponeEnterTransition()
        viewModel.qrcode.observe(viewLifecycleOwner) {
            qrCodeImage.setImageBitmap(it)
            startPostponedEnterTransition()
        }
    }

    override fun onStop() {
        super.onStop()
        exitImmersiveMode()
    }

    fun exitImmersiveMode() {
        binding.toolbar.apply {
            animate().translationX(-height.toFloat())
        }
    }

    fun enterImmersiveMode() {
        binding.toolbar.apply {
            animate().translationX(0.0f)
        }
    }
}
