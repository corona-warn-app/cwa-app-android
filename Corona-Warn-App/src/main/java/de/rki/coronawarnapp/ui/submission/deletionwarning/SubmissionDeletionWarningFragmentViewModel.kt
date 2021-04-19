package de.rki.coronawarnapp.ui.submission.deletionwarning

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.MutableLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.contactdiary.ui.day.ContactDiaryDayViewModel
import de.rki.coronawarnapp.coronatest.CoronaTestRepository
import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.TransactionException
import de.rki.coronawarnapp.exception.http.CwaWebException
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.ui.submission.ApiRequestState
import de.rki.coronawarnapp.ui.submission.qrcode.scan.SubmissionQRCodeScanViewModel
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionNavigationEvents
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.runBlocking
import timber.log.Timber

class SubmissionDeletionWarningFragmentViewModel @AssistedInject constructor(
    private val coronaTestRepository: CoronaTestRepository,
    private val submissionRepository: SubmissionRepository,
    @Assisted private val coronaTest: CoronaTestQRCode,
    @Assisted private val isConsentGiven: Boolean,
    ) : CWAViewModel() {

    val routeToScreen = SingleLiveEvent<SubmissionNavigationEvents>()
    val showRedeemedTokenWarning = SingleLiveEvent<Unit>()
    val registrationState = MutableLiveData(RegistrationState(ApiRequestState.IDLE))
    val registrationError = SingleLiveEvent<CwaWebException>()

    fun deleteExistingAndRegisterNewTest() = launch {
        coronaTestRepository.removeTest(coronaTest.type)
        doDeviceRegistration(coronaTest, isConsentGiven)
    }


    data class RegistrationState(
        val apiRequestState: ApiRequestState,
        val testResult: CoronaTestResult? = null
    )

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    suspend internal fun doDeviceRegistration(request: CoronaTestQRCode, consentGiven: Boolean) {
        try {
            registrationState.postValue(RegistrationState(ApiRequestState.STARTED))
            val coronaTest = submissionRepository.registerTest(request)
            // TODO this needs to depend on what the user selected
            if(consentGiven) {
                submissionRepository.giveConsentToSubmission(type = request.type)
            }
            checkTestResult(coronaTest.testResult)
            registrationState.postValue(RegistrationState(ApiRequestState.SUCCESS, coronaTest.testResult))
        } catch (err: CwaWebException) {
            registrationState.postValue(RegistrationState(ApiRequestState.FAILED))
            registrationError.postValue(err)
        } catch (err: TransactionException) {
            if (err.cause is CwaWebException) {
                registrationError.postValue(err.cause)
            } else {
                err.report(ExceptionCategory.INTERNAL)
            }
            registrationState.postValue(RegistrationState(ApiRequestState.FAILED))
        } catch (err: SubmissionQRCodeScanViewModel.InvalidQRCodeException) {
            registrationState.postValue(RegistrationState(ApiRequestState.FAILED))
            deregisterTestFromDevice(request)
            showRedeemedTokenWarning.postValue(Unit)
        } catch (err: Exception) {
            registrationState.postValue(RegistrationState(ApiRequestState.FAILED))
            err.report(ExceptionCategory.INTERNAL)
        }
    }

    private fun checkTestResult(testResult: CoronaTestResult) {
        if (testResult == CoronaTestResult.PCR_REDEEMED) {
            throw SubmissionQRCodeScanViewModel.InvalidQRCodeException()
        }
    }

    private fun deregisterTestFromDevice(coronaTest:CoronaTestQRCode) {
        launch {
            Timber.d("deregisterTestFromDevice()")

            submissionRepository.removeTestFromDevice(type = coronaTest.type)
            routeToScreen.postValue(SubmissionNavigationEvents.NavigateToMainActivity)
        }
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<SubmissionDeletionWarningFragmentViewModel> {
        fun create(coronaTest: CoronaTestQRCode, isConsentGiven: Boolean): SubmissionDeletionWarningFragmentViewModel
    }
}
