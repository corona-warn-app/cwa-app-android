package de.rki.coronawarnapp.srs.ui

import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.srs.ui.consent.SrsSubmissionConsentFragment
import de.rki.coronawarnapp.srs.ui.consent.SrsSubmissionConsentFragmentModule
import de.rki.coronawarnapp.srs.ui.typeselction.SrsTypeSelectionFragment
import de.rki.coronawarnapp.srs.ui.typeselction.SrsTypeSelectionFragmentModule

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
}
