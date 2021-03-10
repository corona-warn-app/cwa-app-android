package de.rki.coronawarnapp.eventregistration

import dagger.Binds
import dagger.Module
import de.rki.coronawarnapp.eventregistration.checkins.qrcode.DefaultQRCodeVerifier
import de.rki.coronawarnapp.eventregistration.checkins.qrcode.QRCodeVerifier
import de.rki.coronawarnapp.eventregistration.checkins.CheckInsMapper
import de.rki.coronawarnapp.eventregistration.checkins.DefaultCheckInsMapper

@Suppress("EmptyClassBlock")
@Module
abstract class EventRegistrationModule {
    @Binds
    abstract fun checkInsTransformer(transformer: DefaultCheckInsMapper): CheckInsMapper

    @Binds
    abstract fun qrCodeVerifier(qrCodeVerifier: DefaultQRCodeVerifier): QRCodeVerifier
}
