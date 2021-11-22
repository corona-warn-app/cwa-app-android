package de.rki.coronawarnapp.dccticketing

import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.dccticketing.ui.consent.one.DccTicketingConsentOneFragment
import de.rki.coronawarnapp.dccticketing.ui.consent.one.DccTicketingConsentOneFragmentModule

@Module
internal abstract class DccTicketingFragmentModule {

    @ContributesAndroidInjector(modules = [DccTicketingConsentOneFragmentModule::class])
    abstract fun dccTicketingConsentOneFragment(): DccTicketingConsentOneFragment
}
