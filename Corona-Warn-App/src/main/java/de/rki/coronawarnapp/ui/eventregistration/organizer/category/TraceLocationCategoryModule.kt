package de.rki.coronawarnapp.ui.eventregistration.organizer.category

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class TraceLocationCategoryModule {

    @Binds
    @IntoMap
    @CWAViewModelKey(TraceLocationCategoryViewModel::class)
    abstract fun traceLocationCategoryViewModel(factory: TraceLocationCategoryViewModel.Factory):
        CWAViewModelFactory<out CWAViewModel>
}
