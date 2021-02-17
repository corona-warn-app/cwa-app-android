package de.rki.coronawarnapp.ui.submission.qrcode.scan

import androidx.lifecycle.MutableLiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.bugreporting.censors.QRCodeCensor
import de.rki.coronawarnapp.datadonation.analytics.storage.AnalyticsSettings
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.TransactionException
import de.rki.coronawarnapp.exception.http.CwaWebException
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.risk.RiskLevelResult
import de.rki.coronawarnapp.risk.RiskState
import de.rki.coronawarnapp.risk.storage.RiskLevelStorage
import de.rki.coronawarnapp.risk.tryLatestResultsWithDefaults
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData
import de.rki.coronawarnapp.service.submission.QRScanResult
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.ui.submission.ApiRequestState
import de.rki.coronawarnapp.ui.submission.ScanStatus
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionNavigationEvents
import de.rki.coronawarnapp.util.formatter.TestResult
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.first
import org.joda.time.Instant
import timber.log.Timber

class SubmissionQRCodeScanViewModel @AssistedInject constructor(
    private val submissionRepository: SubmissionRepository,
    private val analyticsSettings: AnalyticsSettings,
    private val riskLevelStorage: RiskLevelStorage,
) :
    CWAViewModel() {
    val routeToScreen = SingleLiveEvent<SubmissionNavigationEvents>()
    val showRedeemedTokenWarning = SingleLiveEvent<Unit>()
    val scanStatusValue = SingleLiveEvent<ScanStatus>()

    open class InvalidQRCodeException : Exception("error in qr code")

    fun validateTestGUID(rawResult: String) {
        val scanResult = QRScanResult(rawResult)
        if (scanResult.isValid) {
            QRCodeCensor.lastGUID = scanResult.guid
            scanStatusValue.postValue(ScanStatus.SUCCESS)
            doDeviceRegistration(scanResult)
        } else {
            scanStatusValue.postValue(ScanStatus.INVALID)
        }
    }

    val registrationState = MutableLiveData(RegistrationState(ApiRequestState.IDLE))
    val registrationError = SingleLiveEvent<CwaWebException>()

    data class RegistrationState(
        val apiRequestState: ApiRequestState,
        val testResult: TestResult? = null
    )

    private fun doDeviceRegistration(scanResult: QRScanResult) = launch {
        try {
            registrationState.postValue(RegistrationState(ApiRequestState.STARTED))
            val testResult = submissionRepository.asyncRegisterDeviceViaGUID(scanResult.guid!!)
            checkTestResult(testResult)
            registrationState.postValue(RegistrationState(ApiRequestState.SUCCESS, testResult))
            // Order here is important. Save Analytics after SUCCESS
            saveTestResultAnalyticsSettings(testResult)
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
            deregisterTestFromDevice()
            showRedeemedTokenWarning.postValue(Unit)
        } catch (err: Exception) {
            registrationState.postValue(RegistrationState(ApiRequestState.FAILED))
            err.report(ExceptionCategory.INTERNAL)
        }
    }

    // Collect Test result registration only after user has given a consent.
    // To exclude any registered test result before giving a consent
    private suspend fun saveTestResultAnalyticsSettings(testResult: TestResult) = with(analyticsSettings) {
        if (analyticsEnabled.value) {
            val lastRiskResult = riskLevelStorage
                .latestAndLastSuccessful
                .first()
                .tryLatestResultsWithDefaults()
                .lastCalculated

            val ppaRiskLevel = lastRiskResult.toMetadataRiskLevel()
            testScannedAfterConsent.update { true }
            riskLevelAtTestRegistration.update { ppaRiskLevel }
            if (testResult == TestResult.PENDING) {
                pendingResultReceivedAt.update { Instant.now() }
            }
        }
    }

    private fun checkTestResult(testResult: TestResult) {
        if (testResult == TestResult.REDEEMED) {
            throw InvalidQRCodeException()
        }
    }

    private fun deregisterTestFromDevice() {
        launch {
            Timber.d("deregisterTestFromDevice()")

            submissionRepository.removeTestFromDevice()

            routeToScreen.postValue(SubmissionNavigationEvents.NavigateToMainActivity)
        }
    }

    private fun RiskLevelResult.toMetadataRiskLevel(): PpaData.PPARiskLevel =
        when (riskState) {
            RiskState.INCREASED_RISK -> PpaData.PPARiskLevel.RISK_LEVEL_HIGH
            else -> PpaData.PPARiskLevel.RISK_LEVEL_LOW
        }

    fun onBackPressed() {
        routeToScreen.postValue(SubmissionNavigationEvents.NavigateToConsent)
    }

    fun onClosePressed() {
        routeToScreen.postValue(SubmissionNavigationEvents.NavigateToDispatcher)
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<SubmissionQRCodeScanViewModel>
}
