package de.rki.coronawarnapp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.exposurenotification.ExposureInformation
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import de.rki.coronawarnapp.databinding.FragmentTestRiskLevelCalculationBinding
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.TransactionException
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.nearby.InternalExposureNotificationClient
import de.rki.coronawarnapp.risk.RiskLevel
import de.rki.coronawarnapp.risk.RiskLevelCalculation
import de.rki.coronawarnapp.risk.TimeVariables
import de.rki.coronawarnapp.server.protocols.AppleLegacyKeyExchange
import de.rki.coronawarnapp.service.applicationconfiguration.ApplicationConfigurationService
import de.rki.coronawarnapp.sharing.ExposureSharingService
import de.rki.coronawarnapp.storage.AppDatabase
import de.rki.coronawarnapp.storage.FileStorageHelper
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.storage.RiskLevelRepository
import de.rki.coronawarnapp.transaction.RetrieveDiagnosisKeysTransaction
import de.rki.coronawarnapp.transaction.RiskLevelTransaction
import de.rki.coronawarnapp.ui.viewmodel.SettingsViewModel
import de.rki.coronawarnapp.ui.viewmodel.SubmissionViewModel
import de.rki.coronawarnapp.ui.viewmodel.TracingViewModel
import de.rki.coronawarnapp.util.KeyFileHelper
import de.rki.coronawarnapp.util.security.SecurityHelper
import kotlinx.android.synthetic.deviceForTesters.fragment_test_risk_level_calculation.transmission_number
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

@Suppress("MagicNumber", "LongMethod")
class TestRiskLevelCalculation : Fragment() {
    companion object {
        val TAG: String? = TestRiskLevelCalculation::class.simpleName
    }

    private val tracingViewModel: TracingViewModel by activityViewModels()
    private val settingsViewModel: SettingsViewModel by activityViewModels()
    private val submissionViewModel: SubmissionViewModel by activityViewModels()
    private var _binding: FragmentTestRiskLevelCalculationBinding? = null
    private val binding: FragmentTestRiskLevelCalculationBinding get() = _binding!!

    // reference to the client from the Google framework with the given application context
    private val exposureNotificationClient by lazy {
        Nearby.getExposureNotificationClient(CoronaWarnApplication.getAppContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTestRiskLevelCalculationBinding.inflate(inflater)
        binding.tracingViewModel = tracingViewModel
        binding.settingsViewModel = settingsViewModel
        binding.submissionViewModel = submissionViewModel
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonRetrieveDiagnosisKeys.setOnClickListener {
            tracingViewModel.viewModelScope.launch {
                retrieveDiagnosisKeys()
            }
        }

        binding.buttonProvideKeyViaQr.setOnClickListener {
            scanLocalQRCodeAndProvide()
        }

        binding.buttonCalculateRiskLevel.setOnClickListener {
            tracingViewModel.viewModelScope.launch {
                calculateRiskLevel()
            }
        }

        binding.buttonResetRiskLevel.setOnClickListener {
            tracingViewModel.viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    try {
                        // Database Reset
                        AppDatabase.getInstance(requireContext()).clearAllTables()
                        // Delete Database Instance
                        SecurityHelper.resetSharedPrefs()
                        // Export File Reset
                        FileStorageHelper.getAllFilesInKeyExportDirectory().forEach { it.delete() }

                        LocalData.lastCalculatedRiskLevel(RiskLevel.UNDETERMINED.raw)
                        LocalData.lastSuccessfullyCalculatedRiskLevel(RiskLevel.UNDETERMINED.raw)
                        LocalData.lastTimeDiagnosisKeysFromServerFetch(null)
                        LocalData.googleApiToken(null)
                    } catch (e: java.lang.Exception) {
                        e.report(ExceptionCategory.INTERNAL)
                    }
                }
                RiskLevelTransaction.start()
                Toast.makeText(
                    requireContext(), "Resetted, please fetch diagnosis keys from server again",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        startObserving()
    }

    override fun onResume() {
        super.onResume()
        tracingViewModel.viewModelScope.launch {
            calculateRiskLevel()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        val result: IntentResult? =
            IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                Toast.makeText(requireContext(), "Cancelled", Toast.LENGTH_LONG).show()
            } else {
                ExposureSharingService.getOthersKeys(result.contents, onScannedKey)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private suspend fun retrieveDiagnosisKeys() {
        try {
            RetrieveDiagnosisKeysTransaction.start()
            calculateRiskLevel()
        } catch (e: TransactionException) {
            e.report(ExceptionCategory.INTERNAL)
        }
    }

    private fun scanLocalQRCodeAndProvide() {
        IntentIntegrator.forSupportFragment(this)
            .setOrientationLocked(false)
            .setBeepEnabled(false)
            .initiateScan()
    }

    private val onScannedKey = { key: AppleLegacyKeyExchange.Key? ->
        Timber.i("keys scanned..")
        provideDiagnosisKey(key)
    }

    private fun provideDiagnosisKey(key: AppleLegacyKeyExchange.Key?) {
        if (null == key) {
            Toast.makeText(requireContext(), "No Key data found in QR code", Toast.LENGTH_SHORT)
                .show()
        } else {
            val token = UUID.randomUUID().toString()
            LocalData.googleApiToken(token)

            val appleKeyList = mutableListOf<AppleLegacyKeyExchange.Key>()

            val text = (transmission_number as EditText).text.toString()
            var number = 5
            if (!text.isBlank()) {
                number = Integer.valueOf(text)
            }

            appleKeyList.add(
                AppleLegacyKeyExchange.Key.newBuilder()
                    .setKeyData(key.keyData)
                    .setRollingPeriod(144)
                    .setRollingStartNumber(key.rollingStartNumber)
                    .setTransmissionRiskLevel(number)
                    .build()
            )

            val appleFiles = listOf(
                AppleLegacyKeyExchange.File.newBuilder()
                    .addAllKeys(appleKeyList)
                    .build()
            )

            val dir =
                File(File(requireContext().getExternalFilesDir(null), "key-export"), token)
            dir.mkdirs()

            var googleFileList: List<File>
            lifecycleScope.launch {
                googleFileList = KeyFileHelper.asyncCreateExportFiles(appleFiles, dir)

                Timber.i("Provide ${googleFileList.count()} files with ${appleKeyList.size} keys with token $token")
                try {
                    // only testing implementation: this is used to wait for the broadcastreceiver of the OS / EN API
                    InternalExposureNotificationClient.asyncProvideDiagnosisKeys(
                        googleFileList,
                        ApplicationConfigurationService.asyncRetrieveExposureConfiguration(),
                        token
                    )
                    Toast.makeText(
                        requireContext(),
                        "Provided ${appleKeyList.size} keys to Google API with token $token",
                        Toast.LENGTH_SHORT
                    ).show()
                } catch (e: Exception) {
                    e.report(ExceptionCategory.EXPOSURENOTIFICATION)
                }
            }
        }
    }

    private suspend fun calculateRiskLevel() {
        try {
            RiskLevelTransaction.start()
        } catch (e: TransactionException) {
            e.report(ExceptionCategory.INTERNAL)
        }
    }

    private fun startObserving() {
        tracingViewModel.viewModelScope.launch {
            try {
                val googleToken = LocalData.googleApiToken() ?: UUID.randomUUID().toString()
                val exposureSummary =
                    InternalExposureNotificationClient.asyncGetExposureSummary(googleToken)

                val appConfig =
                    ApplicationConfigurationService.asyncRetrieveApplicationConfiguration()

                val riskLevelScore = RiskLevelCalculation.calculateRiskScore(
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

                binding.labelRiskScore.text = riskAsString

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

                binding.labelBackendParameters.text = configAsString

                val summaryAsString =
                    "Days Since Last Exposure: ${exposureSummary.daysSinceLastExposure}\n" +
                            "Matched Key Count: ${exposureSummary.matchedKeyCount}\n" +
                            "Maximum Risk Score: ${exposureSummary.maximumRiskScore}\n" +
                            "Attenuation Durations: [${exposureSummary.attenuationDurationsInMinutes?.get(
                                0
                            )}," +
                            "${exposureSummary.attenuationDurationsInMinutes?.get(1)}," +
                            "${exposureSummary.attenuationDurationsInMinutes?.get(2)}]\n" +
                            "Summation Risk Score: ${exposureSummary.summationRiskScore}"

                binding.labelExposureSummary.text = summaryAsString

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

                binding.labelFormula.text = formulaString

                binding.labelFullConfig.text = appConfig.toString()

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

                    binding.labelExposureInfo.text = infoString
                }
            } catch (e: Exception) {
                e.report(ExceptionCategory.EXPOSURENOTIFICATION)
            }
        }
    }

    suspend fun asyncGetExposureInformation(token: String): List<ExposureInformation> =
        suspendCoroutine { cont ->
            exposureNotificationClient.getExposureInformation(token)
                .addOnSuccessListener {
                    cont.resume(it)
                }.addOnFailureListener {
                    cont.resumeWithException(it)
                }
        }
}
