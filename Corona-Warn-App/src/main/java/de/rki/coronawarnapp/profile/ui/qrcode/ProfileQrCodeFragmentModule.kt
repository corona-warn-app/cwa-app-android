package de.rki.coronawarnapp.profile.ui.qrcode

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class ProfileQrCodeFragmentModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(ProfileQrCodeFragmentViewModel::class)
    abstract fun profileQrCodeFragmentViewModel(
        factory: ProfileQrCodeFragmentViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
