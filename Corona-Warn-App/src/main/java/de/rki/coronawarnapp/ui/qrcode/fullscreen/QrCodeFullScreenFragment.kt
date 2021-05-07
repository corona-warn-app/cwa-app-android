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
        sharedElementEnterTransition = MaterialContainerTransform().apply {
            scrimColor = Color.TRANSPARENT
        }
        sharedElementReturnTransition = MaterialContainerTransform().apply {
            scrimColor = Color.TRANSPARENT
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) =
        with(binding) {
            toolbar.setNavigationOnClickListener {
                popBackStack()
            }

            qrCodeImage.setOnClickListener {
                viewModel.switchImmersiveMode()
            }

            postponeEnterTransition()
            viewModel.qrcode.observe(viewLifecycleOwner) {
                qrCodeImage.setImageBitmap(it)
                startPostponedEnterTransition()
            }
            viewModel.immersiveMode.observe(viewLifecycleOwner) {
                if (it) enterImmersiveMode() else exitImmersiveMode()
            }
        }

    override fun onStop() {
        super.onStop()

        clearAllFlags()
    }

    private fun clearAllFlags() {
        var flags = SYSTEM_UI_FLAG_VISIBLE
        flags = lightUIFlags(flags)
        requireActivity().window.decorView.systemUiVisibility = flags
        binding.toolbar.apply {
            animate().translationY(0.0f)
        }
    }

    private fun exitImmersiveMode() {
        binding.toolbar.apply {
            animate().translationY(0.0f)
        }

        showSystemUI()
    }

    private fun enterImmersiveMode() {
        hideSystemUI()
        binding.toolbar.apply {
            animate().translationY(-height.toFloat())
        }
    }

    private fun hideSystemUI() {
        var flags = (
            SYSTEM_UI_FLAG_IMMERSIVE
                or SYSTEM_UI_FLAG_LAYOUT_STABLE
                or SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or SYSTEM_UI_FLAG_FULLSCREEN
            )
        flags = lightUIFlags(flags)
        requireActivity().window.decorView.systemUiVisibility = flags
    }

    private fun showSystemUI() {
        var flags = (
            SYSTEM_UI_FLAG_LAYOUT_STABLE
                or SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            )

        flags = lightUIFlags(flags)

        requireActivity().window.decorView.systemUiVisibility = flags
    }

    private fun lightUIFlags(flag: Int): Int {
        var flags = flag
        if (resources.getBoolean(R.bool.lightSystemUI)) {
            flags = flags or SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                flags = flags or SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        }
        return flags
    }
}
