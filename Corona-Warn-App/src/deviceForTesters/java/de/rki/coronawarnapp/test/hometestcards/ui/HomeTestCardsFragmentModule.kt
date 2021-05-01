package de.rki.coronawarnapp.test.hometestcards.ui

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class HomeTestCardsFragmentModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(HomeTestCardsFragmentViewModel::class)
    abstract fun testHomeTestCardsFragment(
        factory: HomeTestCardsFragmentViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
