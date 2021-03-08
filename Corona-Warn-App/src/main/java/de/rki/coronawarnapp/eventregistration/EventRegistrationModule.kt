package de.rki.coronawarnapp.eventregistration

import dagger.Module
import dagger.Provides
import de.rki.coronawarnapp.eventregistration.storage.repo.DefaultTraceLocationRepository
import de.rki.coronawarnapp.eventregistration.storage.repo.TraceLocationRepository
import javax.inject.Singleton

@Suppress("EmptyClassBlock")
@Module
class EventRegistrationModule {

    @Singleton
    @Provides
    fun traceLocationRepository(defaultTraceLocationRepo: DefaultTraceLocationRepository): TraceLocationRepository =
        defaultTraceLocationRepo
}
