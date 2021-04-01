package de.rki.coronawarnapp.eventregistration

import dagger.Binds
import dagger.Module
import de.rki.coronawarnapp.environment.eventregistration.qrcodeposter.QrCodePosterTemplateModule
import de.rki.coronawarnapp.eventregistration.storage.repo.DefaultTraceLocationRepository
import de.rki.coronawarnapp.eventregistration.storage.repo.TraceLocationRepository
import de.rki.coronawarnapp.presencetracing.warning.PresenceTracingWarningModule

@Module(
    includes = [
        QrCodePosterTemplateModule::class,
        PresenceTracingWarningModule::class,
    ]
)
abstract class EventRegistrationModule {

    @Binds
    abstract fun traceLocationRepository(
        defaultTraceLocationRepo: DefaultTraceLocationRepository
    ): TraceLocationRepository
}
