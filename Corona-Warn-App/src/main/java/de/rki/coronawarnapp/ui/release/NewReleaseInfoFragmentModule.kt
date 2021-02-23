package de.rki.coronawarnapp.ui.release

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class NewReleaseInfoFragmentModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(NewReleaseInfoFragmentViewModel::class)
    abstract fun newReleaseInfoFragmentVM(
        factory: NewReleaseInfoFragmentViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
