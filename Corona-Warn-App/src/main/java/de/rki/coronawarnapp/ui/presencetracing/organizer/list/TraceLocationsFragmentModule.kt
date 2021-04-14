package de.rki.coronawarnapp.ui.presencetracing.organizer.list

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class TraceLocationsFragmentModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(TraceLocationsViewModel::class)
    abstract fun traceLocationsViewModel(
        factory: TraceLocationsViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
