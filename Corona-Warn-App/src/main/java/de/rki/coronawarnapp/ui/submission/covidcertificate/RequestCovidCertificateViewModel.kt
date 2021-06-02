package de.rki.coronawarnapp.ui.submission.covidcertificate

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.coronatest.CoronaTestRepository
import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode
import de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission.AnalyticsKeySubmissionCollector
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.ui.submission.qrcode.QrCodeRegistrationStateProcessor
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.flow.first
import org.joda.time.LocalDate
import timber.log.Timber

class RequestCovidCertificateViewModel @AssistedInject constructor(
    @Assisted private val coronaTestQrCode: CoronaTestQRCode,
    @Assisted("coronaTestConsent") private val coronaTestConsent: Boolean,
    @Assisted("deleteOldTest") private val deleteOldTest: Boolean,
    private val qrCodeRegistrationStateProcessor: QrCodeRegistrationStateProcessor,
    private val analyticsKeySubmissionCollector: AnalyticsKeySubmissionCollector,
    private val submissionRepository: SubmissionRepository,
    private val coronaTestRepository: CoronaTestRepository,
) : CWAViewModel() {

    // Test registration LiveData
    val showRedeemedTokenWarning = qrCodeRegistrationStateProcessor.showRedeemedTokenWarning
    val registrationState = qrCodeRegistrationStateProcessor.registrationState
    val registrationError = qrCodeRegistrationStateProcessor.registrationError
    val removalError = SingleLiveEvent<Throwable>()

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
        if (deleteOldTest) removeOldTest()
        registerWithDccConsent(dccConsent)
    }

    private suspend fun registerWithDccConsent(dccConsent: Boolean) {
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

    private suspend fun removeOldTest() {
        try {
            submissionRepository.testForType(coronaTestQrCode.type).first()?.let {
                coronaTestRepository.removeTest(it.identifier)
            } ?: Timber.e("Test for type ${coronaTestQrCode.type} is not found")
        } catch (e: Exception) {
            Timber.d(e, "removeOldTest failed")
            removalError.postValue(e)
        }
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
