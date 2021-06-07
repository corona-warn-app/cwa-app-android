package de.rki.coronawarnapp.greencertificate.ui.certificates.details

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class CovidCertificateDetailsModule {

    @Binds
    @IntoMap
    @CWAViewModelKey(CovidCertificateDetailsViewModel::class)
    abstract fun greenCertificateDetailsFragment(
        factory: CovidCertificateDetailsViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
