package de.rki.coronawarnapp.ui

import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.contactdiary.retention.ContactDiaryRetentionModule
import de.rki.coronawarnapp.contactdiary.storage.ContactDiaryStorageModule
import de.rki.coronawarnapp.contactdiary.ui.ContactDiaryUIModule
import de.rki.coronawarnapp.ui.launcher.LauncherActivity
import de.rki.coronawarnapp.ui.launcher.LauncherActivityModule
import de.rki.coronawarnapp.ui.main.MainActivity
import de.rki.coronawarnapp.ui.main.MainActivityModule
import de.rki.coronawarnapp.ui.main.MainActivityTestModule
import de.rki.coronawarnapp.ui.onboarding.OnboardingActivity
import de.rki.coronawarnapp.ui.onboarding.OnboardingActivityModule

@Module(
    includes = [
        ContactDiaryStorageModule::class,
        ContactDiaryRetentionModule::class
    ]
)
abstract class ActivityBinder {
    @ContributesAndroidInjector(
        modules = [
            MainActivityModule::class,
            MainActivityTestModule::class,
            ContactDiaryUIModule::class
        ]
    )
    abstract fun mainActivity(): MainActivity

    @ContributesAndroidInjector(modules = [LauncherActivityModule::class])
    abstract fun launcherActivity(): LauncherActivity

    @ContributesAndroidInjector(modules = [OnboardingActivityModule::class])
    abstract fun onboardingActivity(): OnboardingActivity
}
