package de.rki.coronawarnapp.dccticketing.ui.validationresult

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
internal abstract class DccTicketingFragmentModule {

    @Binds
    @IntoMap
    @CWAViewModelKey(DccTicketingValidationResultViewModel::class)
    abstract fun dccTicketingValidationFragment(
        factory: DccTicketingValidationResultViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
