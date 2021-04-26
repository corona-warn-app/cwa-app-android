package de.rki.coronawarnapp.ui.presencetracing.organizer.poster

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class QrCodePosterFragmentModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(QrCodePosterViewModel::class)
    abstract fun qrCodePosterFragment(
        factory: QrCodePosterViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
