package de.rki.coronawarnapp.test.eventregistration.ui.createevent

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class CreateEventTestFragmentModule {

    @Binds
    @IntoMap
    @CWAViewModelKey(CreateEventTestViewModel::class)
    abstract fun testCreateEventFragment(
        factory: CreateEventTestViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
