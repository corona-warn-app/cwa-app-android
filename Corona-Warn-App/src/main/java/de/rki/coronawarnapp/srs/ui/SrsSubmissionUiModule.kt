package de.rki.coronawarnapp.srs.ui

import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.srs.ui.checkins.SrsCheckinsFragment
import de.rki.coronawarnapp.srs.ui.checkins.SrsCheckinsFragmentModule
import de.rki.coronawarnapp.srs.ui.consent.SrsSubmissionConsentFragment
import de.rki.coronawarnapp.srs.ui.consent.SrsSubmissionConsentFragmentModule
import de.rki.coronawarnapp.srs.ui.symptoms.calendar.SrsSymptomsCalendarFragment
import de.rki.coronawarnapp.srs.ui.symptoms.calendar.SrsSymptomsCalendarModule
import de.rki.coronawarnapp.srs.ui.symptoms.intro.SrsSymptomsIntroductionFragment
import de.rki.coronawarnapp.srs.ui.symptoms.intro.SrsSymptomsIntroductionModule
import de.rki.coronawarnapp.srs.ui.typeselection.SrsTypeSelectionFragment
import de.rki.coronawarnapp.srs.ui.typeselection.SrsTypeSelectionFragmentModule

@Module(
    includes = [
        SrsSubmissionConsentFragmentModule::class
    ]
)
abstract class SrsSubmissionUiModule {

    @ContributesAndroidInjector(modules = [SrsSubmissionConsentFragmentModule::class])
    abstract fun srsSubmissionConsentFragment(): SrsSubmissionConsentFragment

    @ContributesAndroidInjector(modules = [SrsTypeSelectionFragmentModule::class])
    abstract fun srsSubmissionTypeSelectionFragment(): SrsTypeSelectionFragment

    @ContributesAndroidInjector(modules = [SrsCheckinsFragmentModule::class])
    abstract fun srsCheckinsFragment(): SrsCheckinsFragment

    @ContributesAndroidInjector(modules = [SrsSymptomsIntroductionModule::class])
    abstract fun srsSymptomsIntroductionFragment(): SrsSymptomsIntroductionFragment

    @ContributesAndroidInjector(modules = [SrsSymptomsCalendarModule::class])
    abstract fun srsSymptomsCalendarFragment(): SrsSymptomsCalendarFragment
}
