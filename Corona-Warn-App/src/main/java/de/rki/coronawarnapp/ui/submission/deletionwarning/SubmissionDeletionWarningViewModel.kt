package de.rki.coronawarnapp.ui.submission.deletionwarning

import androidx.navigation.NavDirections
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.coronatest.TestRegistrationRequest
import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode
import de.rki.coronawarnapp.coronatest.tan.CoronaTestTAN
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.submission.TestRegistrationStateProcessor
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import timber.log.Timber

class SubmissionDeletionWarningViewModel @AssistedInject constructor(
    @Assisted private val coronaTestQrCode: CoronaTestQRCode?,
    @Assisted private val coronaTestQrTan: CoronaTestTAN?,
    @Assisted private val isConsentGiven: Boolean,
    private val registrationStateProcessor: TestRegistrationStateProcessor,
) : CWAViewModel() {

    val routeToScreen = SingleLiveEvent<NavDirections>()
    val registrationState = registrationStateProcessor.state.asLiveData2()

    // If there is no qrCode, it must be a TAN, and TANs are always PCR
    internal fun getTestType(): CoronaTest.Type = coronaTestQrCode?.type ?: CoronaTest.Type.PCR

    fun deleteExistingAndRegisterNewTest() = launch {
        require(coronaTestQrCode != null || coronaTestQrTan != null) {
            "Neither QRCode, nor TAN was available."
        }

        if (coronaTestQrCode?.isDccSupportedByPoc == true) {
            SubmissionDeletionWarningFragmentDirections
                .actionSubmissionDeletionWarningFragmentToRequestCovidCertificateFragment(
                    coronaTestQrCode = coronaTestQrCode,
                    coronaTestConsent = isConsentGiven,
                    deleteOldTest = true
                ).run { routeToScreen.postValue(this) }
        } else {
            removeAndRegisterNew(coronaTestQrCode ?: coronaTestQrTan!!)
        }
    }

    private suspend fun removeAndRegisterNew(request: TestRegistrationRequest) {
        val newTest = registrationStateProcessor.startRegistration(
            request = request,
            isSubmissionConsentGiven = isConsentGiven,
            allowReplacement = true
        )

        if (newTest == null) {
            Timber.w("Test registration failed.")
            return
        } else {
            Timber.d("Continuing with our new CoronaTest: %s", newTest)
        }

        when (request) {
            is CoronaTestTAN -> SubmissionDeletionWarningFragmentDirections
                .actionSubmissionDeletionFragmentToSubmissionTestResultNoConsentFragment(newTest.type)

            else -> if (newTest.isPositive) {
                SubmissionDeletionWarningFragmentDirections
                    .actionSubmissionDeletionWarningFragmentToSubmissionTestResultAvailableFragment(newTest.type)
            } else {
                SubmissionDeletionWarningFragmentDirections
                    .actionSubmissionDeletionWarningFragmentToSubmissionTestResultPendingFragment(newTest.type)
            }
        }.run { routeToScreen.postValue(this) }
    }

    fun onCancelButtonClick() {
        SubmissionDeletionWarningFragmentDirections
            .actionSubmissionDeletionWarningFragmentToSubmissionConsentFragment()
            .run { routeToScreen.postValue(this) }
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<SubmissionDeletionWarningViewModel> {
        fun create(
            coronaTestQrCode: CoronaTestQRCode?,
            coronaTestTan: CoronaTestTAN?,
            isConsentGiven: Boolean
        ): SubmissionDeletionWarningViewModel
    }
}
