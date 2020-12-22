package de.rki.coronawarnapp.contactdiary.ui

import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.contactdiary.ui.day.ContactDiaryDayFragment
import de.rki.coronawarnapp.contactdiary.ui.day.ContactDiaryDayModule
import de.rki.coronawarnapp.contactdiary.ui.day.tabs.location.ContactDiaryLocationListFragment
import de.rki.coronawarnapp.contactdiary.ui.day.tabs.location.ContactDiaryLocationListModule
import de.rki.coronawarnapp.contactdiary.ui.day.tabs.person.ContactDiaryPersonListFragment
import de.rki.coronawarnapp.contactdiary.ui.day.tabs.person.ContactDiaryPersonListModule
import de.rki.coronawarnapp.contactdiary.ui.edit.ContactDiaryEditModule
import de.rki.coronawarnapp.contactdiary.ui.onboarding.ContactDiaryOnboardingFragment
import de.rki.coronawarnapp.contactdiary.ui.onboarding.ContactDiaryOnboardingFragmentModule
import de.rki.coronawarnapp.contactdiary.ui.overview.ContactDiaryOverviewFragment
import de.rki.coronawarnapp.contactdiary.ui.overview.ContactDiaryOverviewFragmentModule
import de.rki.coronawarnapp.contactdiary.ui.sheets.location.ContactDiaryLocationBottomSheetDialogFragment
import de.rki.coronawarnapp.contactdiary.ui.sheets.location.ContactDiaryLocationBottomSheetDialogModule
import de.rki.coronawarnapp.contactdiary.ui.sheets.person.ContactDiaryPersonBottomSheetDialogFragment
import de.rki.coronawarnapp.contactdiary.ui.sheets.person.ContactDiaryPersonBottomSheetDialogModule

@Module(includes = [ContactDiaryEditModule::class])
abstract class ContactDiaryUIModule {
    @ContributesAndroidInjector(modules = [ContactDiaryDayModule::class])
    abstract fun contactDiaryDayFragment(): ContactDiaryDayFragment

    @ContributesAndroidInjector(modules = [ContactDiaryPersonListModule::class])
    abstract fun contactDiaryPersonListFragment(): ContactDiaryPersonListFragment

    @ContributesAndroidInjector(modules = [ContactDiaryLocationListModule::class])
    abstract fun contactDiaryLocationListFragment(): ContactDiaryLocationListFragment

    @ContributesAndroidInjector(modules = [ContactDiaryPersonBottomSheetDialogModule::class])
    abstract fun contactDiaryPersonBottomSheetDialogFragment(): ContactDiaryPersonBottomSheetDialogFragment

    @ContributesAndroidInjector(modules = [ContactDiaryLocationBottomSheetDialogModule::class])
    abstract fun contactDiaryLocationBottomSheetDialogFragment(): ContactDiaryLocationBottomSheetDialogFragment

    @ContributesAndroidInjector(modules = [ContactDiaryOnboardingFragmentModule::class])
    abstract fun contactDiaryOnboardingFragment(): ContactDiaryOnboardingFragment

    @ContributesAndroidInjector(modules = [ContactDiaryOverviewFragmentModule::class])
    abstract fun contactDiaryOverviewFragment(): ContactDiaryOverviewFragment
}
