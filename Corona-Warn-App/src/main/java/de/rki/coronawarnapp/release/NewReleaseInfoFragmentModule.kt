package de.rki.coronawarnapp.release

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
    @CWAViewModelKey(NewReleaseInfoViewModel::class)
    abstract fun newReleaseInfoFragmentVM(
        factory: NewReleaseInfoViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
