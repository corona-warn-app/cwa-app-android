package de.rki.coronawarnapp.ui.submission.deletionwarning

import androidx.navigation.NavDirections
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.coronatest.TestRegistrationRequest
import de.rki.coronawarnapp.coronatest.tan.CoronaTestTAN
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.submission.TestRegistrationStateProcessor
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import timber.log.Timber

class SubmissionDeletionWarningViewModel @AssistedInject constructor(
    @Assisted private val testRegistrationRequest: TestRegistrationRequest,
    @Assisted private val isConsentGiven: Boolean,
    private val registrationStateProcessor: TestRegistrationStateProcessor,
) : CWAViewModel() {

    val routeToScreen = SingleLiveEvent<NavDirections>()
    val registrationState = registrationStateProcessor.state.asLiveData2()

    internal fun getTestType(): CoronaTest.Type = testRegistrationRequest.type

    fun deleteExistingAndRegisterNewTest() = launch {
        if (testRegistrationRequest.isDccSupportedByPoc) {
            SubmissionDeletionWarningFragmentDirections
                .actionSubmissionDeletionWarningFragmentToRequestCovidCertificateFragment(
                    testRegistrationRequest = testRegistrationRequest,
                    coronaTestConsent = isConsentGiven,
                    deleteOldTest = true
                ).run { routeToScreen.postValue(this) }
        } else {
            removeAndRegisterNew(testRegistrationRequest)
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
            is CoronaTestTAN ->
                SubmissionDeletionWarningFragmentDirections
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
            testRegistrationRequest: TestRegistrationRequest,
            isConsentGiven: Boolean
        ): SubmissionDeletionWarningViewModel
    }
}
