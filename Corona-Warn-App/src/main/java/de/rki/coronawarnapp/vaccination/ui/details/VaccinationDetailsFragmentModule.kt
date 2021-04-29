package de.rki.coronawarnapp.vaccination.ui.details

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class VaccinationDetailsFragmentModule {

    @Binds
    @IntoMap
    @CWAViewModelKey(VaccinationDetailsViewModel::class)
    abstract fun vaccinationDetailsFragment(
        factory: VaccinationDetailsViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
