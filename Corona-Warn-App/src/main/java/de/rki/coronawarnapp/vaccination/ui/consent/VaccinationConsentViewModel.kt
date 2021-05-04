package de.rki.coronawarnapp.vaccination.ui.consent

import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.vaccination.core.repository.VaccinationRepository

class VaccinationConsentViewModel @AssistedInject constructor(
    private val vaccinationRepository: VaccinationRepository,
    dispatcherProvider: DispatcherProvider
) : CWAViewModel() {

    fun doConsent() {
     //TODO: implement me
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<VaccinationConsentViewModel> {
        fun create(): VaccinationConsentViewModel
    }
}
