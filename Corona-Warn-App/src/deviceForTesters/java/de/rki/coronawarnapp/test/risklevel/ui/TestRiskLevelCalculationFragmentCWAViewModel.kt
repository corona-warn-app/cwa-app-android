package de.rki.coronawarnapp.test.risklevel.ui

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.google.android.gms.nearby.exposurenotification.ExposureInformation
import com.google.android.gms.nearby.exposurenotification.ExposureNotificationClient
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.diagnosiskeys.storage.KeyCacheRepository
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.TransactionException
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.nearby.InternalExposureNotificationClient
import de.rki.coronawarnapp.risk.DefaultRiskLevelCalculation
import de.rki.coronawarnapp.risk.RiskLevel
import de.rki.coronawarnapp.risk.TimeVariables
import de.rki.coronawarnapp.server.protocols.AppleLegacyKeyExchange
import de.rki.coronawarnapp.service.applicationconfiguration.ApplicationConfigurationService
import de.rki.coronawarnapp.storage.AppDatabase
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.storage.RiskLevelRepository
import de.rki.coronawarnapp.transaction.RetrieveDiagnosisKeysTransaction
import de.rki.coronawarnapp.transaction.RiskLevelTransaction
import de.rki.coronawarnapp.util.KeyFileHelper
import de.rki.coronawarnapp.util.security.SecurityHelper
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class TestRiskLevelCalculationFragmentCWAViewModel @AssistedInject constructor(
    @Assisted private val handle: SavedStateHandle,
    @Assisted private val exampleArg: String?,
    private val context: Context, // App context
    private val exposureNotificationClient: ExposureNotificationClient,
    private val keyCacheRepository: KeyCacheRepository
) : CWAViewModel() {

    val startLocalQRCodeScanEvent = SingleLiveEvent<Unit>()
    val riskLevelResetEvent = SingleLiveEvent<Unit>()
    val apiKeysProvidedEvent = SingleLiveEvent<DiagnosisKeyProvidedEvent>()
    val riskScoreState = MutableLiveData<RiskScoreState>(RiskScoreState())

    init {
        Timber.d("CWAViewModel: %s", this)
        Timber.d("SavedStateHandle: %s", handle)
        Timber.d("Example arg: %s", exampleArg)
    }

    fun retrieveDiagnosisKeys() {
        viewModelScope.launch {
            try {
                RetrieveDiagnosisKeysTransaction.start()
                calculateRiskLevel()
            } catch (e: TransactionException) {
                e.report(ExceptionCategory.INTERNAL)
            }
        }
    }

    fun calculateRiskLevel() {
        viewModelScope.launch {
            try {
                RiskLevelTransaction.start()
            } catch (e: TransactionException) {
                e.report(ExceptionCategory.INTERNAL)
            }
        }
    }

    fun resetRiskLevel() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    // Preference reset
                    SecurityHelper.resetSharedPrefs()
                    // Database Reset
                    AppDatabase.reset(context)
                    // Export File Reset
                    keyCacheRepository.clear()

                    LocalData.lastCalculatedRiskLevel(RiskLevel.UNDETERMINED.raw)
                    LocalData.lastSuccessfullyCalculatedRiskLevel(RiskLevel.UNDETERMINED.raw)
                    LocalData.lastTimeDiagnosisKeysFromServerFetch(null)
                    LocalData.googleApiToken(null)
                } catch (e: Exception) {
                    e.report(ExceptionCategory.INTERNAL)
                }
            }
            RiskLevelTransaction.start()
            riskLevelResetEvent.postValue(Unit)
        }
    }

    data class RiskScoreState(
        val riskScoreMsg: String = "",
        val backendParameters: String = "",
        val exposureSummary: String = "",
        val formula: String = "",
        val fullConfig: String = "",
        val exposureInfo: String = ""
    )

    fun startENFObserver() {
        viewModelScope.launch {
            try {
                var workState = riskScoreState.value!!

                val googleToken = LocalData.googleApiToken() ?: UUID.randomUUID().toString()
                val exposureSummary =
                    InternalExposureNotificationClient.asyncGetExposureSummary(googleToken)

                val appConfig =
                    ApplicationConfigurationService.asyncRetrieveApplicationConfiguration()

                val riskLevelScore = DefaultRiskLevelCalculation().calculateRiskScore(
                    appConfig.attenuationDuration,
                    exposureSummary
                )

                val riskAsString = "Level: ${RiskLevelRepository.getLastCalculatedScore()}\n" +
                    "Last successful Level: " +
                    "${LocalData.lastSuccessfullyCalculatedRiskLevel()}\n" +
                    "Calculated Score: ${riskLevelScore}\n" +
                    "Last Time Server Fetch: ${LocalData.lastTimeDiagnosisKeysFromServerFetch()}\n" +
                    "Tracing Duration: " +
                    "${TimeUnit.MILLISECONDS.toDays(TimeVariables.getTimeActiveTracingDuration())} days \n" +
                    "Tracing Duration in last 14 days: " +
                    "${TimeVariables.getActiveTracingDaysInRetentionPeriod()} days \n" +
                    "Last time risk level calculation ${LocalData.lastTimeRiskLevelCalculation()}"

                workState = workState.copy(riskScoreMsg = riskAsString)

                val lowClass =
                    appConfig.riskScoreClasses?.riskClassesList?.find { low -> low.label == "LOW" }
                val highClass =
                    appConfig.riskScoreClasses?.riskClassesList?.find { high -> high.label == "HIGH" }

                val configAsString =
                    "Attenuation Weight Low: ${appConfig.attenuationDuration?.weights?.low}\n" +
                        "Attenuation Weight Mid: ${appConfig.attenuationDuration?.weights?.mid}\n" +
                        "Attenuation Weight High: ${appConfig.attenuationDuration?.weights?.high}\n\n" +
                        "Attenuation Offset: ${appConfig.attenuationDuration?.defaultBucketOffset}\n" +
                        "Attenuation Normalization: " +
                        "${appConfig.attenuationDuration?.riskScoreNormalizationDivisor}\n\n" +
                        "Risk Score Low Class: ${lowClass?.min ?: 0} - ${lowClass?.max ?: 0}\n" +
                        "Risk Score High Class: ${highClass?.min ?: 0} - ${highClass?.max ?: 0}"

                workState = workState.copy(backendParameters = configAsString)

                val summaryAsString =
                    "Days Since Last Exposure: ${exposureSummary.daysSinceLastExposure}\n" +
                        "Matched Key Count: ${exposureSummary.matchedKeyCount}\n" +
                        "Maximum Risk Score: ${exposureSummary.maximumRiskScore}\n" +
                        "Attenuation Durations: [${
                            exposureSummary.attenuationDurationsInMinutes?.get(
                                0
                            )
                        }," +
                        "${exposureSummary.attenuationDurationsInMinutes?.get(1)}," +
                        "${exposureSummary.attenuationDurationsInMinutes?.get(2)}]\n" +
                        "Summation Risk Score: ${exposureSummary.summationRiskScore}"

                workState = workState.copy(exposureSummary = summaryAsString)

                val maxRisk = exposureSummary.maximumRiskScore
                val atWeights = appConfig.attenuationDuration?.weights
                val attenuationDurationInMin =
                    exposureSummary.attenuationDurationsInMinutes
                val attenuationConfig = appConfig.attenuationDuration
                val formulaString =
                    "($maxRisk / ${attenuationConfig?.riskScoreNormalizationDivisor}) * " +
                        "(${attenuationDurationInMin?.get(0)} * ${atWeights?.low} " +
                        "+ ${attenuationDurationInMin?.get(1)} * ${atWeights?.mid} " +
                        "+ ${attenuationDurationInMin?.get(2)} * ${atWeights?.high} " +
                        "+ ${attenuationConfig?.defaultBucketOffset})"

                workState =
                    workState.copy(formula = formulaString, fullConfig = appConfig.toString())

                val token = LocalData.googleApiToken()
                if (token != null) {
                    val exposureInformation = asyncGetExposureInformation(token)

                    var infoString = ""
                    exposureInformation.forEach {
                        infoString += "Attenuation duration in min.: " +
                            "[${it.attenuationDurationsInMinutes?.get(0)}, " +
                            "${it.attenuationDurationsInMinutes?.get(1)}," +
                            "${it.attenuationDurationsInMinutes?.get(2)}]\n" +
                            "Attenuation value: ${it.attenuationValue}\n" +
                            "Duration in min.: ${it.durationMinutes}\n" +
                            "Risk Score: ${it.totalRiskScore}\n" +
                            "Transmission Risk Level: ${it.transmissionRiskLevel}\n" +
                            "Date Millis Since Epoch: ${it.dateMillisSinceEpoch}\n\n"
                    }

                    workState = workState.copy(exposureInfo = infoString)
                }

                riskScoreState.postValue(workState)
            } catch (e: Exception) {
                e.report(ExceptionCategory.EXPOSURENOTIFICATION)
            }
        }
    }

    private suspend fun asyncGetExposureInformation(token: String): List<ExposureInformation> =
        suspendCoroutine { cont ->
            exposureNotificationClient.getExposureInformation(token)
                .addOnSuccessListener {
                    cont.resume(it)
                }.addOnFailureListener {
                    cont.resumeWithException(it)
                }
        }

    data class DiagnosisKeyProvidedEvent(
        val keyCount: Int,
        val token: String
    )

    fun provideDiagnosisKey(transmissionNumber: Int, key: AppleLegacyKeyExchange.Key) {
        val token = UUID.randomUUID().toString()
        LocalData.googleApiToken(token)

        val appleKeyList = mutableListOf<AppleLegacyKeyExchange.Key>()

        AppleLegacyKeyExchange.Key.newBuilder()
            .setKeyData(key.keyData)
            .setRollingPeriod(144)
            .setRollingStartNumber(key.rollingStartNumber)
            .setTransmissionRiskLevel(transmissionNumber)
            .build()
            .also { appleKeyList.add(it) }

        val appleFiles = listOf(
            AppleLegacyKeyExchange.File.newBuilder()
                .addAllKeys(appleKeyList)
                .build()
        )

        val dir = File(File(context.getExternalFilesDir(null), "key-export"), token)
        dir.mkdirs()

        var googleFileList: List<File>
        viewModelScope.launch {
            googleFileList = KeyFileHelper.asyncCreateExportFiles(appleFiles, dir)

            Timber.i("Provide ${googleFileList.count()} files with ${appleKeyList.size} keys with token $token")
            try {
                // only testing implementation: this is used to wait for the broadcastreceiver of the OS / EN API
                InternalExposureNotificationClient.asyncProvideDiagnosisKeys(
                    googleFileList,
                    ApplicationConfigurationService.asyncRetrieveExposureConfiguration(),
                    token
                )
                apiKeysProvidedEvent.postValue(
                    DiagnosisKeyProvidedEvent(
                        keyCount = appleFiles.size,
                        token = token
                    )
                )
            } catch (e: Exception) {
                e.report(ExceptionCategory.EXPOSURENOTIFICATION)
            }
        }
    }

    fun scanLocalQRCodeAndProvide() {
        startLocalQRCodeScanEvent.postValue(Unit)
    }

    fun clearKeyCache() {
        viewModelScope.launch { keyCacheRepository.clear() }
    }

    @AssistedInject.Factory
    interface Factory : CWAViewModelFactory<TestRiskLevelCalculationFragmentCWAViewModel> {
        fun create(
            handle: SavedStateHandle,
            exampleArg: String?
        ): TestRiskLevelCalculationFragmentCWAViewModel
    }
}
