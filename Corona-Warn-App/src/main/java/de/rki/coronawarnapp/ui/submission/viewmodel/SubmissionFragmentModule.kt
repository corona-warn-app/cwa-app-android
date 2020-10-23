package de.rki.coronawarnapp.ui.submission.viewmodel

import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.ui.submission.fragment.SubmissionContactFragment
import de.rki.coronawarnapp.ui.submission.fragment.SubmissionDispatcherFragment
import de.rki.coronawarnapp.ui.submission.fragment.SubmissionDoneFragment
import de.rki.coronawarnapp.ui.submission.fragment.SubmissionIntroFragment
import de.rki.coronawarnapp.ui.submission.fragment.SubmissionQRCodeInfoFragment
import de.rki.coronawarnapp.ui.submission.fragment.SubmissionQRCodeInfoModule
import de.rki.coronawarnapp.ui.submission.fragment.SubmissionQRCodeScanFragment
import de.rki.coronawarnapp.ui.submission.fragment.SubmissionResultPositiveOtherWarningFragment
import de.rki.coronawarnapp.ui.submission.fragment.SubmissionSymptomCalendarFragment
import de.rki.coronawarnapp.ui.submission.fragment.SubmissionSymptomIntroductionFragment
import de.rki.coronawarnapp.ui.submission.fragment.SubmissionTanFragment
import de.rki.coronawarnapp.ui.submission.fragment.SubmissionTestResultFragment

@Module
internal abstract class SubmissionFragmentModule {

    // activity specific injection module for future dependencies

    // example:
    // @ContributesAndroidInjector
    // abstract fun onboardingFragment(): OnboardingFragmentFolder

    @ContributesAndroidInjector(modules = [SubmissionTanModule::class])
    abstract fun submissionTanScreen(): SubmissionTanFragment

    @ContributesAndroidInjector(modules = [SubmissionDispatcherModule::class])
    abstract fun submissionDispatcherScreen(): SubmissionDispatcherFragment

    @ContributesAndroidInjector(modules = [SubmissionTestResultModule::class])
    abstract fun submissionTestResultScreen(): SubmissionTestResultFragment

    @ContributesAndroidInjector(modules = [SubmissionContactModule::class])
    abstract fun submissionContactScreen(): SubmissionContactFragment

    @ContributesAndroidInjector(modules = [SubmissionDoneModule::class])
    abstract fun submissionDoneScreen(): SubmissionDoneFragment

    @ContributesAndroidInjector(modules = [SubmissionIntroModule::class])
    abstract fun submissionIntroScreen(): SubmissionIntroFragment

    @ContributesAndroidInjector(modules = [SubmissionQRCodeScanModule::class])
    abstract fun submissionQRCodeScanScreen(): SubmissionQRCodeScanFragment

    @ContributesAndroidInjector(modules = [SubmissionResultPositiveOtherWarningModule::class])
    abstract fun submissionResultPositiveOtherWarningScreen(): SubmissionResultPositiveOtherWarningFragment

    @ContributesAndroidInjector(modules = [SubmissionSymptomIntroductionModule::class])
    abstract fun submissionSymptomIntroductionScreen(): SubmissionSymptomIntroductionFragment

    @ContributesAndroidInjector(modules = [SubmissionSymptomCalendarModule::class])
    abstract fun submissionSymptomCalendarScreen(): SubmissionSymptomCalendarFragment

    @ContributesAndroidInjector(modules = [SubmissionQRCodeInfoModule::class])
    abstract fun submissionQRCodeInfoScreen(): SubmissionQRCodeInfoFragment
}
