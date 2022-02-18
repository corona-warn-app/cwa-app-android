package de.rki.coronawarnapp.dccreissuance.ui.success

import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class DccReissuanceSuccessViewModel @AssistedInject constructor() : CWAViewModel() {

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<DccReissuanceSuccessViewModel>
}
