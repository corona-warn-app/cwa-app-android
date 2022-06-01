package de.rki.coronawarnapp.familytest.ui.consent

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.bugreporting.censors.family.FamilyTestCensor
import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode
import de.rki.coronawarnapp.submission.TestRegistrationStateProcessor
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import de.rki.coronawarnapp.util.flow.combine

class FamilyTestConsentViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    @Assisted private val coronaTestQRCode: CoronaTestQRCode,
    private val familyTestCensor: FamilyTestCensor,
    private val registrationStateProcessor: TestRegistrationStateProcessor
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    val routeToScreen = SingleLiveEvent<FamilyTestConsentNavigationEvents>()
    val registrationState = registrationStateProcessor.state.asLiveData2()

    private val personName = MutableStateFlow("")

    val isSubmittable = combine(personName, registrationStateProcessor.state) { name, state ->
        name.isNotEmpty() && state !is TestRegistrationStateProcessor.State.Working
    }.asLiveData2()

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

        when {
            coronaTestQRCode.isDccSupportedByPoc -> {
                FamilyTestConsentNavigationEvents.NavigateToCertificateRequest(
                    coronaTestQRCode = coronaTestQRCode,
                    consentGiven = true,
                    allowReplacement = false,
                    personName = personName
                ).also { routeToScreen.postValue(it) }
            }
            else -> {
                registrationStateProcessor.startFamilyTestRegistration(
                    request = coronaTestQRCode,
                    personName = personName
                )
            }
        }
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<FamilyTestConsentViewModel> {
        fun create(
            coronaTestQRCode: CoronaTestQRCode
        ): FamilyTestConsentViewModel
    }
}
