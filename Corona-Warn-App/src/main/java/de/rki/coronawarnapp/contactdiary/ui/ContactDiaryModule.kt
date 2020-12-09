package de.rki.coronawarnapp.contactdiary.ui

import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.contactdiary.ui.day.ContactDiaryDayFragment
import de.rki.coronawarnapp.contactdiary.ui.day.ContactDiaryDayModule
import de.rki.coronawarnapp.contactdiary.ui.day.tabs.location.ContactDiaryLocationListFragment
import de.rki.coronawarnapp.contactdiary.ui.day.tabs.location.ContactDiaryLocationListModule
import de.rki.coronawarnapp.contactdiary.ui.day.tabs.person.ContactDiaryPersonListFragment
import de.rki.coronawarnapp.contactdiary.ui.day.tabs.person.ContactDiaryPersonListModule

@Module
abstract class ContactDiaryModule {
    @ContributesAndroidInjector(modules = [ContactDiaryDayModule::class])
    abstract fun contactDiaryDayFragment(): ContactDiaryDayFragment

    @ContributesAndroidInjector(modules = [ContactDiaryPersonListModule::class])
    abstract fun contactDiaryPersonListFragment(): ContactDiaryPersonListFragment

    @ContributesAndroidInjector(modules = [ContactDiaryLocationListModule::class])
    abstract fun contactDiaryLocationListFragment(): ContactDiaryLocationListFragment
}
