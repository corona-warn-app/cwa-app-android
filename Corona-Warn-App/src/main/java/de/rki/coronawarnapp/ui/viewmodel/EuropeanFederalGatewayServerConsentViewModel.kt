package de.rki.coronawarnapp.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class EuropeanFederalGatewayServerConsentViewModel : ViewModel() {

    val isEuropeanConsentGranted: MutableLiveData<Boolean> = MutableLiveData<Boolean>(false)
}
