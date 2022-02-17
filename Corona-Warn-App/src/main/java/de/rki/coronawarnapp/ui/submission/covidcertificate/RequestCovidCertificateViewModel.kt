package de.rki.coronawarnapp.ui.submission.covidcertificate

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.coronatest.TestRegistrationRequest
import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode
import de.rki.coronawarnapp.submission.TestRegistrationStateProcessor
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import org.joda.time.LocalDate

class RequestCovidCertificateViewModel @AssistedInject constructor(
    @Assisted private val testRegistrationRequest: TestRegistrationRequest,
    @Assisted("coronaTestConsent") private val coronaTestConsent: Boolean,
    @Assisted("deleteOldTest") private val deleteOldTest: Boolean,
    private val registrationStateProcessor: TestRegistrationStateProcessor,
) : CWAViewModel() {

    val registrationState = registrationStateProcessor.state.asLiveData2()

    private val birthDateData = MutableLiveData<LocalDate>(null)
    val birthDate: LiveData<LocalDate> = birthDateData
    val events = SingleLiveEvent<RequestDccNavEvent>()

    fun birthDateChanged(localDate: LocalDate?) {
        birthDateData.value = localDate
    }

    fun onAgreeGC() = registerAndMaybeDelete(dccConsent = true)

    fun onDisagreeGC() = registerAndMaybeDelete(dccConsent = false)

    fun navigateBack() {
        events.postValue(Back)
    }

    private fun registerAndMaybeDelete(dccConsent: Boolean) = launch {
        val consentedQrCode = when (testRegistrationRequest) {
            is CoronaTestQRCode.PCR -> testRegistrationRequest.copy(
                dateOfBirth = birthDateData.value,
                isDccConsentGiven = dccConsent
            )
            is CoronaTestQRCode.RapidPCR -> testRegistrationRequest.copy(isDccConsentGiven = dccConsent)
            is CoronaTestQRCode.RapidAntigen -> testRegistrationRequest.copy(isDccConsentGiven = dccConsent)
            else -> testRegistrationRequest
        }

        registrationStateProcessor.startRegistration(
            request = consentedQrCode,
            isSubmissionConsentGiven = coronaTestConsent,
            allowReplacement = deleteOldTest
        )
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<RequestCovidCertificateViewModel> {
        fun create(
            testRegistrationRequest: TestRegistrationRequest,
            @Assisted("coronaTestConsent") coronaTestConsent: Boolean,
            @Assisted("deleteOldTest") deleteOldTest: Boolean
        ): RequestCovidCertificateViewModel
    }
}
