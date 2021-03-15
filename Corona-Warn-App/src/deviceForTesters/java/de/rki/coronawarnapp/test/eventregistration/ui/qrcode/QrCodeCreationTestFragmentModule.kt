package de.rki.coronawarnapp.test.eventregistration.ui.qrcode

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class QrCodeCreationTestFragmentModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(QrCodeCreationTestViewModel::class)
    abstract fun qrCodeCreation(
        factory: QrCodeCreationTestViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
