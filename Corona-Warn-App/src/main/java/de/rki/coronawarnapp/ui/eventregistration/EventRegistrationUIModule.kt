package de.rki.coronawarnapp.ui.eventregistration

import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.ui.eventregistration.checkin.ConfirmCheckInFragment
import de.rki.coronawarnapp.ui.eventregistration.checkin.ConfirmCheckInModule
import de.rki.coronawarnapp.ui.eventregistration.scan.ScanCheckInQrCodeFragment
import de.rki.coronawarnapp.ui.eventregistration.scan.ScanCheckInQrCodeModule

@Module
internal abstract class EventRegistrationUIModule {

    @ContributesAndroidInjector(modules = [ScanCheckInQrCodeModule::class])
    abstract fun scanCheckInQrCodeFragment(): ScanCheckInQrCodeFragment

    @ContributesAndroidInjector(modules = [ConfirmCheckInModule::class])
    abstract fun confirmCheckInFragment(): ConfirmCheckInFragment
}
