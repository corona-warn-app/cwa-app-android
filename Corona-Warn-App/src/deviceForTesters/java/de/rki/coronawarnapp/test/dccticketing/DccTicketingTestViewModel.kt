package de.rki.coronawarnapp.test.dccticketing

import androidx.lifecycle.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import de.rki.coronawarnapp.dccticketing.core.qrcode.DccTicketingQrCodeSettings
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.encryption.ec.ECKeyPair
import de.rki.coronawarnapp.util.encryption.ec.EcKeyGenerator
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import javax.inject.Inject

@HiltViewModel
class DccTicketingTestViewModel @Inject constructor(
    dispatcherProvider: DispatcherProvider,
    private val ecKeyGenerator: EcKeyGenerator,
    private val qrCodeSettings: DccTicketingQrCodeSettings,
) : CWAViewModel(dispatcherProvider) {

    val ecKeyPair = SingleLiveEvent<ECKeyPair>()

    val checkServiceIdentity = qrCodeSettings.checkServiceIdentity.asLiveData()

    fun generateECKeyPair() = launch {
        ecKeyPair.postValue(
            ecKeyGenerator.generateECKeyPair()
        )
    }

    fun toggleServiceIdentityCheck(isChecked: Boolean) = launch {
        qrCodeSettings.updateCheckServiceIdentity(isChecked)
    }
}
