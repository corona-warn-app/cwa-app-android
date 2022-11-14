package de.rki.coronawarnapp.srs.ui.checkins

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class SrsCheckinsFragmentModule {

    @Binds
    @IntoMap
    @CWAViewModelKey(SrsCheckinsFragmentViewModel::class)
    abstract fun srsCheckinsFragmentViewModel(
        factory: SrsCheckinsFragmentViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
