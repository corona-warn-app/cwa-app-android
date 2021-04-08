package de.rki.coronawarnapp.test.presencetracing.ui.poster

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.ui.eventregistration.organizer.poster.QrCodePosterViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class QrCodePosterTestFragmentModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(QrCodePosterViewModel::class)
    abstract fun qrCodePosterTestFragmentModule(
        factory: QrCodePosterViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
