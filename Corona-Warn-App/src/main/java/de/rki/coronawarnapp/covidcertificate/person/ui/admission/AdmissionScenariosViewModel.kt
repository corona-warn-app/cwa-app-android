package de.rki.coronawarnapp.covidcertificate.person.ui.admission

import dagger.assisted.AssistedFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import javax.inject.Inject

class AdmissionScenariosViewModel @Inject constructor() : CWAViewModel() {

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<AdmissionScenariosViewModel>
}
