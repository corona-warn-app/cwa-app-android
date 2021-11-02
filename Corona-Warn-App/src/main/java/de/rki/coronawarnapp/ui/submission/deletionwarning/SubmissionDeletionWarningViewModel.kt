package de.rki.coronawarnapp.ui.submission.deletionwarning

import androidx.navigation.NavDirections
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.coronatest.TestRegistrationRequest
import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode
import de.rki.coronawarnapp.coronatest.tan.CoronaTestTAN
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.reyclebin.coronatest.RecycledCoronaTestsProvider
import de.rki.coronawarnapp.reyclebin.coronatest.request.RestoreRecycledTestRequest
import de.rki.coronawarnapp.submission.TestRegistrationStateProcessor
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import timber.log.Timber

class SubmissionDeletionWarningViewModel @AssistedInject constructor(
    @Assisted private val testRegistrationRequest: TestRegistrationRequest,
    private val registrationStateProcessor: TestRegistrationStateProcessor,
    private val recycledCoronaTestsProvider: RecycledCoronaTestsProvider
) : CWAViewModel() {

    val routeToScreen = SingleLiveEvent<NavDirections>()
    val registrationState = registrationStateProcessor.state.asLiveData2()

    internal fun getTestType(): CoronaTest.Type = testRegistrationRequest.type

    fun deleteExistingAndRegisterNewTest() = launch {
        removeAndRegisterNew(testRegistrationRequest)
    }

    private suspend fun removeAndRegisterNew(request: TestRegistrationRequest) {
        when (request) {
            is CoronaTestTAN -> {
                val newTest = registrationStateProcessor.startRegistration(
                    request = request,
                    isSubmissionConsentGiven = false,
                    allowReplacement = true
                )

                if (newTest == null) {
                    Timber.w("Test registration failed.")
                    return
                } else {
                    Timber.d("Continuing with our new CoronaTest: %s", newTest)
                }

                routeToScreen.postValue(
                    SubmissionDeletionWarningFragmentDirections
                        .actionSubmissionDeletionFragmentToSubmissionTestResultNoConsentFragment(newTest.type)
                )
            }

            is CoronaTestQRCode -> routeToScreen.postValue(
                SubmissionDeletionWarningFragmentDirections
                    .actionSubmissionDeletionWarningFragmentToSubmissionConsentFragment(
                        request,
                        allowTestReplacement = true
                    )
            )

            is RestoreRecycledTestRequest -> {
                recycledCoronaTestsProvider.restoreCoronaTest(request.identifier)
                if (request.fromRecycleBin) {
                    SubmissionDeletionWarningFragmentDirections.actionSubmissionDeletionWarningFragmentToRecycleBin()
                } else {
                    SubmissionDeletionWarningFragmentDirections
                        .actionSubmissionDeletionWarningFragmentToSubmissionTestResultPendingFragment(
                            testType = request.type,
                            forceTestResultUpdate = true
                        )
                }
            }
        }
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<SubmissionDeletionWarningViewModel> {
        fun create(
            testRegistrationRequest: TestRegistrationRequest
        ): SubmissionDeletionWarningViewModel
    }
}
