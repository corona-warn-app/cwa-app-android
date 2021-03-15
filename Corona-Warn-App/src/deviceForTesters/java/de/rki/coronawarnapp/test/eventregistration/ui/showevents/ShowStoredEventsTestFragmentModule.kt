package de.rki.coronawarnapp.test.eventregistration.ui.showevents

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class ShowStoredEventsTestFragmentModule {

    @Binds
    @IntoMap
    @CWAViewModelKey(ShowStoredEventsTestViewModel::class)
    abstract fun testStoredEventsFragment(
        factory: ShowStoredEventsTestViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
