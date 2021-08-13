package de.rki.coronawarnapp.ui.presencetracing.organizer.warn.list

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class TraceLocationsWarnFragmentModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(TraceLocationsWarnViewModel::class)
    abstract fun traceLocationsViewModel(
        factory: TraceLocationsWarnViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
