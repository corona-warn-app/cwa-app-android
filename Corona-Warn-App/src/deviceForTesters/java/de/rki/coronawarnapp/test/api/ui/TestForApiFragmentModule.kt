package de.rki.coronawarnapp.test.api.ui

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class TestForApiFragmentModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(TestForApiFragmentViewModel::class)
    abstract fun testRiskLevelFragment(factory: TestForApiFragmentViewModel.Factory): CWAViewModelFactory<out CWAViewModel>
}
