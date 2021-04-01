package de.rki.coronawarnapp.ui.eventregistration

import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.ui.eventregistration.attendee.checkins.CheckInsFragment
import de.rki.coronawarnapp.ui.eventregistration.attendee.checkins.CheckInsModule
import de.rki.coronawarnapp.ui.eventregistration.attendee.confirm.ConfirmCheckInFragment
import de.rki.coronawarnapp.ui.eventregistration.attendee.confirm.ConfirmCheckInModule
import de.rki.coronawarnapp.ui.eventregistration.attendee.edit.EditCheckInFragment
import de.rki.coronawarnapp.ui.eventregistration.attendee.edit.EditCheckInModule
import de.rki.coronawarnapp.ui.eventregistration.attendee.scan.ScanCheckInQrCodeFragment
import de.rki.coronawarnapp.ui.eventregistration.attendee.scan.ScanCheckInQrCodeModule
import de.rki.coronawarnapp.ui.eventregistration.organizer.category.TraceLocationCategoryFragment
import de.rki.coronawarnapp.ui.eventregistration.organizer.category.TraceLocationCategoryFragmentModule
import de.rki.coronawarnapp.ui.eventregistration.organizer.create.TraceLocationCreateFragment
import de.rki.coronawarnapp.ui.eventregistration.organizer.create.TraceLocationCreateFragmentModule
import de.rki.coronawarnapp.ui.eventregistration.organizer.list.TraceLocationsFragment
import de.rki.coronawarnapp.ui.eventregistration.organizer.list.TraceLocationsFragmentModule
import de.rki.coronawarnapp.ui.eventregistration.organizer.qrinfo.TraceLocationQRInfoFragment
import de.rki.coronawarnapp.ui.eventregistration.organizer.qrinfo.TraceLocationQRInfoFragmentModule
import de.rki.coronawarnapp.ui.eventregistration.organizer.poster.QrCodePosterFragment
import de.rki.coronawarnapp.ui.eventregistration.organizer.poster.QrCodePosterFragmentModule

@Module
internal abstract class EventRegistrationUIModule {

    @ContributesAndroidInjector(modules = [ScanCheckInQrCodeModule::class])
    abstract fun scanCheckInQrCodeFragment(): ScanCheckInQrCodeFragment

    @ContributesAndroidInjector(modules = [ConfirmCheckInModule::class])
    abstract fun confirmCheckInFragment(): ConfirmCheckInFragment

    @ContributesAndroidInjector(modules = [EditCheckInModule::class])
    abstract fun editCheckInFragment(): EditCheckInFragment

    @ContributesAndroidInjector(modules = [CheckInsModule::class])
    abstract fun checkInsFragment(): CheckInsFragment

    @ContributesAndroidInjector(modules = [TraceLocationCategoryFragmentModule::class])
    abstract fun traceLocationCategoryFragment(): TraceLocationCategoryFragment

    @ContributesAndroidInjector(modules = [TraceLocationCreateFragmentModule::class])
    abstract fun traceLocationCreateFragment(): TraceLocationCreateFragment

    @ContributesAndroidInjector(modules = [TraceLocationQRInfoFragmentModule::class])
    abstract fun traceLocationQRInfoFragment(): TraceLocationQRInfoFragment

    @ContributesAndroidInjector(modules = [TraceLocationsFragmentModule::class])
    abstract fun traceLocationsFragment(): TraceLocationsFragment

    @ContributesAndroidInjector(modules = [QrCodePosterFragmentModule::class])
    abstract fun qrCodePosterFragment(): QrCodePosterFragment
}
