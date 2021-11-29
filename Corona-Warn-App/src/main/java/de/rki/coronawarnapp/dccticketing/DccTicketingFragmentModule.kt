package de.rki.coronawarnapp.dccticketing

import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.dccticketing.ui.certificateselection.DccTicketingCertificateSelectionFragment
import de.rki.coronawarnapp.dccticketing.ui.certificateselection.DccTicketingCertificateSelectionFragmentModule
import de.rki.coronawarnapp.dccticketing.ui.consent.one.DccTicketingConsentOneFragment
import de.rki.coronawarnapp.dccticketing.ui.consent.one.DccTicketingConsentOneFragmentModule
import de.rki.coronawarnapp.dccticketing.ui.consent.two.DccTicketingConsentTwoFragment
import de.rki.coronawarnapp.dccticketing.ui.consent.two.DccTicketingConsentTwoFragmentModule

@Module
internal abstract class DccTicketingFragmentModule {

    @ContributesAndroidInjector(modules = [DccTicketingConsentOneFragmentModule::class])
    abstract fun dccTicketingConsentOneFragment(): DccTicketingConsentOneFragment

    @ContributesAndroidInjector(modules = [DccTicketingCertificateSelectionFragmentModule::class])
    abstract fun dccTicketingCertificateSelectionFragment(): DccTicketingCertificateSelectionFragment

    @ContributesAndroidInjector(modules = [DccTicketingConsentTwoFragmentModule::class])
    abstract fun dccTicketingConsentTwoFragment(): DccTicketingConsentTwoFragment
}
