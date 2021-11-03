package de.rki.coronawarnapp.reyclebin.ui

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class RecyclerBinOverviewFragmentModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(RecyclerBinOverviewViewModel::class)
    abstract fun recyclerBinOverviewFragmentVM(
        factory: RecyclerBinOverviewViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
