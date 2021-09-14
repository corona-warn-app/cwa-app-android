package de.rki.coronawarnapp.qrcode.ui

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class QrCodeScannerFragmentModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(QrCodeScannerViewModel::class)
    abstract fun submissionQRCodeScanFragment(
        factory: QrCodeScannerViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
