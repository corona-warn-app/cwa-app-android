package de.rki.coronawarnapp.test.presencetracing.ui

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class PresenceTracingTestFragmentModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(PresenceTracingTestViewModel::class)
    abstract fun testPresenceTracingFragment(
        factory: PresenceTracingTestViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
