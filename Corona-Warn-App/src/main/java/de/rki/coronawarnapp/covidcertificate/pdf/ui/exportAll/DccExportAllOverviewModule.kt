package de.rki.coronawarnapp.covidcertificate.pdf.ui.exportAll

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class DccExportAllOverviewModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(DccExportAllOverviewViewModel::class)
    abstract fun certificatePosterFragment(
        factory: DccExportAllOverviewViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
