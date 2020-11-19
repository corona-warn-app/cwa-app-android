package de.rki.coronawarnapp.ui.main

import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.test.api.ui.TestForAPIFragment
import de.rki.coronawarnapp.test.api.ui.TestForApiFragmentModule
import de.rki.coronawarnapp.test.appconfig.ui.AppConfigTestFragment
import de.rki.coronawarnapp.test.appconfig.ui.AppConfigTestFragmentModule
import de.rki.coronawarnapp.test.debugoptions.ui.DebugOptionsFragment
import de.rki.coronawarnapp.test.debugoptions.ui.DebugOptionsFragmentModule
import de.rki.coronawarnapp.test.keydownload.ui.KeyDownloadTestFragment
import de.rki.coronawarnapp.test.keydownload.ui.KeyDownloadTestFragmentModule
import de.rki.coronawarnapp.test.menu.ui.TestMenuFragment
import de.rki.coronawarnapp.test.menu.ui.TestMenuFragmentModule
import de.rki.coronawarnapp.test.risklevel.ui.TestRiskLevelCalculationFragment
import de.rki.coronawarnapp.test.risklevel.ui.TestRiskLevelCalculationFragmentModule
import de.rki.coronawarnapp.test.submission.ui.SubmissionTestFragment
import de.rki.coronawarnapp.test.submission.ui.SubmissionTestFragmentModule
import de.rki.coronawarnapp.test.tasks.ui.TestTaskControllerFragment
import de.rki.coronawarnapp.test.tasks.ui.TestTaskControllerFragmentModule

@Module
abstract class MainActivityTestModule {

    @ContributesAndroidInjector(modules = [TestMenuFragmentModule::class])
    abstract fun testMenuFragment(): TestMenuFragment

    @ContributesAndroidInjector(modules = [TestRiskLevelCalculationFragmentModule::class])
    abstract fun testRiskLevelCalculationFragment(): TestRiskLevelCalculationFragment

    @ContributesAndroidInjector(modules = [TestForApiFragmentModule::class])
    abstract fun testRiskLevelApiFragment(): TestForAPIFragment

    @ContributesAndroidInjector(modules = [TestTaskControllerFragmentModule::class])
    abstract fun testTaskControllerFragment(): TestTaskControllerFragment

    @ContributesAndroidInjector(modules = [AppConfigTestFragmentModule::class])
    abstract fun appConfigTestFragment(): AppConfigTestFragment

    @ContributesAndroidInjector(modules = [DebugOptionsFragmentModule::class])
    abstract fun debugOptions(): DebugOptionsFragment

    @ContributesAndroidInjector(modules = [KeyDownloadTestFragmentModule::class])
    abstract fun keyDownload(): KeyDownloadTestFragment

    @ContributesAndroidInjector(modules = [SubmissionTestFragmentModule::class])
    abstract fun submissionTest(): SubmissionTestFragment
}
