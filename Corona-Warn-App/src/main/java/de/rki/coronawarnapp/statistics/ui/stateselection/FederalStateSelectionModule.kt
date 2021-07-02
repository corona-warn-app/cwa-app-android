package de.rki.coronawarnapp.statistics.ui.stateselection

import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class FederalStateSelectionModule {

    @ContributesAndroidInjector
    abstract fun userInput(): FederalStateSelectionFragment

    @Binds
    @IntoMap
    @CWAViewModelKey(FederalStateSelectionViewModel::class)
    abstract fun create(
        factory: FederalStateSelectionViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
