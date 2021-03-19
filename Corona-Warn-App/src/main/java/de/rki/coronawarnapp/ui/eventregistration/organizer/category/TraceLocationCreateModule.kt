package de.rki.coronawarnapp.ui.eventregistration.organizer.category

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.eventregistration.events.ui.category.TraceLocationCreateViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class TraceLocationCreateModule {

    @Binds
    @IntoMap
    @CWAViewModelKey(TraceLocationCreateViewModel::class)
    abstract fun traceLocationCreateViewModel(factory: TraceLocationCreateViewModel.Factory):
        CWAViewModelFactory<out CWAViewModel>
}
