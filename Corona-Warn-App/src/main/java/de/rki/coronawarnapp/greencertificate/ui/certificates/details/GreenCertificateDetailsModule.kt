package de.rki.coronawarnapp.greencertificate.ui.certificates.details

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class GreenCertificateDetailsModule {

    @Binds
    @IntoMap
    @CWAViewModelKey(GreenCertificateDetailsViewModel::class)
    abstract fun greenCertificateDetailsFragment(
        factory: GreenCertificateDetailsViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
