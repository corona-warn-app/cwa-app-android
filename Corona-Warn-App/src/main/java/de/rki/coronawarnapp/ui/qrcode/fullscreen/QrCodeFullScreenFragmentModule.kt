package de.rki.coronawarnapp.ui.qrcode.fullscreen

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class QrCodeFullScreenFragmentModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(QrCodeFullScreenViewModel::class)
    abstract fun checkInsConsentFragment(
        factory: QrCodeFullScreenViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
