package de.rki.coronawarnapp.ui.qrcode.fullscreen

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
import android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
import android.view.View.SYSTEM_UI_FLAG_IMMERSIVE
import android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
import android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
import android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
import android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
import android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
import android.view.View.SYSTEM_UI_FLAG_VISIBLE
import android.view.animation.AccelerateInterpolator
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

        clearSystemUiFlags()
    }

    private fun clearSystemUiFlags() {
        decorView.systemUiVisibility = withLightUiFlags(SYSTEM_UI_FLAG_VISIBLE)
        binding.toolbar.animate().translationY(0.0f)
    }

    private fun exitImmersiveMode() {
        binding.toolbar.animate().translationY(0.0f)
        showSystemUI()
    }

    private fun enterImmersiveMode() {
        hideSystemUI()
        binding.toolbar.apply {
            animate().translationY(-height.toFloat())
        }
    }

    private fun hideSystemUI() {
        decorView.systemUiVisibility = withLightUiFlags(
            SYSTEM_UI_FLAG_IMMERSIVE
                or SYSTEM_UI_FLAG_LAYOUT_STABLE
                or SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or SYSTEM_UI_FLAG_FULLSCREEN
        )
    }

    private fun showSystemUI() {
        decorView.systemUiVisibility = withLightUiFlags(
            SYSTEM_UI_FLAG_LAYOUT_STABLE
                or SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        )
    }

    private val decorView: View get() = requireActivity().window.decorView

    private fun withLightUiFlags(flags: Int): Int {
        var uiFlags = flags
        if (resources.getBoolean(R.bool.lightSystemUI)) {
            uiFlags = uiFlags or SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) uiFlags = uiFlags or SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        }
        return uiFlags
    }
}
