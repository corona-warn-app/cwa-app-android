package testhelpers

import dagger.Module
import de.rki.coronawarnapp.ui.main.home.HomeFragmentTestModule
import de.rki.coronawarnapp.ui.onboarding.OnboardingDeltaInteroperabilityFragmentTestModule
import de.rki.coronawarnapp.ui.onboarding.OnboardingFragmentTestModule
import de.rki.coronawarnapp.ui.onboarding.OnboardingNotificationsTestModule
import de.rki.coronawarnapp.ui.onboarding.OnboardingPrivacyTestModule
import de.rki.coronawarnapp.ui.onboarding.OnboardingTestFragmentModule
import de.rki.coronawarnapp.ui.onboarding.OnboardingTracingFragmentTestModule
import de.rki.coronawarnapp.ui.submission.SubmissionContactTestModule
import de.rki.coronawarnapp.ui.submission.SubmissionDispatcherTestModule
import de.rki.coronawarnapp.ui.submission.SubmissionDoneTestModule
import de.rki.coronawarnapp.ui.submission.SubmissionIntroTestModule
import de.rki.coronawarnapp.ui.submission.SubmissionOtherWarningTestModule
import de.rki.coronawarnapp.ui.submission.SubmissionQRInfoFragmentModule
import de.rki.coronawarnapp.ui.submission.SubmissionQRScanFragmentModule
import de.rki.coronawarnapp.ui.submission.SubmissionSymptomCalendarFragmentTestModule
import de.rki.coronawarnapp.ui.submission.SubmissionSymptomIntroFragmentTestModule
import de.rki.coronawarnapp.ui.submission.SubmissionTanTestModule
import de.rki.coronawarnapp.ui.submission.SubmissionTestResultTestModule

@Module(
    includes = [
        HomeFragmentTestModule::class,

        //Onboarding
        OnboardingFragmentTestModule::class,
        OnboardingDeltaInteroperabilityFragmentTestModule::class,
        OnboardingNotificationsTestModule::class,
        OnboardingPrivacyTestModule::class,
        OnboardingTestFragmentModule::class,
        OnboardingTracingFragmentTestModule::class,

        //Submission
        SubmissionIntroTestModule::class,
        SubmissionDispatcherTestModule::class,
        SubmissionTanTestModule::class,
        SubmissionTestResultTestModule::class,
        SubmissionOtherWarningTestModule::class,
        SubmissionSymptomIntroFragmentTestModule::class,
        SubmissionSymptomCalendarFragmentTestModule::class,
        SubmissionContactTestModule::class,
        SubmissionDoneTestModule::class,
        SubmissionQRInfoFragmentModule::class,
        SubmissionQRScanFragmentModule::class,
    ]
)
class FragmentTestModuleRegistrar
