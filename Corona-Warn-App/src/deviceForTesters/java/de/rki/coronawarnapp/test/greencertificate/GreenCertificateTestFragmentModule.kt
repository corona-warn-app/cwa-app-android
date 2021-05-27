package de.rki.coronawarnapp.test.greencertificate

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class GreenCertificateTestFragmentModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(GreenCertificateTestFragmentViewModel::class)
    abstract fun testVaccinationFragment(
        factory: GreenCertificateTestFragmentViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
