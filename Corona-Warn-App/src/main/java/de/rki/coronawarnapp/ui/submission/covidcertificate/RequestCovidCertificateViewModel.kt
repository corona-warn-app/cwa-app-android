package de.rki.coronawarnapp.ui.submission.covidcertificate

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode
import de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission.AnalyticsKeySubmissionCollector
import de.rki.coronawarnapp.submission.TestRegistrationStateProcessor
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import org.joda.time.LocalDate

class RequestCovidCertificateViewModel @AssistedInject constructor(
    @Assisted private val coronaTestQrCode: CoronaTestQRCode,
    @Assisted("coronaTestConsent") private val coronaTestConsent: Boolean,
    @Assisted("deleteOldTest") private val deleteOldTest: Boolean,
    private val registrationStateProcessor: TestRegistrationStateProcessor,
    private val analyticsKeySubmissionCollector: AnalyticsKeySubmissionCollector,
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

    fun navigateToHomeScreen() {
        events.postValue(ToHomeScreen)
    }

    fun navigateToDispatcherScreen() {
        events.postValue(ToDispatcherScreen)
    }

    private fun registerAndMaybeDelete(dccConsent: Boolean) = launch {
        val consentedQrCode = when (coronaTestQrCode) {
            is CoronaTestQRCode.PCR -> coronaTestQrCode.copy(
                dateOfBirth = birthDateData.value,
                isDccConsentGiven = dccConsent
            )
            is CoronaTestQRCode.RapidAntigen -> coronaTestQrCode.copy(isDccConsentGiven = dccConsent)
        }

        registrationStateProcessor.startRegistration(
            request = consentedQrCode,
            isSubmissionConsentGiven = coronaTestConsent,
            allowReplacement = deleteOldTest
        )

        if (coronaTestConsent) analyticsKeySubmissionCollector.reportAdvancedConsentGiven(consentedQrCode.type)
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<RequestCovidCertificateViewModel> {
        fun create(
            coronaTestQrCode: CoronaTestQRCode,
            @Assisted("coronaTestConsent") coronaTestConsent: Boolean,
            @Assisted("deleteOldTest") deleteOldTest: Boolean
        ): RequestCovidCertificateViewModel
    }
}
