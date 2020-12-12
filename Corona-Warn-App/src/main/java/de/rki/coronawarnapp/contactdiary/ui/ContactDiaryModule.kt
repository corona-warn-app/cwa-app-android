package de.rki.coronawarnapp.contactdiary.ui

import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.contactdiary.ui.onboarding.ContactDiaryOnboardingFragment
import de.rki.coronawarnapp.contactdiary.ui.onboarding.ContactDiaryOnboardingFragmentModul
import de.rki.coronawarnapp.contactdiary.ui.overview.ContactDiaryOverviewFragment
import de.rki.coronawarnapp.contactdiary.ui.overview.ContactDiaryOverviewFragmentModule

@Module
abstract class ContactDiaryModule {
    @ContributesAndroidInjector(modules = [ContactDiaryOnboardingFragmentModul::class])
    abstract fun contactDiaryOnboardingFragment(): ContactDiaryOnboardingFragment

    @ContributesAndroidInjector(modules = [ContactDiaryOverviewFragmentModule::class])
    abstract fun contactDiaryOverviewFragment(): ContactDiaryOverviewFragment
}
