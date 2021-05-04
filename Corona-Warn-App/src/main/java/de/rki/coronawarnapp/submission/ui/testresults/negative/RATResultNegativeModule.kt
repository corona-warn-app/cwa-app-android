package de.rki.coronawarnapp.submission.ui.testresults.negative

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class RATResultNegativeModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(RATResultNegativeViewModel::class)
    abstract fun ratResultNegativeFragment(
        factory: RATResultNegativeViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
