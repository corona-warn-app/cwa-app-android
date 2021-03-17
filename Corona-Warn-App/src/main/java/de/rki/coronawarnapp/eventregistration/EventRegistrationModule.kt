package de.rki.coronawarnapp.eventregistration

import dagger.Binds
import dagger.Module
import de.rki.coronawarnapp.eventregistration.checkins.download.DownloadedCheckInsRepo
import de.rki.coronawarnapp.eventregistration.checkins.download.FakeDownloadedCheckInsRepo
import de.rki.coronawarnapp.eventregistration.checkins.qrcode.DefaultQRCodeVerifier
import de.rki.coronawarnapp.eventregistration.checkins.qrcode.QRCodeVerifier
import de.rki.coronawarnapp.eventregistration.events.server.CreateTraceLocationModule
import de.rki.coronawarnapp.eventregistration.storage.repo.DefaultTraceLocationRepository
import de.rki.coronawarnapp.eventregistration.storage.repo.TraceLocationRepository
import de.rki.coronawarnapp.eventregistration.checkins.CheckInsTransformer
import de.rki.coronawarnapp.eventregistration.checkins.DefaultCheckInsTransformer

@Module(
    includes = [
        CreateTraceLocationModule::class
    ]
)
abstract class EventRegistrationModule {

    @Binds
    abstract fun checkInsTransformer(transformer: DefaultCheckInsTransformer): CheckInsTransformer

    @Binds
    abstract fun qrCodeVerifier(qrCodeVerifier: DefaultQRCodeVerifier): QRCodeVerifier

    @Binds
    abstract fun traceLocationRepository(defaultTraceLocationRepo: DefaultTraceLocationRepository):
        TraceLocationRepository

    @Binds
    abstract fun downloadedCheckInsRepo(repository: FakeDownloadedCheckInsRepo): DownloadedCheckInsRepo
}
