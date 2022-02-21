package de.rki.coronawarnapp.dccreissuance.ui.consent

import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class DccReissuanceConsentViewModel @AssistedInject constructor() : CWAViewModel() {

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<DccReissuanceConsentViewModel>
}
