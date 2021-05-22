package de.rki.coronawarnapp.ui.qrcode.fullscreen

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.animation.AccelerateInterpolator
import androidx.core.view.WindowInsetsCompat.Type
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_BARS_BY_TOUCH
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.google.android.material.transition.MaterialContainerTransform
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentQrCodeFullScreenBinding
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import javax.inject.Inject

class QrCodeFullScreenFragment : Fragment(R.layout.fragment_qr_code_full_screen), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory

    private val binding: FragmentQrCodeFullScreenBinding by viewBinding()
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

    private val window: Window get() = requireActivity().window
    private val insetsController by lazy {
        WindowInsetsControllerCompat(window, window.decorView).apply {
            systemBarsBehavior = BEHAVIOR_SHOW_BARS_BY_TOUCH
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val containerTransform = MaterialContainerTransform()
            .apply {
                scrimColor = Color.TRANSPARENT
                interpolator = AccelerateInterpolator()
            }
        sharedElementEnterTransition = containerTransform
        sharedElementReturnTransition = containerTransform
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) =
        with(binding) {
            toolbar.setNavigationOnClickListener { popBackStack() }
            root.setOnClickListener { viewModel.switchImmersiveMode() }

            postponeEnterTransition()
            viewModel.qrcode.observe(viewLifecycleOwner) {
                qrCodeImage.setImageBitmap(it)
                startPostponedEnterTransition()
            }
            viewModel.immersiveMode.observe(viewLifecycleOwner) { immersive ->
                if (immersive) enterImmersiveMode() else exitImmersiveMode()
            }
        }

    override fun onStop() {
        super.onStop()

        exitImmersiveMode()
    }

    private fun exitImmersiveMode() {
        binding.toolbar.animate().translationY(0.0f)
        insetsController.show(Type.systemBars())
    }

    private fun enterImmersiveMode() {
        insetsController.hide(Type.systemBars())
        binding.toolbar.apply { animate().translationY(-height.toFloat()) }
    }
}
