package de.rki.coronawarnapp.eventregistration

import dagger.Binds
import dagger.Module
import de.rki.coronawarnapp.environment.eventregistration.CreateTraceLocationModule
import de.rki.coronawarnapp.eventregistration.storage.repo.DefaultTraceLocationRepository
import de.rki.coronawarnapp.eventregistration.storage.repo.TraceLocationRepository
import de.rki.coronawarnapp.presencetracing.warning.PresenceTracingWarningModule

@Module(
    includes = [
        CreateTraceLocationModule::class,
        PresenceTracingWarningModule::class,
    ]
)
abstract class EventRegistrationModule {

    @Binds
    abstract fun traceLocationRepository(
        defaultTraceLocationRepo: DefaultTraceLocationRepository
    ): TraceLocationRepository
}
