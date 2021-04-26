package de.rki.coronawarnapp.test.presencetracing.ui.poster

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class QrCodePosterTestFragmentModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(QrCodePosterTestViewModel::class)
    abstract fun qrCodePosterTestFragmentModule(
        factory: QrCodePosterTestViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
