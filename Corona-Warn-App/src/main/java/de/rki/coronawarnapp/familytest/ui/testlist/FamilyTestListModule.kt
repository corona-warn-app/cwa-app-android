package de.rki.coronawarnapp.familytest.ui.testlist

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class FamilyTestListModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(FamilyTestListViewModel::class)
    abstract fun familyTestListFragment(
        factory: FamilyTestListViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
