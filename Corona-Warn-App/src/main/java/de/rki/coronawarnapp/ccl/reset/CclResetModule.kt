package de.rki.coronawarnapp.ccl.reset

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet
import de.rki.coronawarnapp.util.reset.Resettable

@Module
interface CclResetModule {

    @Binds
    @IntoSet
    fun bindResettableCclReset(resettable: CclReset): Resettable
}
