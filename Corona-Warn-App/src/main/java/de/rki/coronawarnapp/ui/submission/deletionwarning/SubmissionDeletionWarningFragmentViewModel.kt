package de.rki.coronawarnapp.ui.submission.deletionwarning

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.MutableLiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.coronatest.CoronaTestRepository
import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestGUID
import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.TransactionException
import de.rki.coronawarnapp.exception.http.CwaWebException
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.service.submission.QRScanResult
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.ui.submission.ApiRequestState
import de.rki.coronawarnapp.ui.submission.qrcode.scan.SubmissionQRCodeScanViewModel
import de.rki.coronawarnapp.util.formatter.TestResult
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.lang.Exception

class SubmissionDeletionWarningFragmentViewModel @AssistedInject constructor(
    private val coronaTestRepository: CoronaTestRepository,
    private val submissionRepository: SubmissionRepository,
) : CWAViewModel() {

    var testDeletionFinished = MutableLiveData<Boolean>()
    val showRedeemedTokenWarning = SingleLiveEvent<Unit>()


    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<SubmissionDeletionWarningFragmentViewModel>

    fun deleteExistingTest(qrScanResult: CoronaTestQRCode) = launch {

        try {
            coronaTestRepository.removeTest(qrScanResult.guid)

        } catch (e: NotImplementedError) {
            Timber.d("Test removing failed with exception: ${e.toString()}")
        } finally {
            withContext(Dispatchers.Main) {
                testDeletionFinished.value = true
            }
        }
    }

    val registrationState = MutableLiveData(SubmissionQRCodeScanViewModel.RegistrationState(ApiRequestState.IDLE))
    val registrationError = SingleLiveEvent<CwaWebException>()

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun doDeviceRegistration(scanResult: QRScanResult) = launch {
        try {
            registrationState.postValue(SubmissionQRCodeScanViewModel.RegistrationState(ApiRequestState.STARTED))
            val testResult = submissionRepository.asyncRegisterDeviceViaGUID(scanResult.guid!!)
            checkTestResult(testResult)
            registrationState.postValue(
                SubmissionQRCodeScanViewModel.RegistrationState(
                    ApiRequestState.SUCCESS,
                    testResult
                )
            )
        } catch (err: CwaWebException) {
            registrationState.postValue(SubmissionQRCodeScanViewModel.RegistrationState(ApiRequestState.FAILED))
            registrationError.postValue(err)
        } catch (err: TransactionException) {
            if (err.cause is CwaWebException) {
                registrationError.postValue(err.cause)
            } else {
                err.report(ExceptionCategory.INTERNAL)
            }
            registrationState.postValue(SubmissionQRCodeScanViewModel.RegistrationState(ApiRequestState.FAILED))
        } catch (err: SubmissionQRCodeScanViewModel.InvalidQRCodeException) {
            registrationState.postValue(SubmissionQRCodeScanViewModel.RegistrationState(ApiRequestState.FAILED))
            //deregisterTestFromDevice()
            showRedeemedTokenWarning.postValue(Unit)
        } catch (err: Exception) {
            registrationState.postValue(SubmissionQRCodeScanViewModel.RegistrationState(ApiRequestState.FAILED))
            err.report(ExceptionCategory.INTERNAL)
        }
    }

    private fun checkTestResult(testResult: TestResult) {
        if (testResult == TestResult.REDEEMED) {
            throw SubmissionQRCodeScanViewModel.InvalidQRCodeException()
        }
    }
}
