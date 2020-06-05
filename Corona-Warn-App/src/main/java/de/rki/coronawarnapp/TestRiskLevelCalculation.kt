package de.rki.coronawarnapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.google.android.gms.nearby.exposurenotification.ExposureSummary
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import de.rki.coronawarnapp.databinding.FragmentTestRiskLevelCalculationBinding
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.TransactionException
import de.rki.coronawarnapp.exception.report
import de.rki.coronawarnapp.nearby.InternalExposureNotificationClient
import de.rki.coronawarnapp.risk.RiskLevel
import de.rki.coronawarnapp.risk.TimeVariables
import de.rki.coronawarnapp.server.protocols.AppleLegacyKeyExchange
import de.rki.coronawarnapp.server.protocols.ApplicationConfigurationOuterClass
import de.rki.coronawarnapp.service.applicationconfiguration.ApplicationConfigurationService
import de.rki.coronawarnapp.sharing.ExposureSharingService
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.transaction.RetrieveDiagnosisKeysTransaction
import de.rki.coronawarnapp.transaction.RiskLevelTransaction
import de.rki.coronawarnapp.ui.viewmodel.SettingsViewModel
import de.rki.coronawarnapp.ui.viewmodel.SubmissionViewModel
import de.rki.coronawarnapp.ui.viewmodel.TracingViewModel
import de.rki.coronawarnapp.util.KeyFileHelper
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID
import java.util.concurrent.TimeUnit

@Suppress("MagicNumber")
class TestRiskLevelCalculation : Fragment() {
    companion object {
        val TAG: String? = TestRiskLevelCalculation::class.simpleName
    }

    data class TransactionValues(
        var appConfig: ApplicationConfigurationOuterClass.ApplicationConfiguration? = null,
        var exposureSummary: ExposureSummary? = null,
        var riskScore: Double? = null,
        var riskLevel: RiskLevel? = null
    )

    private val tracingViewModel: TracingViewModel by activityViewModels()
    private val settingsViewModel: SettingsViewModel by activityViewModels()
    private val submissionViewModel: SubmissionViewModel by activityViewModels()
    private var _binding: FragmentTestRiskLevelCalculationBinding? = null
    private val binding: FragmentTestRiskLevelCalculationBinding get() = _binding!!

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

        startObserving()
    }

    override fun onResume() {
        super.onResume()
        tracingViewModel.viewModelScope.launch {
            RiskLevelTransaction.recordedTransactionValuesForTestingOnly = TransactionValues()
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
        Log.i(TestForAPIFragment.TAG, "keys scanned..")
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

            appleKeyList.add(
                AppleLegacyKeyExchange.Key.newBuilder()
                    .setKeyData(key.keyData)
                    .setRollingPeriod(144)
                    .setRollingStartNumber(key.rollingStartNumber)
                    .setTransmissionRiskLevel(1)
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

                Log.i(
                    TAG,
                    "Provide ${googleFileList.count()} files with ${appleKeyList.size} keys with token $token"
                )
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
        RiskLevelTransaction.tempExposedTransactionValuesForTestingOnly.observe(
            viewLifecycleOwner,
            Observer {
                tracingViewModel.viewModelScope.launch {
                    val riskAsString = "Level: ${it.riskLevel}\n" +
                            "Last successful Level: " +
                            "${LocalData.lastSuccessfullyCalculatedRiskLevel()}\n" +
                            "Calculated Score: ${it.riskScore}\n" +
                            "Last Time Server Fetch: ${LocalData.lastTimeDiagnosisKeysFromServerFetch()}\n" +
                            "Tracing Duration: " +
                            "${TimeUnit.MILLISECONDS.toDays(TimeVariables.getTimeActiveTracingDuration())} days \n" +
                            "Tracing Duration in last 14 days: " +
                            "${TimeVariables.getActiveTracingDaysInRetentionPeriod()} days"
                    binding.labelRiskScore.text = riskAsString

                    val lowClass =
                        it.appConfig?.riskScoreClasses?.riskClassesList?.find { low -> low.label == "LOW" }
                    val highClass =
                        it.appConfig?.riskScoreClasses?.riskClassesList?.find { high -> high.label == "HIGH" }

                    val configAsString =
                        "Attenuation Weight Low: ${it.appConfig?.attenuationDuration?.weights?.low}\n" +
                                "Attenuation Weight Mid: ${it.appConfig?.attenuationDuration?.weights?.mid}\n" +
                                "Attenuation Weight High: ${it.appConfig?.attenuationDuration?.weights?.high}\n\n" +
                                "Attenuation Offset: ${it.appConfig?.attenuationDuration?.defaultBucketOffset}\n" +
                                "Attenuation Normalization: " +
                                "${it.appConfig?.attenuationDuration?.riskScoreNormalizationDivisor}\n\n" +
                                "Risk Score Low Class: ${lowClass?.min ?: 0} - ${lowClass?.max ?: 0}\n" +
                                "Risk Score High Class: ${highClass?.min ?: 0} - ${highClass?.max ?: 0}"

                    binding.labelBackendParameters.text = configAsString

                    val summaryAsString =
                        "Days Since Last Exposure: ${it.exposureSummary?.daysSinceLastExposure}\n" +
                                "Matched Key Count: ${it.exposureSummary?.matchedKeyCount}\n" +
                                "Maximum Risk Score: ${it.exposureSummary?.maximumRiskScore}\n" +
                                "Attenuation Durations: [${it.exposureSummary?.attenuationDurationsInMinutes?.get(
                                    0
                                )}," +
                                "${it.exposureSummary?.attenuationDurationsInMinutes?.get(1)}," +
                                "${it.exposureSummary?.attenuationDurationsInMinutes?.get(2)}]\n" +
                                "Summation Risk Score: ${it.exposureSummary?.summationRiskScore}"

                    binding.labelExposureSummary.text = summaryAsString

                    val maxRisk = it.exposureSummary?.maximumRiskScore
                    val atWeights = it.appConfig?.attenuationDuration?.weights
                    val attenuationDurationInMin =
                        it.exposureSummary?.attenuationDurationsInMinutes
                    val attenuationConfig = it.appConfig?.attenuationDuration
                    val formulaString =
                        "($maxRisk / ${attenuationConfig?.riskScoreNormalizationDivisor}) * " +
                                "(${attenuationDurationInMin?.get(0)} * ${atWeights?.low} " +
                                "+ ${attenuationDurationInMin?.get(1)} * ${atWeights?.mid} " +
                                "+ ${attenuationDurationInMin?.get(2)} * ${atWeights?.high} " +
                                "+ ${attenuationConfig?.defaultBucketOffset})"

                    binding.labelFormula.text = formulaString

                    binding.labelFullConfig.text = it.appConfig?.toString()
                }
            })
    }
}
