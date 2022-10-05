package de.rki.coronawarnapp.ui.submission.covidcertificate

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.coronatest.TestRegistrationRequest
import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode
import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode.CategoryType
import de.rki.coronawarnapp.submission.TestRegistrationStateProcessor
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import java.time.LocalDate

class RequestCovidCertificateViewModel @AssistedInject constructor(
    @Assisted private val testRequest: TestRegistrationRequest,
    @Assisted("coronaTestConsent") private val coronaTestConsent: Boolean,
    @Assisted("allowTestReplacement") private val allowTestReplacement: Boolean,
    @Assisted private val personName: String?,
    private val registrationStateProcessor: TestRegistrationStateProcessor,
) : CWAViewModel() {

    val registrationState = registrationStateProcessor.state.asLiveData2()

    private val birthDateData = MutableLiveData<LocalDate?>(null)
    val birthDate: LiveData<LocalDate?> = birthDateData
    val events = SingleLiveEvent<RequestDccNavEvent>()

    fun birthDateChanged(localDate: LocalDate?) {
        birthDateData.value = localDate
    }

    fun onAgreeGC() = registerTestWithDccConsent(dccConsent = true)

    fun onDisagreeGC() = registerTestWithDccConsent(dccConsent = false)

    fun navigateBack() = events.postValue(Back)

    private fun registerTestWithDccConsent(dccConsent: Boolean) = launch {
        val consentedQrCode = when (testRequest) {
            is CoronaTestQRCode.PCR -> testRequest.copy(
                dateOfBirth = birthDateData.value,
                isDccConsentGiven = dccConsent
            )

            is CoronaTestQRCode.RapidPCR -> testRequest.copy(isDccConsentGiven = dccConsent)
            is CoronaTestQRCode.RapidAntigen -> testRequest.copy(isDccConsentGiven = dccConsent)
            else -> testRequest
        }

        if (consentedQrCode is CoronaTestQRCode && consentedQrCode.categoryType == CategoryType.FAMILY) {
            requireNotNull(personName) { "Family test should have a person name" }
            registrationStateProcessor.startFamilyTestRegistration(
                request = consentedQrCode,
                personName = personName,
            )
        } else {
            registrationStateProcessor.startTestRegistration(
                request = consentedQrCode,
                isSubmissionConsentGiven = coronaTestConsent,
                allowTestReplacement = allowTestReplacement
            )
        }
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<RequestCovidCertificateViewModel> {
        fun create(
            testRegistrationRequest: TestRegistrationRequest,
            @Assisted("coronaTestConsent") coronaTestConsent: Boolean,
            @Assisted("allowTestReplacement") allowTestReplacement: Boolean,
            @Assisted personName: String?,
        ): RequestCovidCertificateViewModel
    }
}
