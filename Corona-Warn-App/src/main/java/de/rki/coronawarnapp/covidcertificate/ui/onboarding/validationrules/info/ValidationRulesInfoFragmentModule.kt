package de.rki.coronawarnapp.covidcertificate.ui.onboarding.validationrules.info

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class ValidationRulesInfoFragmentModule {

    @Binds
    @IntoMap
    @CWAViewModelKey(ValidationRulesInfoViewModel::class)
    abstract fun validationRulesInfoFragment(
        factory: ValidationRulesInfoViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
