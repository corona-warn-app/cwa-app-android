package de.rki.coronawarnapp.test.eol

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class EolTestModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(EolTestViewModel::class)
    abstract fun eolTest(factory: EolTestViewModel.Factory): CWAViewModelFactory<out CWAViewModel>
}
