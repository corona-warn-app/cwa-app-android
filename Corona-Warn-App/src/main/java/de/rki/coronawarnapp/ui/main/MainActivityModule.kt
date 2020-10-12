package de.rki.coronawarnapp.ui.main

import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.ui.interoperability.InteroperabilityConfigurationFragment
import de.rki.coronawarnapp.ui.interoperability.InteroperabilityConfigurationFragmentModule
import de.rki.coronawarnapp.ui.main.home.HomeFragmentModule
import de.rki.coronawarnapp.ui.onboarding.OnboardingDeltaInteroperabilityModule
import de.rki.coronawarnapp.ui.riskdetails.RiskDetailsFragmentModule
import de.rki.coronawarnapp.ui.submission.fragment.SubmissionContactFragment
import de.rki.coronawarnapp.ui.submission.fragment.SubmissionTanFragment
import de.rki.coronawarnapp.ui.submission.fragment.SubmissionDispatcherFragment
import de.rki.coronawarnapp.ui.submission.fragment.SubmissionTestResultFragment
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionContactModule
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionDispatcherModule
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionTanModule
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionTestResultModule

@Module(
    includes = [
        OnboardingDeltaInteroperabilityModule::class,
        HomeFragmentModule::class,
        RiskDetailsFragmentModule::class
    ]
)
abstract class MainActivityModule {

    // activity specific injection module for future dependencies

    // example:
    // @ContributesAndroidInjector
    // abstract fun mainFragment(): MainFragment

    @ContributesAndroidInjector(modules = [InteroperabilityConfigurationFragmentModule::class])
    abstract fun intertopConfigScreen(): InteroperabilityConfigurationFragment

    @ContributesAndroidInjector(modules = [SubmissionTanModule::class])
    abstract fun submissionTanScreen(): SubmissionTanFragment

    @ContributesAndroidInjector(modules = [SubmissionDispatcherModule::class])
    abstract fun submissionDispatcherScreen(): SubmissionDispatcherFragment

    @ContributesAndroidInjector(modules = [SubmissionTestResultModule::class])
    abstract fun submissionTestResultScreen(): SubmissionTestResultFragment

    @ContributesAndroidInjector(modules = [SubmissionContactModule::class])
    abstract fun submissionContactScreen(): SubmissionContactFragment
}
