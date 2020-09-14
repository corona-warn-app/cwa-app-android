package de.rki.coronawarnapp.test.risklevel.ui

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class TestRiskLevelCalculationFragmentModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(TestRiskLevelCalculationFragmentCWAViewModel::class)
    abstract fun testRiskLevelFragment(factory: TestRiskLevelCalculationFragmentCWAViewModel.Factory): CWAViewModelFactory<out CWAViewModel>
}
