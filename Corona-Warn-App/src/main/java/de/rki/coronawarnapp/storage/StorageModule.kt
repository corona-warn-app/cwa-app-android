package de.rki.coronawarnapp.storage

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet
import de.rki.coronawarnapp.util.reset.Resettable

@Module
interface StorageModule {

    @Binds
    @IntoSet
    fun bindResettableOnboardingSettings(resettable: OnboardingSettings): Resettable

    @Binds
    @IntoSet
    fun bindResettableTracingSettings(resettable: TracingSettings): Resettable
}
