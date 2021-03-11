package de.rki.coronawarnapp.eventregistration

import dagger.Binds
import dagger.Module
import dagger.Provides
import de.rki.coronawarnapp.eventregistration.storage.repo.DefaultTraceLocationRepository
import de.rki.coronawarnapp.eventregistration.storage.repo.TraceLocationRepository
import de.rki.coronawarnapp.eventregistration.checkins.qrcode.DefaultQRCodeVerifier
import de.rki.coronawarnapp.eventregistration.checkins.qrcode.QRCodeVerifier
import javax.inject.Singleton

@Module
abstract class EventRegistrationModule {
    @Binds
    abstract fun qrCodeVerifier(qrCodeVerifier: DefaultQRCodeVerifier): QRCodeVerifier

    @Singleton
    @Provides
    fun traceLocationRepository(defaultTraceLocationRepo: DefaultTraceLocationRepository): TraceLocationRepository =
        defaultTraceLocationRepo
}
