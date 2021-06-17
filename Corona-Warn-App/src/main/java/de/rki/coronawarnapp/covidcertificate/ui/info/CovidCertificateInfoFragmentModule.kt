package de.rki.coronawarnapp.covidcertificate.ui.info

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class CovidCertificateInfoFragmentModule {

    @Binds
    @IntoMap
    @CWAViewModelKey(CovidCertificateInfoViewModel::class)
    abstract fun vaccinationDetailsFragment(
        factory: CovidCertificateInfoViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
