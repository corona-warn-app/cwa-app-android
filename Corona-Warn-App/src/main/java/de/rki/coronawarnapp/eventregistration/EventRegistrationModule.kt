package de.rki.coronawarnapp.eventregistration

import dagger.Binds
import dagger.Module
import de.rki.coronawarnapp.eventregistration.checkins.qrcode.DefaultQRCodeVerifier
import de.rki.coronawarnapp.eventregistration.checkins.qrcode.QRCodeVerifier

@Module
abstract class EventRegistrationModule {
    @Binds
    abstract fun qrCodeVerifier(qrCodeVerifier: DefaultQRCodeVerifier): QRCodeVerifier
}
