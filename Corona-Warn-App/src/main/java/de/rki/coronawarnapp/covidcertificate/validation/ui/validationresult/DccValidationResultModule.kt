package de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.failed.DccValidationFailedViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class DccValidationResultModule {

    @Binds
    @IntoMap
    @CWAViewModelKey(DccValidationFailedViewModel::class)
    abstract fun dccValidationFailedFragment(
        factory: DccValidationFailedViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
