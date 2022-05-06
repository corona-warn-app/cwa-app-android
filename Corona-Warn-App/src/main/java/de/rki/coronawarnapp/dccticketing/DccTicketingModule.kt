package de.rki.coronawarnapp.dccticketing

import dagger.Module
import de.rki.coronawarnapp.dccticketing.core.DccTicketingCoreModule

@Module(includes = [DccTicketingCoreModule::class])
interface DccTicketingModule {
    // =^..^=
}
