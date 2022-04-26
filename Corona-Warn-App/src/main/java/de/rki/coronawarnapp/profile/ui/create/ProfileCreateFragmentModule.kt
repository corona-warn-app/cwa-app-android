package de.rki.coronawarnapp.profile.ui.create

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class ProfileCreateFragmentModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(ProfileCreateFragmentViewModel::class)
    abstract fun profileCreateFragmentViewModel(
        factory: ProfileCreateFragmentViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
