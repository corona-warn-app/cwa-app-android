package de.rki.coronawarnapp.vaccination.ui.scan

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class VaccinationQrCodeScanModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(VaccinationQrCodeScanViewModel::class)
    abstract fun vaccinationQrCodeScanFragment(
        factory: VaccinationQrCodeScanViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
