package de.rki.coronawarnapp.test.eventregistration.ui

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class EventRegistrationTestFragmentModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(EventRegistrationTestFragmentViewModel::class)
    abstract fun testEventRegistrationFragment(
        factory: EventRegistrationTestFragmentViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
