package de.rki.coronawarnapp.test.dccticketing

import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.util.encryption.ec.ECKeyPair
import de.rki.coronawarnapp.util.encryption.ec.EcKeyGenerator
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class DccTicketingTestViewModel @AssistedInject constructor(
    private val ecKeyGenerator: EcKeyGenerator
) : CWAViewModel() {

    val ecKeyPair = SingleLiveEvent<ECKeyPair>()

    fun generateECKeyPair() = launch {
        ecKeyPair.postValue(
            ecKeyGenerator.generateECKeyPair()
        )
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<DccTicketingTestViewModel>
}
