package de.rki.coronawarnapp.tracing.ui.details

import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class TracingDetailsFragmentModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(TracingDetailsFragmentViewModel::class)
    abstract fun homeFragment(
        factory: TracingDetailsFragmentViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>

    @ContributesAndroidInjector
    abstract fun riskDetails(): TracingDetailsFragment
}
