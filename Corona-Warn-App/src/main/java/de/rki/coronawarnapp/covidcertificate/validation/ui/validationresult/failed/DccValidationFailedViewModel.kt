package de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.failed

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.covidcertificate.validation.core.DccValidation
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory

class DccValidationFailedViewModel @AssistedInject constructor(
    @Assisted private val validation: DccValidation,
    dispatcherProvider: DispatcherProvider,
) : CWAViewModel(dispatcherProvider) {

    @AssistedFactory
    interface Factory : CWAViewModelFactory<DccValidationFailedViewModel> {
        fun create(validation: DccValidation): DccValidationFailedViewModel
    }
}
