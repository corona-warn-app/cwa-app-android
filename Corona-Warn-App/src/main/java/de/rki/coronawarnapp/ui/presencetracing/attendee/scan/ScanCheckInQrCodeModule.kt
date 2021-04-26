package de.rki.coronawarnapp.ui.presencetracing.attendee.scan

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class ScanCheckInQrCodeModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(ScanCheckInQrCodeViewModel::class)
    abstract fun scanCheckInQrCodeFragment(
        factory: ScanCheckInQrCodeViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
