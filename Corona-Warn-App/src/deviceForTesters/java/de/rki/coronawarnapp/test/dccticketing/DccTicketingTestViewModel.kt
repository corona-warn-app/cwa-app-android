package de.rki.coronawarnapp.test.dccticketing

import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.dccticketing.core.qrcode.DccTicketingQrCodeSettings
import de.rki.coronawarnapp.util.encryption.ec.ECKeyPair
import de.rki.coronawarnapp.util.encryption.ec.EcKeyGenerator
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class DccTicketingTestViewModel @AssistedInject constructor(
    private val ecKeyGenerator: EcKeyGenerator,
    private val qrCodeSettings: DccTicketingQrCodeSettings,
) : CWAViewModel() {

    val ecKeyPair = SingleLiveEvent<ECKeyPair>()

    val checkServiceIdentity = qrCodeSettings.checkServiceIdentity

    fun generateECKeyPair() = launch {
        ecKeyPair.postValue(
            ecKeyGenerator.generateECKeyPair()
        )
    }

    fun toggleServiceIdentityCheck(isChecked: Boolean) {
        qrCodeSettings.checkServiceIdentity.update { isChecked }
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<DccTicketingTestViewModel>
}
