package de.rki.coronawarnapp.ui.submission.deletionwarning

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.coronatest.CoronaTestRepository
import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode
import de.rki.coronawarnapp.coronatest.qrcode.InvalidQRCodeException
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.coronatest.tan.CoronaTestTAN
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.TransactionException
import de.rki.coronawarnapp.exception.http.CwaWebException
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.ui.submission.ApiRequestState
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionNavigationEvents
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.flow.first
import timber.log.Timber

class SubmissionDeletionWarningViewModel @AssistedInject constructor(
    private val coronaTestRepository: CoronaTestRepository,
    private val submissionRepository: SubmissionRepository,
    @Assisted private val coronaTestQrCode: CoronaTestQRCode?,
    @Assisted private val coronaTestQrTan: CoronaTestTAN?,

    @Assisted private val isConsentGiven: Boolean,
) : CWAViewModel() {

    val routeToScreen = SingleLiveEvent<SubmissionNavigationEvents>()
    val showRedeemedTokenWarning = SingleLiveEvent<Unit>()
    private val mutableRegistrationState = MutableLiveData(RegistrationState(ApiRequestState.IDLE))
    val registrationState: LiveData<RegistrationState> = mutableRegistrationState
    val registrationError = SingleLiveEvent<CwaWebException>()

    fun deleteExistingAndRegisterNewTest() = launch {
        when (getRegistrationType()) {
            RegistrationType.QR -> deleteExistingAndRegisterNewTestWithQrCode()
            RegistrationType.TAN -> deleteExistingAndRegisterNewTestWitTAN()
        }
    }

    private suspend fun deleteExistingAndRegisterNewTestWithQrCode() {
        try {
            val currentTest = submissionRepository.testForType(coronaTestQrCode!!.type).first()
            coronaTestRepository.removeTest(currentTest!!.identifier)
            doDeviceRegistration(coronaTestQrCode)
        } catch (err: Exception) {
            Timber.e(err, "Removal of existing test failed with msg: ${err.message}")
            err.report(ExceptionCategory.INTERNAL)
        }
    }

    private suspend fun deleteExistingAndRegisterNewTestWitTAN() {
        try {
            val currentTest = submissionRepository.testForType(CoronaTest.Type.PCR).first()
            coronaTestRepository.removeTest(currentTest!!.identifier)
            onTanSubmit()
        } catch (err: Exception) {
            Timber.e(err, "Removal of existing test failed with msg: ${err.message}")
            err.report(ExceptionCategory.INTERNAL)
        }
    }

    private suspend fun onTanSubmit() {

        try {
            mutableRegistrationState.postValue(RegistrationState(ApiRequestState.STARTED))
            submissionRepository.registerTest(coronaTestQrTan!!)
            mutableRegistrationState.postValue(RegistrationState(ApiRequestState.SUCCESS))
        } catch (err: CwaWebException) {
            mutableRegistrationState.postValue(RegistrationState(ApiRequestState.FAILED))
            registrationError.postValue(err)
        } catch (err: TransactionException) {
            if (err.cause is CwaWebException) {
                registrationError.postValue(err.cause)
            } else {
                err.report(ExceptionCategory.INTERNAL)
            }
            mutableRegistrationState.postValue(RegistrationState(ApiRequestState.FAILED))
        } catch (err: Exception) {
            mutableRegistrationState.postValue(RegistrationState(ApiRequestState.FAILED))
            err.report(ExceptionCategory.INTERNAL)
        }
    }

    data class RegistrationState(
        val apiRequestState: ApiRequestState,
        val testResult: CoronaTestResult? = null
    )

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal suspend fun doDeviceRegistration(coronaTestQRCode: CoronaTestQRCode) {
        try {
            mutableRegistrationState.postValue(RegistrationState(ApiRequestState.STARTED))
            val coronaTest = submissionRepository.registerTest(coronaTestQRCode)
            if (isConsentGiven) {
                submissionRepository.giveConsentToSubmission(type = coronaTestQRCode.type)
            }
            checkTestResult(coronaTest.testResult)
            mutableRegistrationState.postValue(
                RegistrationState(
                    ApiRequestState.SUCCESS,
                    coronaTest.testResult
                )
            )
        } catch (err: CwaWebException) {
            Timber.e(err, "Msg: ${err.message}")
            mutableRegistrationState.postValue(RegistrationState(ApiRequestState.FAILED))
            registrationError.postValue(err)
        } catch (err: TransactionException) {
            Timber.e(err, "Msg: ${err.message}")
            if (err.cause is CwaWebException) {
                registrationError.postValue(err.cause)
            } else {
                err.report(ExceptionCategory.INTERNAL)
            }
            mutableRegistrationState.postValue(RegistrationState(ApiRequestState.FAILED))
        } catch (err: InvalidQRCodeException) {
            Timber.e(err, "Msg: ${err.message}")
            mutableRegistrationState.postValue(RegistrationState(ApiRequestState.FAILED))
            deregisterTestFromDevice(coronaTestQRCode)
            showRedeemedTokenWarning.postValue(Unit)
        } catch (err: Exception) {
            Timber.e(err, "Msg: ${err.message}")
            mutableRegistrationState.postValue(RegistrationState(ApiRequestState.FAILED))
            err.report(ExceptionCategory.INTERNAL)
        }
    }

    fun onCancelButtonClick() {
        routeToScreen.postValue(SubmissionNavigationEvents.NavigateToConsent)
    }

    fun triggerNavigationToSubmissionTestResultAvailableFragment() {
        routeToScreen.postValue(SubmissionNavigationEvents.NavigateToResultAvailableScreen(coronaTestQrCode!!.type))
    }

    fun triggerNavigationToSubmissionTestResultPendingFragment() {
        routeToScreen.postValue(SubmissionNavigationEvents.NavigateToResultPendingScreen(coronaTestQrCode!!.type))
    }

    private fun checkTestResult(testResult: CoronaTestResult) {
        if (testResult == CoronaTestResult.PCR_REDEEMED) {
            throw InvalidQRCodeException()
        }
    }

    private fun deregisterTestFromDevice(coronaTest: CoronaTestQRCode) {
        launch {
            Timber.d("deregisterTestFromDevice()")

            submissionRepository.removeTestFromDevice(type = coronaTest.type)
            routeToScreen.postValue(SubmissionNavigationEvents.NavigateToMainActivity)
        }
    }

    fun getRegistrationType(): RegistrationType {
        return if (coronaTestQrCode != null) {
            RegistrationType.QR
        } else {
            RegistrationType.TAN
        }
    }

    fun getTestType(): CoronaTest.Type {
        return coronaTestQrCode?.type ?: return CoronaTest.Type.PCR
    }

    sealed class RegistrationType {
        object TAN : RegistrationType()
        object QR : RegistrationType()
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
