package de.rki.coronawarnapp.familytest.ui.selection

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class TestRegistrationSelectionModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(TestRegistrationSelectionViewModel::class)
    abstract fun testRegistrationSelectionFragment(
        factory: TestRegistrationSelectionViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
