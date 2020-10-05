package de.rki.coronawarnapp.test.menu.ui

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class TestMenuFragmentModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(TestMenuFragmentViewModel::class)
    abstract fun testRiskLevelFragment(factory: TestMenuFragmentViewModel.Factory): CWAViewModelFactory<out CWAViewModel>
}
