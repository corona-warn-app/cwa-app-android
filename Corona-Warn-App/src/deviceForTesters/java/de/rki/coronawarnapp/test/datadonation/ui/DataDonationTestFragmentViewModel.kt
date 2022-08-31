package de.rki.coronawarnapp.test.datadonation.ui

import androidx.lifecycle.asLiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.SafetyNetRequirementsContainer
import de.rki.coronawarnapp.datadonation.analytics.Analytics
import de.rki.coronawarnapp.datadonation.analytics.storage.LastAnalyticsSubmission
import de.rki.coronawarnapp.datadonation.analytics.storage.LastAnalyticsSubmissionLogger
import de.rki.coronawarnapp.datadonation.safetynet.CWASafetyNet
import de.rki.coronawarnapp.datadonation.safetynet.DeviceAttestation
import de.rki.coronawarnapp.datadonation.safetynet.SafetyNetClientWrapper
import de.rki.coronawarnapp.datadonation.safetynet.SafetyNetException
import de.rki.coronawarnapp.datadonation.storage.OTPRepository
import de.rki.coronawarnapp.datadonation.survey.SurveyException
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData
import de.rki.coronawarnapp.storage.TestSettings
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.security.RandomStrong
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import kotlin.random.Random

class DataDonationTestFragmentViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    private val safetyNetClientWrapper: SafetyNetClientWrapper,
    @RandomStrong private val randomSource: Random,
    private val analytics: Analytics,
    private val lastAnalyticsSubmissionLogger: LastAnalyticsSubmissionLogger,
    private val cwaSafetyNet: CWASafetyNet,
    otpRepository: OTPRepository,
    private val appConfigProvider: AppConfigProvider,
    private val testSettings: TestSettings
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    val infoEvents = SingleLiveEvent<String>()

    private val currentReportInternal = MutableStateFlow<SafetyNetClientWrapper.Report?>(null)
    val currentReport = currentReportInternal.asLiveData(context = dispatcherProvider.Default)

    private val currentValidationInternal =
        MutableStateFlow<Pair<SafetyNetRequirementsContainer?, Throwable?>?>(null)
    val currentValidation = currentValidationInternal.asLiveData(context = dispatcherProvider.Default)
    val copyJWSEvent = SingleLiveEvent<String>()

    private val currentAnalyticsDataInternal = MutableStateFlow<PpaData.PPADataAndroid?>(null)
    val currentAnalyticsData = currentAnalyticsDataInternal.asLiveData(context = dispatcherProvider.Default)
    val copyAnalyticsEvent = SingleLiveEvent<String>()

    private val lastAnalyticsDataInternal = MutableStateFlow<LastAnalyticsSubmission?>(null)
    val lastAnalyticsData = lastAnalyticsDataInternal.asLiveData(context = dispatcherProvider.Default)

    val isSafetyNetTimeCheckSkipped = testSettings.skipSafetyNetTimeCheck
        .asLiveData(context = dispatcherProvider.Default)

    val otp: String = otpRepository.otpAuthorizationResult?.toString() ?: "No OTP generated and authorized yet"

    val surveyConfig = appConfigProvider.currentConfig
        .map { it.survey.toString() }
        .asLiveData(context = dispatcherProvider.Default)

    private val currentSafetyNetExceptionTypeInternal = MutableStateFlow(SafetyNetException.Type.values().first())
    val currentSafetyNetExceptionType =
        currentSafetyNetExceptionTypeInternal.asLiveData(context = dispatcherProvider.Default)

    private val currentSurveyExceptionTypeInternal = MutableStateFlow(SurveyException.Type.values().first())
    val currentSurveyExceptionType = currentSurveyExceptionTypeInternal.asLiveData(context = dispatcherProvider.Default)

    val showErrorDialog = SingleLiveEvent<Exception>()

    fun createSafetyNetReport() {
        launch {
            val nonce = ByteArray(16)
            randomSource.nextBytes(nonce)
            try {
                val report = safetyNetClientWrapper.attest(nonce)
                currentReportInternal.value = report
            } catch (e: Exception) {
                Timber.e(e, "attest() failed.")
                infoEvents.postValue(e.toString())
            }
        }
    }

    fun validateSafetyNetStrict() {
        validateRequirements(
            SafetyNetRequirementsContainer(
                requireBasicIntegrity = true,
                requireCTSProfileMatch = true,
                requireEvaluationTypeBasic = true,
                requireEvaluationTypeHardwareBacked = true
            )
        )
    }

    fun validateSafetyNetCasually() {
        validateRequirements(SafetyNetRequirementsContainer())
    }

    private fun validateRequirements(requirements: SafetyNetRequirementsContainer) {
        launch {
            val payload = ByteArray(16)
            randomSource.nextBytes(payload)
            try {
                val result = cwaSafetyNet.attest(
                    object : DeviceAttestation.Request {
                        override val scenarioPayload: ByteArray = payload
                    }
                )
                result.requirePass(requirements)
                currentValidationInternal.value = requirements to null
            } catch (e: Exception) {
                Timber.e(e, "validateRequirements() did not pass.")
                currentValidationInternal.value = requirements to e
            }
        }
    }

    fun copyJWS() {
        launch {
            val value = currentReport.value?.jwsResult ?: ""
            copyJWSEvent.postValue(value)
        }
    }

    fun collectAnalyticsData() = launch {
        try {
            val configData = appConfigProvider.getAppConfig()
            val ppaDataAndroid = PpaData.PPADataAndroid.newBuilder()
            analytics.collectContributions(configData, ppaDataAndroid)
            currentAnalyticsDataInternal.value = ppaDataAndroid.build()
        } catch (e: Exception) {
            Timber.e(e, "collectContributions() failed.")
            infoEvents.postValue(e.toString())
        }
    }

    fun submitAnalytics() = launch {
        infoEvents.postValue("Starting Analytics Submission")
        val configData = appConfigProvider.getAppConfig()
        analytics.submitAnalyticsData(configData)
        infoEvents.postValue("Analytics Submission Done")
        checkLastAnalytics()
    }

    fun copyAnalytics() = launch {
        val value = currentAnalyticsData.value?.toString() ?: ""
        copyAnalyticsEvent.postValue(value)
    }

    fun checkLastAnalytics() = launch {
        try {
            lastAnalyticsDataInternal.value = lastAnalyticsSubmissionLogger.getLastAnalyticsData()
        } catch (e: Exception) {
            Timber.e(e, "checkLastAnalytics() failed.")
            infoEvents.postValue(e.toString())
        }
    }

    fun toggleSkipSafetyNetTimeCheck() = launch {
        testSettings.updateSkipSafetyNetTimeCheck { !it }
    }

    fun selectSafetyNetExceptionType(type: SafetyNetException.Type) {
        currentSafetyNetExceptionTypeInternal.value = type
    }

    fun showSafetyNetErrorDialog() {
        showErrorDialog.postValue(
            SafetyNetException(
                type = currentSafetyNetExceptionTypeInternal.value,
                message = "simulated"
            )
        )
    }

    fun selectSurveyExceptionType(type: SurveyException.Type) {
        currentSurveyExceptionTypeInternal.value = type
    }

    fun showSurveyErrorDialog() {
        showErrorDialog.postValue(
            SurveyException(type = currentSurveyExceptionTypeInternal.value)
        )
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<DataDonationTestFragmentViewModel>
}
