package de.rki.coronawarnapp.ui.coronatest.rat.profile.create

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class RATProfileCreateFragmentModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(RATProfileCreateFragmentViewModel::class)
    abstract fun ratProfileCreateFragmentViewModel(
        factory: RATProfileCreateFragmentViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
