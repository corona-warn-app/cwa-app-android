package de.rki.coronawarnapp.main

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet
import de.rki.coronawarnapp.util.reset.Resettable

@Module
interface MainModule {

    @Binds
    @IntoSet
    fun bindResettableCWASettings(resettable: CWASettings): Resettable
}
