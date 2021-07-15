package de.rki.coronawarnapp.covidcertificate.person.ui.details

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class PersonDetailsFragmentModule {

    @Binds
    @IntoMap
    @CWAViewModelKey(PersonDetailsViewModel::class)
    abstract fun personDetailsFragment(
        factory: PersonDetailsViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
