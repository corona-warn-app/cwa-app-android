package de.rki.coronawarnapp.contactdiary.ui

import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.contactdiary.ui.onboarding.ContactDiaryOnboardingFragment;
import de.rki.coronawarnapp.contactdiary.ui.onboarding.ContactDiaryOnboardingFragmentModel

@Module
abstract class ContactDiaryModule {
    @ContributesAndroidInjector(modules = [ContactDiaryOnboardingFragmentModel::class])
    abstract fun contactDiaryOnboardingFragment(): ContactDiaryOnboardingFragment


}
