package de.rki.coronawarnapp.ui.main.overview

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class MainOverviewFragmentModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(MainOverviewViewModel::class)
    abstract fun mainOverviewViewFragment(
        factory: MainOverviewViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
