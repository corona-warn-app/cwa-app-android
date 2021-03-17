package de.rki.coronawarnapp.eventregistration.events.ui.category

import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
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

    @ContributesAndroidInjector
    abstract fun traceLocationCategoryFragment(): TraceLocationCategoryFragment
}
