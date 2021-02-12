package de.rki.coronawarnapp.test.datadonation.ui

import androidx.annotation.StringRes
import androidx.lifecycle.asLiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.SafetyNetRequirementsContainer
import de.rki.coronawarnapp.datadonation.safetynet.CWASafetyNet
import de.rki.coronawarnapp.datadonation.safetynet.DeviceAttestation
import de.rki.coronawarnapp.datadonation.safetynet.SafetyNetClientWrapper
import de.rki.coronawarnapp.datadonation.safetynet.SafetyNetException
import de.rki.coronawarnapp.datadonation.storage.OTPRepository
import de.rki.coronawarnapp.datadonation.survey.SurveyException
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.security.SecureRandom

class DataDonationTestFragmentViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    private val safetyNetClientWrapper: SafetyNetClientWrapper,
    private val secureRandom: SecureRandom,
    private val cwaSafetyNet: CWASafetyNet,
    otpRepository: OTPRepository,
    appConfigProvider: AppConfigProvider,
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    private val currentReportInternal = MutableStateFlow<SafetyNetClientWrapper.Report?>(null)
    val currentReport = currentReportInternal.asLiveData(context = dispatcherProvider.Default)

    private val currentValidationInternal =
        MutableStateFlow<Pair<SafetyNetRequirementsContainer?, Throwable?>?>(null)
    val currentValidation = currentValidationInternal.asLiveData(context = dispatcherProvider.Default)

    val errorEvents = SingleLiveEvent<Throwable>()
    val copyJWSEvent = SingleLiveEvent<String>()

    val otp: String = otpRepository.lastOTP?.toString() ?: "No OTP received yet"

    val surveyConfig = appConfigProvider.currentConfig
        .map { it.survey.toString() }
        .asLiveData(context = dispatcherProvider.Default)

    private val currentSafetyNetExceptionTypeInternal = MutableStateFlow(SafetyNetException.Type.values().first())
    val currentSafetyNetExceptionType =
        currentSafetyNetExceptionTypeInternal.asLiveData(context = dispatcherProvider.Default)

    private val currentSurveyExceptionTypeInternal = MutableStateFlow(SurveyException.Type.values().first())
    val currentSurveyExceptionType = currentSurveyExceptionTypeInternal.asLiveData(context = dispatcherProvider.Default)

    val showErrorDialog = SingleLiveEvent<@StringRes Int>()

    fun createSafetyNetReport() {
        launch {
            val nonce = ByteArray(16)
            secureRandom.nextBytes(nonce)
            try {
                val report = safetyNetClientWrapper.attest(nonce)
                currentReportInternal.value = report
            } catch (e: Exception) {
                Timber.e(e, "attest() failed.")
                errorEvents.postValue(e)
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
            secureRandom.nextBytes(payload)
            try {
                val result = cwaSafetyNet.attest(object : DeviceAttestation.Request {
                    override val scenarioPayload: ByteArray = payload
                })
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

    fun selectSafetyNetExceptionType(type: SafetyNetException.Type) {
        currentSafetyNetExceptionTypeInternal.value = type
    }

    fun showSafetyNetErrorDialog() {
        when (currentSafetyNetExceptionTypeInternal.value) {
            SafetyNetException.Type.APK_PACKAGE_NAME_MISMATCH,
            SafetyNetException.Type.ATTESTATION_FAILED,
            SafetyNetException.Type.ATTESTATION_REQUEST_FAILED,
            SafetyNetException.Type.DEVICE_TIME_UNVERIFIED,
            SafetyNetException.Type.NONCE_MISMATCH ->
                R.string.datadonation_details_survey_consent_error_TRY_AGAIN_LATER
            SafetyNetException.Type.BASIC_INTEGRITY_REQUIRED,
            SafetyNetException.Type.CTS_PROFILE_MATCH_REQUIRED,
            SafetyNetException.Type.EVALUATION_TYPE_BASIC_REQUIRED,
            SafetyNetException.Type.EVALUATION_TYPE_HARDWARE_BACKED_REQUIRED ->
                R.string.datadonation_details_survey_consent_error_DEVICE_NOT_TRUSTED
            SafetyNetException.Type.DEVICE_TIME_INCORRECT ->
                R.string.datadonation_details_survey_consent_error_CHANGE_DEVICE_TIME
            SafetyNetException.Type.PLAY_SERVICES_VERSION_MISMATCH ->
                R.string.datadonation_details_survey_consent_error_UPDATE_PLAY_SERVICES
            SafetyNetException.Type.TIME_SINCE_ONBOARDING_UNVERIFIED ->
                R.string.datadonation_details_survey_consent_error_TIME_SINCE_ONBOARDING_UNVERIFIED
        }.also { showErrorDialog.postValue(it) }
    }

    fun selectSurveyExceptionType(type: SurveyException.Type) {
        currentSurveyExceptionTypeInternal.value = type
    }

    fun showSurveyErrorDialog() {
        when (currentSurveyExceptionTypeInternal.value) {
            SurveyException.Type.ALREADY_PARTICIPATED_THIS_MONTH -> R.string.datadonation_details_survey_consent_error_ALREADY_PARTICIPATED
        }.also { showErrorDialog.postValue(it) }
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<DataDonationTestFragmentViewModel>
}
