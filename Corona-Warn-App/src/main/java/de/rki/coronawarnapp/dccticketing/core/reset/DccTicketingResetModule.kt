package de.rki.coronawarnapp.dccticketing.core.reset

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet
import de.rki.coronawarnapp.util.reset.Resettable

@Module
interface DccTicketingResetModule {

    @Binds
    @IntoSet
    fun bindResettableDccTicketingReset(resettable: DccTicketingReset): Resettable
}
