package de.rki.coronawarnapp.test.coronatest.ui

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class CoronaTestTestFragmentModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(CoronaTestTestFragmentViewModel::class)
    abstract fun coronaTest(
        factory: CoronaTestTestFragmentViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
