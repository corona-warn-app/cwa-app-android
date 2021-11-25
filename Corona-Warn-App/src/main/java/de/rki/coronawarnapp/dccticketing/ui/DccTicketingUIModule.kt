package de.rki.coronawarnapp.dccticketing.ui

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.dccticketing.ui.validationresult.success.DccTicketingValidationViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
class DccTicketingUIModule {

//    @Binds
//    @IntoMap
//    @CWAViewModelKey(DccTicketingValidationViewModel::class)
//    abstract fun dccTicketingValidationViewModel(
//        factory: DccTicketingValidationViewModel.Factory
//    ): CWAViewModelFactory<out CWAViewModel>

}
