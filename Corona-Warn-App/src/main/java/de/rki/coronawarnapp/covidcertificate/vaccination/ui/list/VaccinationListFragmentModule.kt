package de.rki.coronawarnapp.covidcertificate.vaccination.ui.list

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class VaccinationListFragmentModule {

    @Binds
    @IntoMap
    @CWAViewModelKey(VaccinationListViewModel::class)
    abstract fun vaccinationListFragment(
        factory: VaccinationListViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
