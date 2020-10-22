package de.rki.coronawarnapp.ui.submission

import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.ui.submission.fragment.SubmissionContactFragment
import de.rki.coronawarnapp.ui.submission.fragment.SubmissionDispatcherFragment
import de.rki.coronawarnapp.ui.submission.fragment.SubmissionDoneFragment
import de.rki.coronawarnapp.ui.submission.fragment.SubmissionIntroFragment
import de.rki.coronawarnapp.ui.submission.qrcode.info.SubmissionQRCodeInfoFragment
import de.rki.coronawarnapp.ui.submission.qrcode.info.SubmissionQRCodeInfoModule
import de.rki.coronawarnapp.ui.submission.qrcode.scan.SubmissionQRCodeScanFragment
import de.rki.coronawarnapp.ui.submission.qrcode.scan.SubmissionQRCodeScanModule
import de.rki.coronawarnapp.ui.submission.symptoms.calendar.SubmissionSymptomCalendarFragment
import de.rki.coronawarnapp.ui.submission.symptoms.calendar.SubmissionSymptomCalendarModule
import de.rki.coronawarnapp.ui.submission.symptoms.introduction.SubmissionSymptomIntroductionFragment
import de.rki.coronawarnapp.ui.submission.symptoms.introduction.SubmissionSymptomIntroductionModule
import de.rki.coronawarnapp.ui.submission.tan.SubmissionTanFragment
import de.rki.coronawarnapp.ui.submission.tan.SubmissionTanModule
import de.rki.coronawarnapp.ui.submission.testresult.SubmissionTestResultFragment
import de.rki.coronawarnapp.ui.submission.testresult.SubmissionTestResultModule
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionContactModule
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionDispatcherModule
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionDoneModule
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionIntroModule
import de.rki.coronawarnapp.ui.submission.warnothers.SubmissionResultPositiveOtherWarningFragment
import de.rki.coronawarnapp.ui.submission.warnothers.SubmissionResultPositiveOtherWarningModule

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
