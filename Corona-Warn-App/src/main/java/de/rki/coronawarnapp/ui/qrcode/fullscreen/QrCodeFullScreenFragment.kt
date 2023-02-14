package de.rki.coronawarnapp.ui.qrcode.fullscreen

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
import android.view.animation.AccelerateInterpolator
import androidx.core.view.WindowInsetsCompat.Type
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_BARS_BY_SWIPE
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import coil.loadAny
import com.google.android.material.transition.MaterialContainerTransform
import dagger.hilt.android.AndroidEntryPoint
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentQrCodeFullScreenBinding
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.assistedViewModel
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import javax.inject.Inject

@AndroidEntryPoint
class QrCodeFullScreenFragment : Fragment(R.layout.fragment_qr_code_full_screen) {

    @Inject lateinit var factory: QrCodeFullScreenViewModel.Factory
    private val binding by viewBinding<FragmentQrCodeFullScreenBinding>()
    private val args by navArgs<QrCodeFullScreenFragmentArgs>()
    private val viewModel by assistedViewModel {
        factory.create(qrCode = args.qrCode)
    }

    private val insetsController by lazy { insetsController() }

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
            viewModel.qrCodeRequest.observe(viewLifecycleOwner) {
                qrCodeImage.loadAny(it) {
                    crossfade(true)
                }
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

    override fun onResume() {
        super.onResume()
        keepScreenOn(on = true)
    }

    override fun onPause() {
        super.onPause()
        keepScreenOn(on = false)
    }

    private fun keepScreenOn(on: Boolean) = with(requireActivity().window) {
        if (on) addFlags(FLAG_KEEP_SCREEN_ON) else clearFlags(FLAG_KEEP_SCREEN_ON)
        attributes = attributes.apply { screenBrightness = if (on) 1f else -1f }
    }

    private fun exitImmersiveMode() {
        binding.toolbar.animate().alpha(1.0f)
        insetsController.show(Type.systemBars())
    }

    private fun enterImmersiveMode() {
        insetsController.hide(Type.systemBars())
        binding.toolbar.animate().alpha(0.0f)
    }

    private fun insetsController(): WindowInsetsControllerCompat {
        val window = requireActivity().window
        return WindowInsetsControllerCompat(window, window.decorView)
            .apply {
                systemBarsBehavior = BEHAVIOR_SHOW_BARS_BY_SWIPE
            }
    }
}
