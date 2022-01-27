package de.rki.coronawarnapp.test.ccl

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class CCLTestModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(CCLTestViewModel::class)
    abstract fun dscTest(factory: CCLTestViewModel.Factory): CWAViewModelFactory<out CWAViewModel>
}
