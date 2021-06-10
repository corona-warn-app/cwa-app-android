package de.rki.coronawarnapp.ui.submission.covidcertificate

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class RequestCovidCertificateFragmentModule {

    @Binds
    @IntoMap
    @CWAViewModelKey(RequestCovidCertificateViewModel::class)
    abstract fun requestGreenCertificateFragment(
        factory: RequestCovidCertificateViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
