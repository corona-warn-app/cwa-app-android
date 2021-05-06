package de.rki.coronawarnapp.vaccination.ui.consent

import androidx.lifecycle.asLiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.storage.interoperability.InteroperabilityRepository
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import de.rki.coronawarnapp.vaccination.core.repository.VaccinationRepository

class VaccinationConsentViewModel @AssistedInject constructor(
    private val vaccinationRepository: VaccinationRepository,
    interoperabilityRepository: InteroperabilityRepository,
    dispatcherProvider: DispatcherProvider
) : CWAViewModel(dispatcherProvider) {

    val countryList = interoperabilityRepository.countryList
        .asLiveData(context = dispatcherProvider.Default)

    fun doConsent() {
        //TODO: implement me
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<VaccinationConsentViewModel>
}
