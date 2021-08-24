package de.rki.coronawarnapp.ui.presencetracing.organizer.warn.duration

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class TraceLocationWarnDurationFragmentModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(TraceLocationWarnDurationViewModel::class)
    abstract fun traceLocationsWarnDurationViewModel(
        factory: TraceLocationWarnDurationViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
