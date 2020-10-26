package testhelpers

import dagger.Module
import de.rki.coronawarnapp.ui.main.home.HomeFragmentTestModule
import de.rki.coronawarnapp.ui.onboarding.OnboardingDeltaInteroperabilityFragmentTestModule
import de.rki.coronawarnapp.ui.onboarding.OnboardingFragmentTestModule
import de.rki.coronawarnapp.ui.onboarding.OnboardingNotificationsTestModule
import de.rki.coronawarnapp.ui.onboarding.OnboardingPrivacyTestModule
import de.rki.coronawarnapp.ui.onboarding.OnboardingTestFragmentModule
import de.rki.coronawarnapp.ui.onboarding.OnboardingTracingFragmentTestModule

@Module(
    includes = [
        HomeFragmentTestModule::class,
        OnboardingFragmentTestModule::class,
        OnboardingDeltaInteroperabilityFragmentTestModule::class,
        OnboardingNotificationsTestModule::class,
        OnboardingPrivacyTestModule::class,
        OnboardingTestFragmentModule::class,
        OnboardingTracingFragmentTestModule::class
    ]
)
class FragmentTestModuleRegistrar
