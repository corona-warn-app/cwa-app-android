package de.rki.coronawarnapp.dccticketing.ui.consent.one

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class DccTicketingConsentOneFragmentModule {

    @Binds
    @IntoMap
    @CWAViewModelKey(DccTicketingConsentOneViewModel::class)
    abstract fun dccTicketingConsentOneFragment(
        factory: DccTicketingConsentOneViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
