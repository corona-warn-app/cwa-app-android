package de.rki.coronawarnapp.covidcertificate.validation.ui

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class ValidationStartModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(ValidationStartViewModel::class)
    abstract fun validationStartFragment(
        factory: ValidationStartViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
