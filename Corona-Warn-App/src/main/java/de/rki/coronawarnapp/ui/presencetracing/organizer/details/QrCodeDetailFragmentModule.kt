package de.rki.coronawarnapp.ui.presencetracing.organizer.details

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class QrCodeDetailFragmentModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(QrCodeDetailViewModel::class)
    abstract fun qrCodeDetailFragmentVM(
        factory: QrCodeDetailViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
