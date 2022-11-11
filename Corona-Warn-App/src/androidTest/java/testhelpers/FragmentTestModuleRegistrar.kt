package testhelpers

import dagger.Module
import dagger.Provides
import de.rki.coronawarnapp.bugreporting.DebugLogTestModule
import de.rki.coronawarnapp.bugreporting.DebugLogUploadTestModule
import de.rki.coronawarnapp.covidcertificate.boosterinfodetails.BoosterInfoDetailsFragmentTestModule
import de.rki.coronawarnapp.covidcertificate.person.ui.admission.AdmissionScenariosFragmentTestModule
import de.rki.coronawarnapp.covidcertificate.person.ui.details.PersonDetailsFragmentTestModule
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.PersonOverviewFragmentTestModule
import de.rki.coronawarnapp.covidcertificate.recovery.ui.RecoveryCertificateDetailsFragmentTestModule
import de.rki.coronawarnapp.covidcertificate.test.ui.CovidCertificateDetailsFragmentTestModule
import de.rki.coronawarnapp.covidcertificate.vaccination.ui.details.VaccinationDetailsFragmentTestModule
import de.rki.coronawarnapp.dccreissuance.ui.consent.DccReissuanceConsentFragmentTestModule
import de.rki.coronawarnapp.dccreissuance.ui.consent.acccerts.DccReissuanceAccCertsFragmentTestModule
import de.rki.coronawarnapp.dccticketing.ui.certificateselection.DccTicketingCertificateSelectionFragmentModule
import de.rki.coronawarnapp.dccticketing.ui.consent.one.DccTicketingConsentOneFragmentTestModule
import de.rki.coronawarnapp.dccticketing.ui.consent.two.DccTicketingConsentTwoFragmentModule
import de.rki.coronawarnapp.dccticketing.ui.validationresult.success.DccTicketingValidationResultFragmentTestModule
import de.rki.coronawarnapp.familytest.ui.consent.FamilyTestConsentFragmentTestModule
import de.rki.coronawarnapp.familytest.ui.selection.TestRegistrationSelectionFragmentTestModule
import de.rki.coronawarnapp.familytest.ui.testlist.FamilyTestsListFragmentTestModule
import de.rki.coronawarnapp.profile.ui.create.ProfileCreateFragmentTestModule
import de.rki.coronawarnapp.profile.ui.list.ProfileListFragmentTestModule
import de.rki.coronawarnapp.profile.ui.onboarding.ProfileOnboardingFragmentTestModule
import de.rki.coronawarnapp.qrcode.ui.QrCodeScannerFragmentTestModule
import de.rki.coronawarnapp.reyclebin.ui.RecyclerBinOverviewFragmentTestModule
import de.rki.coronawarnapp.srs.ui.consent.SrsSubmissionConsentFragmentTestModule
import de.rki.coronawarnapp.srs.ui.typeselection.SrsTypeSelectionFragmentTestModule
import de.rki.coronawarnapp.ui.contactdiary.ContactDiaryDayFragmentTestModule
import de.rki.coronawarnapp.ui.contactdiary.ContactDiaryEditLocationsFragmentTestModule
import de.rki.coronawarnapp.ui.contactdiary.ContactDiaryEditPersonsFragmentTestModule
import de.rki.coronawarnapp.ui.contactdiary.ContactDiaryLocationListFragmentTestModule
import de.rki.coronawarnapp.ui.contactdiary.ContactDiaryOnboardingFragmentTestModule
import de.rki.coronawarnapp.ui.contactdiary.ContactDiaryOverviewFragmentTestModule
import de.rki.coronawarnapp.ui.contactdiary.ContactDiaryPersonListFragmentTestModule
import de.rki.coronawarnapp.ui.coronatest.rat.profile.qrcode.ProfileQrCodeFragmentTestModule
import de.rki.coronawarnapp.ui.eventregistration.organizer.CreateEventTestModule
import de.rki.coronawarnapp.ui.eventregistration.organizer.QrCodeDetailFragmentTestModule
import de.rki.coronawarnapp.ui.eventregistration.organizer.TraceLocationsFragmentTestModule
import de.rki.coronawarnapp.ui.main.home.HomeFragmentTestModule
import de.rki.coronawarnapp.ui.onboarding.OnboardingAnalyticsFragmentTestModule
import de.rki.coronawarnapp.ui.onboarding.OnboardingDeltaInteroperabilityFragmentTestModule
import de.rki.coronawarnapp.ui.onboarding.OnboardingDeltaNotificationsFragmentTestModule
import de.rki.coronawarnapp.ui.onboarding.OnboardingFragmentTestModule
import de.rki.coronawarnapp.ui.onboarding.OnboardingNotificationsTestModule
import de.rki.coronawarnapp.ui.onboarding.OnboardingPrivacyTestModule
import de.rki.coronawarnapp.ui.onboarding.OnboardingTestFragmentModule
import de.rki.coronawarnapp.ui.onboarding.OnboardingTracingFragmentTestModule
import de.rki.coronawarnapp.ui.presencetracing.organizer.warn.duration.TraceLocationWarnDurationFragmentTestModule
import de.rki.coronawarnapp.ui.presencetracing.organizer.warn.list.TraceLocationSelectionFragmentTestModule
import de.rki.coronawarnapp.ui.presencetracing.organizer.warn.tan.TraceLocationWarnTanFragmentTestModule
import de.rki.coronawarnapp.ui.settings.notifications.NotificationSettingsFragmentTestModule
import de.rki.coronawarnapp.ui.statistics.StatisticsExplanationFragmentTestModule
import de.rki.coronawarnapp.ui.submission.SubmissionConsentFragmentTestModule
import de.rki.coronawarnapp.ui.submission.SubmissionContactTestModule
import de.rki.coronawarnapp.ui.submission.SubmissionDispatcherTestModule
import de.rki.coronawarnapp.ui.submission.SubmissionSymptomCalendarFragmentTestModule
import de.rki.coronawarnapp.ui.submission.SubmissionSymptomIntroFragmentTestModule
import de.rki.coronawarnapp.ui.submission.SubmissionTanTestModule
import de.rki.coronawarnapp.ui.submission.SubmissionTestResultConsentGivenTestModule
import de.rki.coronawarnapp.ui.submission.SubmissionTestResultNoConsentModel
import de.rki.coronawarnapp.ui.submission.SubmissionTestResultTestAvailableModule
import de.rki.coronawarnapp.ui.submission.SubmissionTestResultTestModule
import de.rki.coronawarnapp.ui.submission.SubmissionTestResultTestNegativeModule
import de.rki.coronawarnapp.ui.submission.SubmissionYourConsentFragmentTestModule
import de.rki.coronawarnapp.ui.submission.covidcertificate.RequestCovidCertificateFragmentTestModule
import de.rki.coronawarnapp.ui.submission.submissiondone.SubmissionDoneFragmentTestModule
import de.rki.coronawarnapp.ui.tracing.TracingDetailsFragmentTestTestModule
import de.rki.coronawarnapp.ui.vaccination.CovidCertificateInfoFragmentTestModule
import de.rki.coronawarnapp.util.coroutine.AppScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.TestScope

@Module(
    includes = [
        HomeFragmentTestModule::class,

        // -------- Onboarding --------
        OnboardingFragmentTestModule::class,
        OnboardingDeltaInteroperabilityFragmentTestModule::class,
        OnboardingNotificationsTestModule::class,
        OnboardingPrivacyTestModule::class,
        OnboardingTestFragmentModule::class,
        OnboardingTracingFragmentTestModule::class,
        OnboardingAnalyticsFragmentTestModule::class,
        OnboardingDeltaNotificationsFragmentTestModule::class,

        // -------- Submission --------
        SubmissionDispatcherTestModule::class,
        SubmissionTanTestModule::class,
        SubmissionTestResultTestModule::class,
        SubmissionTestResultTestNegativeModule::class,
        SubmissionTestResultTestAvailableModule::class,
        SubmissionTestResultNoConsentModel::class,
        SubmissionTestResultConsentGivenTestModule::class,
        SubmissionSymptomIntroFragmentTestModule::class,
        SubmissionContactTestModule::class,
        SubmissionConsentFragmentTestModule::class,
        SubmissionYourConsentFragmentTestModule::class,
        SubmissionSymptomCalendarFragmentTestModule::class,
        SrsSubmissionConsentFragmentTestModule::class,
        SrsTypeSelectionFragmentTestModule::class,
        SubmissionDoneFragmentTestModule::class,

        // -------- Tracing --------
        TracingDetailsFragmentTestTestModule::class,

        // -------- Contact Diary --------
        ContactDiaryOnboardingFragmentTestModule::class,
        ContactDiaryOverviewFragmentTestModule::class,
        ContactDiaryDayFragmentTestModule::class,
        ContactDiaryPersonListFragmentTestModule::class,
        ContactDiaryLocationListFragmentTestModule::class,
        ContactDiaryEditLocationsFragmentTestModule::class,
        ContactDiaryEditPersonsFragmentTestModule::class,

        // -------- Statistics --------
        StatisticsExplanationFragmentTestModule::class,

        // -------- Bugreporting --------
        DebugLogUploadTestModule::class,
        DebugLogTestModule::class,

        // -------- Presence tracing --------
        CreateEventTestModule::class,
        TraceLocationsFragmentTestModule::class,
        QrCodeDetailFragmentTestModule::class,
        TraceLocationSelectionFragmentTestModule::class,
        TraceLocationWarnDurationFragmentTestModule::class,
        TraceLocationWarnTanFragmentTestModule::class,

        // -------- Certificates --------
        VaccinationDetailsFragmentTestModule::class,
        RecoveryCertificateDetailsFragmentTestModule::class,
        CovidCertificateInfoFragmentTestModule::class,
        RequestCovidCertificateFragmentTestModule::class,
        CovidCertificateDetailsFragmentTestModule::class,
        PersonOverviewFragmentTestModule::class,
        PersonDetailsFragmentTestModule::class,
        BoosterInfoDetailsFragmentTestModule::class,
        DccReissuanceConsentFragmentTestModule::class,
        DccReissuanceAccCertsFragmentTestModule::class,

        // -------- Profile ------------
        ProfileCreateFragmentTestModule::class,
        ProfileOnboardingFragmentTestModule::class,
        ProfileQrCodeFragmentTestModule::class,
        ProfileListFragmentTestModule::class,

        QrCodeScannerFragmentTestModule::class,

        // --------- Settings ---------
        NotificationSettingsFragmentTestModule::class,

        // --------- Recycler Bin ---------
        RecyclerBinOverviewFragmentTestModule::class,

        // --------- Dcc Ticketing Validation ---------
        DccTicketingValidationResultFragmentTestModule::class,
        DccTicketingCertificateSelectionFragmentModule::class,
        DccTicketingConsentTwoFragmentModule::class,
        DccTicketingConsentOneFragmentTestModule::class,

        // --------- Admission Scenarios ---------
        AdmissionScenariosFragmentTestModule::class,

        // --------- Family Test Certificate ---------
        FamilyTestConsentFragmentTestModule::class,
        TestRegistrationSelectionFragmentTestModule::class,
        FamilyTestsListFragmentTestModule::class,
    ]
)
class FragmentTestModuleRegistrar {
    @Provides
    @AppScope
    fun appScope(): CoroutineScope = TestScope()
}
