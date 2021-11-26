package de.rki.coronawarnapp.dccticketing

import dagger.Module
import de.rki.coronawarnapp.dccticketing.core.DccTicketingCoreModule
import de.rki.coronawarnapp.dccticketing.ui.validationresult.DccTicketingFragmentModule

@Module(includes = [DccTicketingFragmentModule::class, DccTicketingCoreModule::class])
class DccTicketingModule {
    // =^..^=
}
