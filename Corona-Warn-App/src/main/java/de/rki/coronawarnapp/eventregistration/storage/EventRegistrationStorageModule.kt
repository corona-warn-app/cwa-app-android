package de.rki.coronawarnapp.eventregistration.storage

import dagger.Module
import dagger.Provides
import de.rki.coronawarnapp.eventregistration.storage.repo.DefaultTraceLocationRepository
import de.rki.coronawarnapp.eventregistration.storage.repo.TraceLocationRepository
import javax.inject.Singleton

@Module
class EventRegistrationStorageModule {

    @Singleton
    @Provides
    fun traceLocationRepository(defaultTraceLocationRepo: DefaultTraceLocationRepository): TraceLocationRepository =
        defaultTraceLocationRepo
}
