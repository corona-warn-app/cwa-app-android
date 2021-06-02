package de.rki.coronawarnapp.ui.submission.qrcode

import androidx.lifecycle.MutableLiveData
import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode
import de.rki.coronawarnapp.coronatest.qrcode.InvalidQRCodeException
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.http.CwaWebException
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.ui.submission.ApiRequestState
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import timber.log.Timber
import javax.inject.Inject

class QrCodeRegistrationStateProcessor @Inject constructor(
    private val submissionRepository: SubmissionRepository
) {

    data class RegistrationState(
        val apiRequestState: ApiRequestState,
        val test: CoronaTest? = null
    )

    val showRedeemedTokenWarning = SingleLiveEvent<Unit>()
    val registrationState = MutableLiveData(RegistrationState(ApiRequestState.IDLE))
    val registrationError = SingleLiveEvent<CwaWebException>()

    suspend fun startQrCodeRegistration(coronaTestQRCode: CoronaTestQRCode, isConsentGiven: Boolean) =
        try {
            registrationState.postValue(RegistrationState(ApiRequestState.STARTED))
            val coronaTest = submissionRepository.registerTest(coronaTestQRCode)
            if (isConsentGiven) {
                submissionRepository.giveConsentToSubmission(type = coronaTestQRCode.type)
            }
            checkTestResult(coronaTestQRCode, coronaTest)
            registrationState.postValue(RegistrationState(ApiRequestState.SUCCESS, coronaTest))
        } catch (err: CwaWebException) {
            registrationState.postValue(RegistrationState(ApiRequestState.FAILED))
            registrationError.postValue(err)
        } catch (err: InvalidQRCodeException) {
            registrationState.postValue(RegistrationState(ApiRequestState.FAILED))
            Timber.d("deregisterTestFromDevice()")
            submissionRepository.removeTestFromDevice(type = coronaTestQRCode.type)
            showRedeemedTokenWarning.postValue(Unit)
        } catch (err: Exception) {
            registrationState.postValue(RegistrationState(ApiRequestState.FAILED))
            err.report(ExceptionCategory.INTERNAL)
        }

    private fun checkTestResult(request: CoronaTestQRCode, test: CoronaTest) {
        if (test.isRedeemed) {
            throw InvalidQRCodeException("CoronaTestResult already redeemed ${request.registrationIdentifier}")
        }
    }

    enum class ValidationState {
        STARTED, INVALID, SUCCESS
    }
}
