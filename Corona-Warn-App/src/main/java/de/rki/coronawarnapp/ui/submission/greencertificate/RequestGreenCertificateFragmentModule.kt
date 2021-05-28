package de.rki.coronawarnapp.ui.submission.greencertificate

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class RequestGreenCertificateFragmentModule {

    @Binds
    @IntoMap
    @CWAViewModelKey(RequestGreenCertificateViewModel::class)
    abstract fun requestGreenCertificateFragment(
        factory: RequestGreenCertificateViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
