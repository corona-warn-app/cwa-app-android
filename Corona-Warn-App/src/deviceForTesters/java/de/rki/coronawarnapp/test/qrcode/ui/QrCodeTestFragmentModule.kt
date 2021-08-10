package de.rki.coronawarnapp.test.qrcode.ui

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class QrCodeTestFragmentModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(QrCodeTestFragmentViewModel::class)
    abstract fun testQrCodeViewModel(
        factory: QrCodeTestFragmentViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
