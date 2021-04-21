package de.rki.coronawarnapp.ui.submission.qrcode

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.MutableLiveData
import de.rki.coronawarnapp.bugreporting.censors.QRCodeCensor
import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode
import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQrCodeValidator
import de.rki.coronawarnapp.coronatest.qrcode.InvalidQRCodeException
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.TransactionException
import de.rki.coronawarnapp.exception.http.CwaWebException
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.ui.submission.ApiRequestState
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import timber.log.Timber
import javax.inject.Inject

class QrCodeSubmission @Inject constructor(
    private val submissionRepository: SubmissionRepository,
    private val qrCodeValidator: CoronaTestQrCodeValidator
) {

    data class RegistrationState(
        val apiRequestState: ApiRequestState,
        val testResult: CoronaTestResult? = null
    )

    //val routeToScreen = SingleLiveEvent<SubmissionNavigationEvents>()
    val showRedeemedTokenWarning = SingleLiveEvent<Unit>()
    val qrCodeValidationState = SingleLiveEvent<ValidationState>()
    val registrationState = MutableLiveData(RegistrationState(ApiRequestState.IDLE))
    val registrationError = SingleLiveEvent<CwaWebException>()

    suspend fun startQrCodeRegistration(rawResult: String) {
        try {
            val coronaTestQRCode = qrCodeValidator.validate(rawResult)
            // TODO this needs to be adapted to work for different types
            QRCodeCensor.lastGUID = coronaTestQRCode.registrationIdentifier
            qrCodeValidationState.postValue(ValidationState.SUCCESS)
            doDeviceRegistration(coronaTestQRCode)
        } catch (err: InvalidQRCodeException) {
            qrCodeValidationState.postValue(ValidationState.INVALID)
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal suspend fun doDeviceRegistration(coronaTestQRCode: CoronaTestQRCode) =
        try {
            registrationState.postValue(RegistrationState(ApiRequestState.STARTED))
            val coronaTest = submissionRepository.registerTest(coronaTestQRCode)
            submissionRepository.giveConsentToSubmission(type = coronaTestQRCode.type)
            checkTestResult(coronaTest.testResult)
            registrationState.postValue(
                RegistrationState(
                    ApiRequestState.SUCCESS,
                    coronaTest.testResult
                )
            )
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
        } catch (err: InvalidQRCodeException) {
            registrationState.postValue(RegistrationState(ApiRequestState.FAILED))
            Timber.d("deregisterTestFromDevice()")
            //TODO
            submissionRepository.removeTestFromDevice(type = CoronaTest.Type.PCR)
            //routeToScreen.postValue(SubmissionNavigationEvents.NavigateToMainActivity)
            showRedeemedTokenWarning.postValue(Unit)
        } catch (err: Exception) {
            registrationState.postValue(RegistrationState(ApiRequestState.FAILED))
            err.report(ExceptionCategory.INTERNAL)
        }

    private fun checkTestResult(testResult: CoronaTestResult) {
        if (testResult == CoronaTestResult.PCR_REDEEMED) {
            throw InvalidQRCodeException()
        }
    }

    enum class ValidationState {
        STARTED, INVALID, SUCCESS
    }
}


