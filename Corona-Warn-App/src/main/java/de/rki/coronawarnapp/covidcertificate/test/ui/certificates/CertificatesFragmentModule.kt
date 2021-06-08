package de.rki.coronawarnapp.covidcertificate.test.ui.certificates

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class CertificatesFragmentModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(CertificatesViewModel::class)
    abstract fun certificatesFragment(
        factory: CertificatesViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
