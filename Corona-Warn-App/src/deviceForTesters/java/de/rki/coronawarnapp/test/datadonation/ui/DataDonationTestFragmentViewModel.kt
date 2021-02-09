package de.rki.coronawarnapp.test.datadonation.ui

import androidx.lifecycle.asLiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.datadonation.analytics.Analytics
import de.rki.coronawarnapp.appconfig.SafetyNetRequirementsContainer
import de.rki.coronawarnapp.datadonation.safetynet.CWASafetyNet
import de.rki.coronawarnapp.datadonation.safetynet.DeviceAttestation
import de.rki.coronawarnapp.datadonation.safetynet.SafetyNetClientWrapper
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import timber.log.Timber
import java.security.SecureRandom

class DataDonationTestFragmentViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    private val safetyNetClientWrapper: SafetyNetClientWrapper,
    private val secureRandom: SecureRandom,
    private val analytics: Analytics,
    private val cwaSafetyNet: CWASafetyNet
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

    fun createSafetyNetReport() {
        launch {
            val nonce = ByteArray(16)
            secureRandom.nextBytes(nonce)
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

    fun collectAnalyticsData() = launch {
        try {
            val ppaDataAndroid = PpaData.PPADataAndroid.newBuilder()
            analytics.collectContributions(ppaDataBuilder = ppaDataAndroid)
            currentAnalyticsDataInternal.value = ppaDataAndroid.build()
        } catch (e: Exception) {
            Timber.e(e, "collectContributions() failed.")
            infoEvents.postValue(e.toString())
        }
    }

    fun submitAnalytics() = launch {
        infoEvents.postValue("Starting Analytics Submission")
        analytics.submitAnalyticsData()
        infoEvents.postValue("Analytics Submission Done")
    }

    fun copyAnalytics() = launch {
        val value = currentAnalyticsData.value?.toString() ?: ""
        copyAnalyticsEvent.postValue(value)
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<DataDonationTestFragmentViewModel>
}
