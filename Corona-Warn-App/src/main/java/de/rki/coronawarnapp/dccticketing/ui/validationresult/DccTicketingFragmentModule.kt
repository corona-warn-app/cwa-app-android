package de.rki.coronawarnapp.dccticketing.ui.validationresult

import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.dccticketing.ui.consent.one.DccTicketingConsentOneFragment
import de.rki.coronawarnapp.dccticketing.ui.consent.one.DccTicketingConsentOneFragmentModule
import de.rki.coronawarnapp.dccticketing.ui.validationresult.success.DccTicketingValidationSuccessFragment
import de.rki.coronawarnapp.dccticketing.ui.validationresult.success.DccTicketingValidationViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
internal abstract class DccTicketingFragmentModule {

    @Binds
    @IntoMap
    @CWAViewModelKey(DccTicketingValidationViewModel::class)
    abstract fun dccTicketingValidationFragment(
        factory: DccTicketingValidationViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
