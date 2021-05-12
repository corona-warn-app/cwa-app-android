package de.rki.coronawarnapp.vaccination.ui.consent

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class VaccinationConsentFragmentModule {

    @Binds
    @IntoMap
    @CWAViewModelKey(VaccinationConsentViewModel::class)
    abstract fun vaccinationDetailsFragment(
        factory: VaccinationConsentViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
