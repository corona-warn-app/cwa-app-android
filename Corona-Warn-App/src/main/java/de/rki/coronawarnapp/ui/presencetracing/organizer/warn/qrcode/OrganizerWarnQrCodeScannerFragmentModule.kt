package de.rki.coronawarnapp.ui.presencetracing.organizer.warn.qrcode

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class OrganizerWarnQrCodeScannerFragmentModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(OrganizerWarnQrCodeScannerViewModel::class)
    abstract fun organizerWarnQrCodeScannerViewModel(
        factory: OrganizerWarnQrCodeScannerViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
