package de.rki.coronawarnapp.ui.presencetracing

import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.ui.presencetracing.attendee.checkins.CheckInsFragment
import de.rki.coronawarnapp.ui.presencetracing.attendee.checkins.CheckInsModule
import de.rki.coronawarnapp.ui.presencetracing.attendee.checkins.consent.CheckInsConsentFragment
import de.rki.coronawarnapp.ui.presencetracing.attendee.checkins.consent.CheckInsConsentFragmentModule
import de.rki.coronawarnapp.ui.presencetracing.attendee.confirm.ConfirmCheckInFragment
import de.rki.coronawarnapp.ui.presencetracing.attendee.confirm.ConfirmCheckInModule
import de.rki.coronawarnapp.ui.presencetracing.attendee.edit.EditCheckInFragment
import de.rki.coronawarnapp.ui.presencetracing.attendee.edit.EditCheckInModule
import de.rki.coronawarnapp.ui.presencetracing.attendee.onboarding.CheckInOnboardingFragment
import de.rki.coronawarnapp.ui.presencetracing.attendee.onboarding.CheckInOnboardingModule
import de.rki.coronawarnapp.ui.presencetracing.attendee.scan.ScanCheckInQrCodeFragment
import de.rki.coronawarnapp.ui.presencetracing.attendee.scan.ScanCheckInQrCodeModule
import de.rki.coronawarnapp.ui.presencetracing.organizer.category.TraceLocationCategoryFragment
import de.rki.coronawarnapp.ui.presencetracing.organizer.category.TraceLocationCategoryFragmentModule
import de.rki.coronawarnapp.ui.presencetracing.organizer.create.TraceLocationCreateFragment
import de.rki.coronawarnapp.ui.presencetracing.organizer.create.TraceLocationCreateFragmentModule
import de.rki.coronawarnapp.ui.presencetracing.organizer.details.QrCodeDetailFragment
import de.rki.coronawarnapp.ui.presencetracing.organizer.details.QrCodeDetailFragmentModule
import de.rki.coronawarnapp.ui.presencetracing.organizer.list.TraceLocationsFragment
import de.rki.coronawarnapp.ui.presencetracing.organizer.list.TraceLocationsFragmentModule
import de.rki.coronawarnapp.ui.presencetracing.organizer.poster.QrCodePosterFragment
import de.rki.coronawarnapp.ui.presencetracing.organizer.poster.QrCodePosterFragmentModule
import de.rki.coronawarnapp.ui.presencetracing.organizer.qrinfo.TraceLocationQRInfoFragment
import de.rki.coronawarnapp.ui.presencetracing.organizer.qrinfo.TraceLocationQRInfoFragmentModule

@Module
internal abstract class PresenceTracingUIModule {

    @ContributesAndroidInjector(modules = [ScanCheckInQrCodeModule::class])
    abstract fun scanCheckInQrCodeFragment(): ScanCheckInQrCodeFragment

    @ContributesAndroidInjector(modules = [ConfirmCheckInModule::class])
    abstract fun confirmCheckInFragment(): ConfirmCheckInFragment

    @ContributesAndroidInjector(modules = [EditCheckInModule::class])
    abstract fun editCheckInFragment(): EditCheckInFragment

    @ContributesAndroidInjector(modules = [CheckInsModule::class])
    abstract fun checkInsFragment(): CheckInsFragment

    @ContributesAndroidInjector(modules = [CheckInOnboardingModule::class])
    abstract fun checkInOnboardingFragment(): CheckInOnboardingFragment

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

    @ContributesAndroidInjector(modules = [QrCodeDetailFragmentModule::class])
    abstract fun qrCodeDetailFragment(): QrCodeDetailFragment

    @ContributesAndroidInjector(modules = [CheckInsConsentFragmentModule::class])
    abstract fun checkInsConsentFragment(): CheckInsConsentFragment
}
