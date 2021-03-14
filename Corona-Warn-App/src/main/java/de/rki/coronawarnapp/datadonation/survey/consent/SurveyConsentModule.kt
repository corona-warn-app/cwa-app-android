package de.rki.coronawarnapp.datadonation.survey.consent

import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class SurveyConsentModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(SurveyConsentViewModel::class)
    abstract fun surveyConsentVM(
        factory: SurveyConsentViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>

    @ContributesAndroidInjector
    abstract fun surveyConsentFragment(): SurveyConsentFragment
}
