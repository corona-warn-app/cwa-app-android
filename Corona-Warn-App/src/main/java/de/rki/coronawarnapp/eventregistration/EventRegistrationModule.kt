package de.rki.coronawarnapp.eventregistration

import dagger.Binds
import dagger.Module
import de.rki.coronawarnapp.eventregistration.checkins.qrcode.DefaultQRCodeVerifier
import de.rki.coronawarnapp.eventregistration.checkins.qrcode.QRCodeVerifier
import de.rki.coronawarnapp.eventregistration.storage.repo.DefaultTraceLocationRepository
import de.rki.coronawarnapp.eventregistration.storage.repo.TraceLocationRepository

@Module
abstract class EventRegistrationModule {

    @Binds
    abstract fun qrCodeVerifier(qrCodeVerifier: DefaultQRCodeVerifier): QRCodeVerifier

    @Binds
    abstract fun traceLocationRepository(defaultTraceLocationRepo: DefaultTraceLocationRepository)
        : TraceLocationRepository
}
