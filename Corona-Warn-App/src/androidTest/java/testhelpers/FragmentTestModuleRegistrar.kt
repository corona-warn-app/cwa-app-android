package testhelpers

import dagger.Module
import de.rki.coronawarnapp.ui.contactdiary.ContactDiaryDayFragmentTestModule
import de.rki.coronawarnapp.ui.contactdiary.ContactDiaryEditLocationsFragmentTestModule
import de.rki.coronawarnapp.ui.contactdiary.ContactDiaryEditPersonsFragmentTestModule
import de.rki.coronawarnapp.ui.contactdiary.ContactDiaryLocationListFragmentTestModule
import de.rki.coronawarnapp.ui.contactdiary.ContactDiaryOnboardingFragmentTestModule
import de.rki.coronawarnapp.ui.contactdiary.ContactDiaryOverviewFragmentTestModule
import de.rki.coronawarnapp.ui.contactdiary.ContactDiaryPersonListFragmentTestModule
import de.rki.coronawarnapp.ui.main.home.HomeFragmentTestModule
import de.rki.coronawarnapp.ui.onboarding.OnboardingDeltaInteroperabilityFragmentTestModule
import de.rki.coronawarnapp.ui.onboarding.OnboardingFragmentTestModule
import de.rki.coronawarnapp.ui.onboarding.OnboardingNotificationsTestModule
import de.rki.coronawarnapp.ui.onboarding.OnboardingPrivacyTestModule
import de.rki.coronawarnapp.ui.onboarding.OnboardingTestFragmentModule
import de.rki.coronawarnapp.ui.onboarding.OnboardingTracingFragmentTestModule
import de.rki.coronawarnapp.ui.submission.SubmissionConsentFragmentTestModule
import de.rki.coronawarnapp.ui.submission.SubmissionContactTestModule
import de.rki.coronawarnapp.ui.submission.SubmissionDispatcherTestModule
import de.rki.coronawarnapp.ui.submission.SubmissionQRScanFragmentModule
import de.rki.coronawarnapp.ui.submission.SubmissionSymptomCalendarFragmentTestModule
import de.rki.coronawarnapp.ui.submission.SubmissionSymptomIntroFragmentTestModule
import de.rki.coronawarnapp.ui.submission.SubmissionTanTestModule
import de.rki.coronawarnapp.ui.submission.SubmissionTestResultConsentGivenTestModule
import de.rki.coronawarnapp.ui.submission.SubmissionTestResultNoConsentModel
import de.rki.coronawarnapp.ui.submission.SubmissionTestResultTestAvailableModule
import de.rki.coronawarnapp.ui.submission.SubmissionTestResultTestModule
import de.rki.coronawarnapp.ui.submission.SubmissionTestResultTestNegativeModule
import de.rki.coronawarnapp.ui.submission.SubmissionYourConsentFragmentTestModule
import de.rki.coronawarnapp.ui.tracing.TracingDetailsFragmentTestTestModule

@Module(
    includes = [
        HomeFragmentTestModule::class,
        // Onboarding
        OnboardingFragmentTestModule::class,
        OnboardingDeltaInteroperabilityFragmentTestModule::class,
        OnboardingNotificationsTestModule::class,
        OnboardingPrivacyTestModule::class,
        OnboardingTestFragmentModule::class,
        OnboardingTracingFragmentTestModule::class,
        // Submission
        SubmissionDispatcherTestModule::class,
        SubmissionTanTestModule::class,
        SubmissionTestResultTestModule::class,
        SubmissionTestResultTestNegativeModule::class,
        SubmissionTestResultTestAvailableModule::class,
        SubmissionTestResultNoConsentModel::class,
        SubmissionTestResultConsentGivenTestModule::class,
        SubmissionSymptomIntroFragmentTestModule::class,
        SubmissionContactTestModule::class,
        SubmissionQRScanFragmentModule::class,
        SubmissionConsentFragmentTestModule::class,
        SubmissionYourConsentFragmentTestModule::class,
        SubmissionSymptomCalendarFragmentTestModule::class,
        SubmissionQRScanFragmentModule::class,
        // Tracing
        TracingDetailsFragmentTestTestModule::class,
        // Contact Diary
        ContactDiaryOnboardingFragmentTestModule::class,
        ContactDiaryOverviewFragmentTestModule::class,
        ContactDiaryDayFragmentTestModule::class,
        ContactDiaryPersonListFragmentTestModule::class,
        ContactDiaryLocationListFragmentTestModule::class,
        ContactDiaryEditLocationsFragmentTestModule::class,
        ContactDiaryEditPersonsFragmentTestModule::class
    ]
)
class FragmentTestModuleRegistrar
