package de.rki.coronawarnapp.presencetracing

import dagger.Binds
import dagger.Module
import de.rki.coronawarnapp.environment.presencetracing.qrcodeposter.QrCodePosterTemplateModule
import de.rki.coronawarnapp.presencetracing.organizer.submission.OrganizerSubmissionModule
import de.rki.coronawarnapp.presencetracing.storage.repo.DefaultTraceLocationRepository
import de.rki.coronawarnapp.presencetracing.storage.repo.TraceLocationRepository
import de.rki.coronawarnapp.presencetracing.warning.PresenceTracingWarningModule

@Module(
    includes = [
        QrCodePosterTemplateModule::class,
        PresenceTracingWarningModule::class,
        OrganizerSubmissionModule::class,
    ]
)
abstract class PresenceTracingModule {

    @Binds
    abstract fun traceLocationRepository(
        defaultTraceLocationRepo: DefaultTraceLocationRepository
    ): TraceLocationRepository
}
