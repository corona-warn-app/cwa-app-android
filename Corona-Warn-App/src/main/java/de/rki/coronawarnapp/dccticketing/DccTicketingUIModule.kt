package de.rki.coronawarnapp.dccticketing

import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.dccticketing.ui.consent.one.DccTicketingConsentOneFragment
import de.rki.coronawarnapp.dccticketing.ui.consent.one.DccTicketingConsentOneFragmentModule
import de.rki.coronawarnapp.dccticketing.ui.validationresult.DccTicketingFragmentModule
import de.rki.coronawarnapp.dccticketing.ui.validationresult.DccTicketingValidationSuccessFragment

@Module
abstract class DccTicketingUIModule {

    @ContributesAndroidInjector(modules = [DccTicketingConsentOneFragmentModule::class])
    abstract fun dccTicketingConsentOneFragment(): DccTicketingConsentOneFragment

    @ContributesAndroidInjector(modules = [DccTicketingFragmentModule::class])
    abstract fun dccTicketingValidationSuccessFragment(): DccTicketingValidationSuccessFragment

}
