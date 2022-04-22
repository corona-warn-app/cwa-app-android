package de.rki.coronawarnapp.familytest.ui.consent

import androidx.lifecycle.asLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.bugreporting.censors.family.FamilyTestCensor
import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class FamilyTestConsentViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    @Assisted private val coronaTestQRCode: CoronaTestQRCode,
    private val familyTestCensor: FamilyTestCensor,
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    val routeToScreen = SingleLiveEvent<FamilyTestConsentNavigationEvents>()

    private val personName = MutableStateFlow("")

    val isValid = personName
        .map { it.isNotEmpty() }
        .asLiveData()

    fun nameChanged(value: String) {
        personName.value = value
    }

    fun onDataPrivacyClick() {
        routeToScreen.postValue(FamilyTestConsentNavigationEvents.NavigateToDataPrivacy)
    }

    fun onNavigateBack() {
        routeToScreen.postValue(FamilyTestConsentNavigationEvents.NavigateBack)
    }

    fun onConsentButtonClick() = launch {
        val personName = personName.first()
        familyTestCensor.addName(personName)
        FamilyTestConsentNavigationEvents.NavigateToCertificateRequest(
            coronaTestQRCode = coronaTestQRCode,
            consentGiven = true,
            allowReplacement = false,
            personName = personName
        ).run { routeToScreen.postValue(this) }
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<FamilyTestConsentViewModel> {
        fun create(
            coronaTestQRCode: CoronaTestQRCode
        ): FamilyTestConsentViewModel
    }
}
