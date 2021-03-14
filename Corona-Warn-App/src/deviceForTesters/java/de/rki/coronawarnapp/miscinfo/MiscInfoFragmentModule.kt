package de.rki.coronawarnapp.miscinfo

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class MiscInfoFragmentModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(MiscInfoFragmentViewModel::class)
    abstract fun testTaskControllerFragment(
        factory: MiscInfoFragmentViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
