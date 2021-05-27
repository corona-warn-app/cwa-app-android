package de.rki.coronawarnapp.greencertificate.ui.certificates.details

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class CertificateDetailsModule {

    @Binds
    @IntoMap
    @CWAViewModelKey(CertificateDetailsViewModel::class)
    abstract fun vaccinationDetailsFragment(
        factory: CertificateDetailsViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
