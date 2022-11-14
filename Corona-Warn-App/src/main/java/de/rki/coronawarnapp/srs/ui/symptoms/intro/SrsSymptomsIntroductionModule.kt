package de.rki.coronawarnapp.srs.ui.symptoms.intro

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class SrsSymptomsIntroductionModule {

    @Binds
    @IntoMap
    @CWAViewModelKey(SrsSymptomsIntroductionViewModel::class)
    abstract fun srsSymptionIntroductionViewModel(
        factory: SrsSymptomsIntroductionViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
