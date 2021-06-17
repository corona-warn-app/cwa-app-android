package de.rki.coronawarnapp.covidcertificate.test.ui.details

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class TestCertificateDetailsModule {

    @Binds
    @IntoMap
    @CWAViewModelKey(TestCertificateDetailsViewModel::class)
    abstract fun testCertificateDetailsFragment(
        factory: TestCertificateDetailsViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
