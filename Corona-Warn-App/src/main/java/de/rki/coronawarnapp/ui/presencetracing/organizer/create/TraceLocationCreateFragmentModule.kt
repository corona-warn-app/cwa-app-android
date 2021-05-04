package de.rki.coronawarnapp.ui.presencetracing.organizer.create

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class TraceLocationCreateFragmentModule {

    @Binds
    @IntoMap
    @CWAViewModelKey(TraceLocationCreateViewModel::class)
    abstract fun traceLocationCreateViewModel(factory: TraceLocationCreateViewModel.Factory):
        CWAViewModelFactory<out CWAViewModel>
}
