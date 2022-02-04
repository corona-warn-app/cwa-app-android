package de.rki.coronawarnapp.covidcertificate.boosterinfodetails

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class BoosterInfoDetailsFragmentModule {

    @Binds
    @IntoMap
    @CWAViewModelKey(BoosterInfoDetailsViewModel::class)
    abstract fun boosterInfoDetailsFragment(
        factory: BoosterInfoDetailsViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
