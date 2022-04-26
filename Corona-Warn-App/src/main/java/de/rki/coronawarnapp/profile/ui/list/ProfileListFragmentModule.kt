package de.rki.coronawarnapp.profile.ui.list

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class ProfileListFragmentModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(ProfileListViewModel::class)
    abstract fun profileListViewModel(
        factory: ProfileListViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
