package de.rki.coronawarnapp.test.dsc.ui

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class DscModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(DscTestViewModel::class)
    abstract fun dscTest(factory: DscTestViewModel.Factory): CWAViewModelFactory<out CWAViewModel>
}
