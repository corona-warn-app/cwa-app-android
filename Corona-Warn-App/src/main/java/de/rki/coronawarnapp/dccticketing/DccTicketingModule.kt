package de.rki.coronawarnapp.dccticketing

import dagger.Module
import de.rki.coronawarnapp.dccticketing.core.DccTicketingCoreModule
import de.rki.coronawarnapp.dccticketing.ui.DccTicketingUIModule

@Module(includes = [DccTicketingUIModule::class, DccTicketingCoreModule::class])
class DccTicketingModule {
}
