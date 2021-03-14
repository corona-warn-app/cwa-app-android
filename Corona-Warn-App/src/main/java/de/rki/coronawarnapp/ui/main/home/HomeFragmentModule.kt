package de.rki.coronawarnapp.ui.main.home

import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class HomeFragmentModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(HomeFragmentViewModel::class)
    abstract fun homeFragment(
        factory: HomeFragmentViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>

    @ContributesAndroidInjector
    abstract fun homeScreen(): HomeFragment
}
