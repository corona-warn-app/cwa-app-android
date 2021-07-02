package de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.passed

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class DccValidationPassedFragmentModule {

    @Binds
    @IntoMap
    @CWAViewModelKey(DccValidationPassedViewModel::class)
    abstract fun dccValidationPassedFragment(
        factory: DccValidationPassedViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
