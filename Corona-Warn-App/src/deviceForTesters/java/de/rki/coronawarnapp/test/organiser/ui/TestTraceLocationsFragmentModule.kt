package de.rki.coronawarnapp.test.organiser.ui

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class TestTraceLocationsFragmentModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(TestTraceLocationsViewModel::class)
    abstract fun traceLocationsViewModel(
        factory: TestTraceLocationsViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
