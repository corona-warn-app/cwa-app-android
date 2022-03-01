package de.rki.coronawarnapp.ui.main

import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.miscinfo.MiscInfoFragment
import de.rki.coronawarnapp.miscinfo.MiscInfoFragmentModule
import de.rki.coronawarnapp.test.appconfig.ui.AppConfigTestFragment
import de.rki.coronawarnapp.test.appconfig.ui.AppConfigTestFragmentModule
import de.rki.coronawarnapp.test.ccl.CclTestFragment
import de.rki.coronawarnapp.test.ccl.CclTestModule
import de.rki.coronawarnapp.test.contactdiary.ui.ContactDiaryTestFragment
import de.rki.coronawarnapp.test.contactdiary.ui.ContactDiaryTestFragmentModule
import de.rki.coronawarnapp.test.datadonation.ui.DataDonationTestFragment
import de.rki.coronawarnapp.test.datadonation.ui.DataDonationTestFragmentModule
import de.rki.coronawarnapp.test.dccticketing.DccTicketingTestFragment
import de.rki.coronawarnapp.test.dccticketing.DccTicketingTestModule
import de.rki.coronawarnapp.test.debugoptions.ui.DebugOptionsFragment
import de.rki.coronawarnapp.test.debugoptions.ui.DebugOptionsFragmentModule
import de.rki.coronawarnapp.test.deltaonboarding.ui.DeltaOnboardingFragment
import de.rki.coronawarnapp.test.deltaonboarding.ui.DeltaOnboardingFragmentModule
import de.rki.coronawarnapp.test.dsc.ui.DscTestFragment
import de.rki.coronawarnapp.test.dsc.ui.DscTestModule
import de.rki.coronawarnapp.test.hometestcards.ui.HomeTestCardsFragment
import de.rki.coronawarnapp.test.hometestcards.ui.HomeTestCardsFragmentModule
import de.rki.coronawarnapp.test.keydownload.ui.KeyDownloadTestFragment
import de.rki.coronawarnapp.test.keydownload.ui.KeyDownloadTestFragmentModule
import de.rki.coronawarnapp.test.menu.ui.TestMenuFragment
import de.rki.coronawarnapp.test.menu.ui.TestMenuFragmentModule
import de.rki.coronawarnapp.test.playground.ui.PlaygroundFragment
import de.rki.coronawarnapp.test.playground.ui.PlaygroundModule
import de.rki.coronawarnapp.test.presencetracing.ui.PresenceTracingTestFragment
import de.rki.coronawarnapp.test.presencetracing.ui.PresenceTracingTestFragmentModule
import de.rki.coronawarnapp.test.presencetracing.ui.poster.QrCodePosterTestFragment
import de.rki.coronawarnapp.test.presencetracing.ui.poster.QrCodePosterTestFragmentModule
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

    @ContributesAndroidInjector(modules = [MiscInfoFragmentModule::class])
    abstract fun miscInfoFragment(): MiscInfoFragment

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

    @ContributesAndroidInjector(modules = [ContactDiaryTestFragmentModule::class])
    abstract fun contactDiaryTest(): ContactDiaryTestFragment

    @ContributesAndroidInjector(modules = [PlaygroundModule::class])
    abstract fun playground(): PlaygroundFragment

    @ContributesAndroidInjector(modules = [DataDonationTestFragmentModule::class])
    abstract fun dataDonation(): DataDonationTestFragment

    @ContributesAndroidInjector(modules = [DeltaOnboardingFragmentModule::class])
    abstract fun deltaOnboarding(): DeltaOnboardingFragment

    @ContributesAndroidInjector(modules = [PresenceTracingTestFragmentModule::class])
    abstract fun presenceTracingTestFragment(): PresenceTracingTestFragment

    @ContributesAndroidInjector(modules = [QrCodePosterTestFragmentModule::class])
    abstract fun qrCodePosterTestFragment(): QrCodePosterTestFragment

    @ContributesAndroidInjector(modules = [HomeTestCardsFragmentModule::class])
    abstract fun homeTestCards(): HomeTestCardsFragment

    @ContributesAndroidInjector(modules = [DscTestModule::class])
    abstract fun dscTest(): DscTestFragment

    @ContributesAndroidInjector(modules = [DccTicketingTestModule::class])
    abstract fun dccTicketingTestFragment(): DccTicketingTestFragment

    @ContributesAndroidInjector(modules = [CclTestModule::class])
    abstract fun cclTestFragment(): CclTestFragment
}
