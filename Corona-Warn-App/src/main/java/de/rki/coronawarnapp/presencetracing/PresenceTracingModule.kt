package de.rki.coronawarnapp.presencetracing

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet
import de.rki.coronawarnapp.environment.presencetracing.qrcodeposter.QrCodePosterTemplateModule
import de.rki.coronawarnapp.presencetracing.checkins.CheckInRepository
import de.rki.coronawarnapp.presencetracing.organizer.submission.OrganizerSubmissionModule
import de.rki.coronawarnapp.presencetracing.storage.repo.DefaultTraceLocationRepository
import de.rki.coronawarnapp.presencetracing.storage.repo.TraceLocationRepository
import de.rki.coronawarnapp.presencetracing.warning.PresenceTracingWarningModule
import de.rki.coronawarnapp.ui.presencetracing.TraceLocationPreferences
import de.rki.coronawarnapp.util.reset.Resettable

@Module(
    includes = [
        QrCodePosterTemplateModule::class,
        PresenceTracingWarningModule::class,
        OrganizerSubmissionModule::class,
        PresenceTracingModule.ResetModule::class,
    ]
)
interface PresenceTracingModule {

    @Binds
    fun traceLocationRepository(
        defaultTraceLocationRepo: DefaultTraceLocationRepository
    ): TraceLocationRepository

    @Module
    interface ResetModule {

        @Binds
        @IntoSet
        fun bindResettableTraceLocationPreferences(resettable: TraceLocationPreferences): Resettable

        @Binds
        @IntoSet
        fun bindResettableTraceLocationSettings(resettable: TraceLocationSettings): Resettable

        @Binds
        @IntoSet
        fun bindResettableCheckInRepository(resettable: CheckInRepository): Resettable

        @Binds
        @IntoSet
        fun bindResettableTraceLocationRepository(resettable: DefaultTraceLocationRepository): Resettable
    }
}
