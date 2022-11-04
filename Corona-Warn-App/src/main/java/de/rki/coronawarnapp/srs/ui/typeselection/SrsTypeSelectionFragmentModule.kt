package de.rki.coronawarnapp.srs.ui.typeselection

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class SrsTypeSelectionFragmentModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(SrsTypeSelectionFragmentViewModel::class)
    abstract fun srsTypeSelectionFragmentViewModel(
        factory: SrsTypeSelectionFragmentViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
