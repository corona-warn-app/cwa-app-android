package de.rki.coronawarnapp.ui.presencetracing.organizer.warn.info

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class TraceLocationWarnInfoFragmentModule {

    @Binds
    @IntoMap
    @CWAViewModelKey(TraceLocationWarnInfoViewModel::class)
    abstract fun traceLocationCreateViewModel(factory: TraceLocationWarnInfoViewModel.Factory):
        CWAViewModelFactory<out CWAViewModel>
}
