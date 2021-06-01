package de.rki.coronawarnapp.ui.submission.greencertificate

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode
import de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission.AnalyticsKeySubmissionCollector
import de.rki.coronawarnapp.ui.submission.qrcode.QrCodeRegistrationStateProcessor
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import org.joda.time.LocalDate

class RequestCovidCertificateViewModel @AssistedInject constructor(
    @Assisted private val coronaTestQrCode: CoronaTestQRCode,
    @Assisted private val coronaTestConsent: Boolean,
    private val qrCodeRegistrationStateProcessor: QrCodeRegistrationStateProcessor,
    private val analyticsKeySubmissionCollector: AnalyticsKeySubmissionCollector,
) : CWAViewModel() {

    // Test registration LiveData
    val showRedeemedTokenWarning = qrCodeRegistrationStateProcessor.showRedeemedTokenWarning
    val registrationState = qrCodeRegistrationStateProcessor.registrationState
    val registrationError = qrCodeRegistrationStateProcessor.registrationError

    private val birthDateData = MutableLiveData<LocalDate>(null)
    val birthDate: LiveData<LocalDate> = birthDateData
    val events = SingleLiveEvent<RequestDccNavEvent>()

    fun birthDateChanged(localDate: LocalDate?) {
        birthDateData.value = localDate
    }

    fun onAgreeGC() = registerWithDccConsent(dccConsent = true)

    fun onDisagreeGC() = registerWithDccConsent(dccConsent = false)

    fun navigateBack() {
        events.postValue(Back)
    }

    fun navigateToHomeScreen() {
        events.postValue(ToHomeScreen)
    }

    fun navigateToDispatcherScreen() {
        events.postValue(ToDispatcherScreen)
    }

    private fun registerWithDccConsent(dccConsent: Boolean) = launch {
        val consentedQrCode = when (coronaTestQrCode) {
            is CoronaTestQRCode.PCR -> coronaTestQrCode.copy(
                dateOfBirth = birthDateData.value,
                isDccConsentGiven = dccConsent
            )
            is CoronaTestQRCode.RapidAntigen -> coronaTestQrCode.copy(isDccConsentGiven = dccConsent)
        }

        qrCodeRegistrationStateProcessor.startQrCodeRegistration(consentedQrCode, coronaTestConsent)
        if (coronaTestConsent) analyticsKeySubmissionCollector.reportAdvancedConsentGiven(consentedQrCode.type)
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<RequestCovidCertificateViewModel> {
        fun create(
            coronaTestQrCode: CoronaTestQRCode,
            coronaTestConsent: Boolean
        ): RequestCovidCertificateViewModel
    }
}
