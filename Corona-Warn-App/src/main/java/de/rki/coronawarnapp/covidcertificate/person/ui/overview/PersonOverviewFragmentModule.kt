package de.rki.coronawarnapp.covidcertificate.person.ui.overview

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class PersonOverviewFragmentModule {

    @Binds
    @IntoMap
    @CWAViewModelKey(PersonOverviewViewModel::class)
    abstract fun personOverviewFragment(
        factory: PersonOverviewViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
