package de.rki.coronawarnapp.contactdiary.ui

import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.contactdiary.ui.onboarding.ContactDiaryOnboardingFragment
import de.rki.coronawarnapp.contactdiary.ui.onboarding.ContactDiaryOnboardingFragmentModule

@Module
abstract class ContactDiaryModule {
    @ContributesAndroidInjector(modules = [ContactDiaryOnboardingFragmentModule::class])
    abstract fun contactDiaryOnboardingFragment(): ContactDiaryOnboardingFragment
}
