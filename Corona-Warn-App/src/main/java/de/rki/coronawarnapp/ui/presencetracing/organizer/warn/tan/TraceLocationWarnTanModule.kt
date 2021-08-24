package de.rki.coronawarnapp.ui.presencetracing.organizer.warn.tan

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class TraceLocationWarnTanModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(TraceLocationWarnTanViewModel::class)
    abstract fun traceLocationTanFragment(
        factory: TraceLocationWarnTanViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
