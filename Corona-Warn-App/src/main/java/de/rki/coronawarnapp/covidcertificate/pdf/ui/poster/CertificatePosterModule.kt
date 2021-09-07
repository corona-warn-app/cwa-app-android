package de.rki.coronawarnapp.covidcertificate.pdf.ui.poster

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class CertificatePosterModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(CertificatePosterViewModel::class)
    abstract fun certificatePosterFragment(
        factory: CertificatePosterViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
