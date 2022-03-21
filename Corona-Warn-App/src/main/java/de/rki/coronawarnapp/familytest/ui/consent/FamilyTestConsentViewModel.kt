package de.rki.coronawarnapp.familytest.ui.consent

import androidx.lifecycle.asLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.lang.Exception

class FamilyTestConsentViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    @Assisted private val coronaTestQRCode: CoronaTestQRCode,
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    private val registrationStateInternal = MutableStateFlow<State>(State.Idle) // TODO: FOR TEST ONLY
    val registrationState = registrationStateInternal.asLiveData() // TODO: FOR TEST ONLY

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
        try {
            // TODO: Replace with proper test registration process
            registrationStateInternal.value = State.Working // TODO: FOR TEST ONLY
            delay(2000) // TODO: FOR TEST ONLY
            FamilyTestConsentNavigationEvents.NavigateToCertificateRequest(
                coronaTestQRCode = coronaTestQRCode,
                consentGiven = true,
                allowReplacement = false // TODO: check if it should be passed as navArg
            ).run { routeToScreen.postValue(this) }
        } catch (exception: Exception) {
            // TODO: exception handler
            Timber.d(exception, "Something went wrong...")
        }
    }

    // TODO: FOR TEST ONLY
    sealed class State {
        object Idle : State()
        object Working : State()
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<FamilyTestConsentViewModel> {
        fun create(
            coronaTestQRCode: CoronaTestQRCode
        ): FamilyTestConsentViewModel
    }
}
