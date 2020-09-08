package de.rki.coronawarnapp.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.rki.coronawarnapp.ui.SingleLiveEvent
import de.rki.coronawarnapp.ui.submission.EuropeanConsentEvent

class EuropeanFederalGatewayServerConsentViewModel : ViewModel() {

    val isEuropeanConsentGranted: MutableLiveData<Boolean> = MutableLiveData<Boolean>(false)
    val routeToScreen: SingleLiveEvent<EuropeanConsentEvent> = SingleLiveEvent()

    fun onNextButtonClick() {
        if(isEuropeanConsentGranted.value!!) {
            routeToScreen.value = EuropeanConsentEvent.NavigateToTargetGermany
        }
        else{
            routeToScreen.value = EuropeanConsentEvent.NavigateToKeysSubmission
        }
    }

    fun onBackButtonClick() {
        routeToScreen.value = EuropeanConsentEvent.NavigateToPreviousScreen
    }

    fun updateSwitch(onOff: Boolean) {
        isEuropeanConsentGranted.value = onOff
    }
}
