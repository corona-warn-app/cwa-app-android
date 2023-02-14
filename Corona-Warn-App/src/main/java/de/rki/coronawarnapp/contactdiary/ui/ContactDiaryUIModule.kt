package de.rki.coronawarnapp.contactdiary.ui

import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.contactdiary.ui.day.ContactDiaryDayFragment
import de.rki.coronawarnapp.contactdiary.ui.day.ContactDiaryDayModule
import de.rki.coronawarnapp.contactdiary.ui.day.tabs.location.ContactDiaryLocationListFragment
import de.rki.coronawarnapp.contactdiary.ui.day.tabs.location.ContactDiaryLocationListModule
import de.rki.coronawarnapp.contactdiary.ui.day.tabs.person.ContactDiaryPersonListFragment
import de.rki.coronawarnapp.contactdiary.ui.day.tabs.person.ContactDiaryPersonListModule
import de.rki.coronawarnapp.contactdiary.ui.onboarding.ContactDiaryOnboardingFragment
import de.rki.coronawarnapp.contactdiary.ui.overview.ContactDiaryOverviewFragment
import de.rki.coronawarnapp.contactdiary.ui.location.ContactDiaryAddLocationFragment
import de.rki.coronawarnapp.contactdiary.ui.location.ContactDiaryAddLocationFragmentModule
import de.rki.coronawarnapp.contactdiary.ui.person.ContactDiaryAddPersonFragment
import de.rki.coronawarnapp.contactdiary.ui.person.ContactDiaryAddPersonModule

@Module(includes = [ContactDiaryEditModule::class])
abstract class ContactDiaryUIModule {
    @ContributesAndroidInjector(modules = [ContactDiaryDayModule::class])
    abstract fun contactDiaryDayFragment(): ContactDiaryDayFragment

    @ContributesAndroidInjector(modules = [ContactDiaryPersonListModule::class])
    abstract fun contactDiaryPersonListFragment(): ContactDiaryPersonListFragment

    @ContributesAndroidInjector(modules = [ContactDiaryLocationListModule::class])
    abstract fun contactDiaryLocationListFragment(): ContactDiaryLocationListFragment

    @ContributesAndroidInjector(modules = [ContactDiaryAddPersonModule::class])
    abstract fun contactDiaryAddPersonFragment(): ContactDiaryAddPersonFragment

    @ContributesAndroidInjector(modules = [ContactDiaryAddLocationFragmentModule::class])
    abstract fun contactDiaryAddLocationFragment(): ContactDiaryAddLocationFragment

    @ContributesAndroidInjector(modules = [ContactDiaryOnboardingFragmentModule::class])
    abstract fun contactDiaryOnboardingFragment(): ContactDiaryOnboardingFragment

    @ContributesAndroidInjector(modules = [ContactDiaryOverviewFragmentModule::class])
    abstract fun contactDiaryOverviewFragment(): ContactDiaryOverviewFragment
}
