package de.rki.coronawarnapp.test.organiser.ui

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class TraceLocationOrganiserQrCodesListTestFragmentModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(TraceLocationOrganiserQrCodesListTestFragmentViewModel::class)
    abstract fun organiserQrCodesListViewModel(
        factory: TraceLocationOrganiserQrCodesListTestFragmentViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
