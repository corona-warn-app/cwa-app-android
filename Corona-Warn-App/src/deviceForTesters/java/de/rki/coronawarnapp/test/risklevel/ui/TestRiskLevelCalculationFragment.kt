package de.rki.coronawarnapp.test.risklevel.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentTestRiskLevelCalculationBinding
import de.rki.coronawarnapp.server.protocols.AppleLegacyKeyExchange
import de.rki.coronawarnapp.sharing.ExposureSharingService
import de.rki.coronawarnapp.test.menu.ui.TestMenuItem
import de.rki.coronawarnapp.ui.viewmodel.SettingsViewModel
import de.rki.coronawarnapp.ui.viewmodel.SubmissionViewModel
import de.rki.coronawarnapp.ui.viewmodel.TracingViewModel
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import timber.log.Timber
import javax.inject.Inject

@Suppress("MagicNumber", "LongMethod")
class TestRiskLevelCalculationFragment : Fragment(R.layout.fragment_test_risk_level_calculation),
    AutoInject {
    private val navArgs by navArgs<TestRiskLevelCalculationFragmentArgs>()

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val vm: TestRiskLevelCalculationFragmentCWAViewModel by cwaViewModelsAssisted(
        { viewModelFactory },
        { factory, handle ->
            factory as TestRiskLevelCalculationFragmentCWAViewModel.Factory
            factory.create(handle, navArgs.exampleArgument)
        }
    )

    private val tracingViewModel: TracingViewModel by activityViewModels()
    private val settingsViewModel: SettingsViewModel by activityViewModels()
    private val submissionViewModel: SubmissionViewModel by activityViewModels()

    private val binding: FragmentTestRiskLevelCalculationBinding by viewBindingLazy()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vm.tracingCardState.observe2(this) {
            binding.tracingCard = it
        }

        binding.tracingViewModel = tracingViewModel
        binding.settingsViewModel = settingsViewModel
        binding.submissionViewModel = submissionViewModel

        binding.buttonRetrieveDiagnosisKeys.setOnClickListener { vm.retrieveDiagnosisKeys() }
        binding.buttonProvideKeyViaQr.setOnClickListener { vm.scanLocalQRCodeAndProvide() }
        binding.buttonCalculateRiskLevel.setOnClickListener { vm.calculateRiskLevel() }
        binding.buttonClearDiagnosisKeyCache.setOnClickListener { vm.clearKeyCache() }

        binding.buttonResetRiskLevel.setOnClickListener { vm.resetRiskLevel() }
        vm.riskLevelResetEvent.observe2(this) {
            Toast.makeText(
                requireContext(), "Reset done, please fetch diagnosis keys from server again",
                Toast.LENGTH_SHORT
            ).show()
        }

        vm.riskScoreState.observe2(this) { state ->
            binding.labelRiskScore.text = state.riskScoreMsg
            binding.labelBackendParameters.text = state.backendParameters
            binding.labelExposureSummary.text = state.exposureSummary
            binding.labelFormula.text = state.formula
            binding.labelFullConfig.text = state.fullConfig
            binding.labelExposureInfo.text = state.exposureInfo
        }
        vm.startENFObserver()

        vm.apiKeysProvidedEvent.observe2(this) { event ->
            Toast.makeText(
                requireContext(),
                "Provided ${event.keyCount} keys to Google API with token ${event.token}",
                Toast.LENGTH_SHORT
            ).show()
        }

        vm.startLocalQRCodeScanEvent.observe2(this) {
            IntentIntegrator.forSupportFragment(this)
                .setOrientationLocked(false)
                .setBeepEnabled(false)
                .initiateScan()
        }
    }

    override fun onResume() {
        super.onResume()
        vm.calculateRiskLevel()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result: IntentResult =
            IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
                ?: return super.onActivityResult(requestCode, resultCode, data)

        if (result.contents == null) {
            Toast.makeText(requireContext(), "Cancelled", Toast.LENGTH_LONG).show()
            return
        }

        ExposureSharingService.getOthersKeys(result.contents) { key: AppleLegacyKeyExchange.Key? ->
            Timber.i("Keys scanned: %s", key)
            if (key == null) {
                Toast.makeText(
                    requireContext(), "No Key data found in QR code", Toast.LENGTH_SHORT
                ).show()
                return@getOthersKeys Unit
            }

            val text = binding.transmissionNumber.text.toString()
            val number = if (!text.isBlank()) Integer.valueOf(text) else 5
            vm.provideDiagnosisKey(number, key)
        }
    }

    companion object {
        val TAG: String = TestRiskLevelCalculationFragment::class.simpleName!!
        val MENU_ITEM = TestMenuItem(
            title = "Risklevel Calculation",
            description = "Risklevel calculation related test options.",
            targetId = R.id.test_risklevel_calculation_fragment
        )
    }
}
