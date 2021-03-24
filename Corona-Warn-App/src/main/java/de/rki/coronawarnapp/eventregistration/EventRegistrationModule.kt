package de.rki.coronawarnapp.eventregistration

import dagger.Binds
import dagger.Module
import de.rki.coronawarnapp.environment.eventregistration.CreateTraceLocationModule
import de.rki.coronawarnapp.eventregistration.checkins.download.FakeTraceTimeIntervalWarningRepository
import de.rki.coronawarnapp.eventregistration.checkins.download.TraceTimeIntervalWarningRepository
import de.rki.coronawarnapp.eventregistration.storage.repo.DefaultTraceLocationRepository
import de.rki.coronawarnapp.eventregistration.storage.repo.TraceLocationRepository

@Module(
    includes = [
        CreateTraceLocationModule::class
    ]
)
abstract class EventRegistrationModule {

    @Binds
    abstract fun traceLocationRepository(defaultTraceLocationRepo: DefaultTraceLocationRepository):
        TraceLocationRepository

    @Binds
    abstract fun traceTimeIntervalWarningRepository(repository: FakeTraceTimeIntervalWarningRepository):
        TraceTimeIntervalWarningRepository
}
