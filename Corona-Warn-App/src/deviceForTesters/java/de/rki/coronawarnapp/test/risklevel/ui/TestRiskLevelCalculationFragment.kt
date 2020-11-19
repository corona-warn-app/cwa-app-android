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
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import timber.log.Timber
import javax.inject.Inject

@Suppress("LongMethod")
class TestRiskLevelCalculationFragment : Fragment(R.layout.fragment_test_risk_level_calculation),
    AutoInject {
    private val navArgs by navArgs<TestRiskLevelCalculationFragmentArgs>()

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val vm: TestRiskLevelCalculationFragmentCWAViewModel by cwaViewModelsAssisted(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, handle ->
            factory as TestRiskLevelCalculationFragmentCWAViewModel.Factory
            factory.create(handle, navArgs.exampleArgument)
        }
    )

    private val settingsViewModel: SettingsViewModel by activityViewModels()

    private val binding: FragmentTestRiskLevelCalculationBinding by viewBindingLazy()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vm.tracingCardState.observe2(this) {
            binding.tracingCard = it
        }

        binding.settingsViewModel = settingsViewModel

        vm.showRiskStatusCard.observe2(this) {
            binding.showRiskStatusCard = it
        }

        binding.buttonRetrieveDiagnosisKeys.setOnClickListener { vm.retrieveDiagnosisKeys() }
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
            binding.labelRiskAdditionalInfo.text = state.riskScoreMsg
            binding.labelBackendParameters.text = state.backendParameters
            binding.labelFormula.text = state.formula
        }
        vm.startENFObserver()

        vm.apiKeysProvidedEvent.observe2(this) { event ->
            Toast.makeText(
                requireContext(),
                "Provided ${event.keyCount} keys to Google API",
                Toast.LENGTH_SHORT
            ).show()
        }

        vm.startLocalQRCodeScanEvent.observe2(this) {
            IntentIntegrator.forSupportFragment(this)
                .setOrientationLocked(false)
                .setBeepEnabled(false)
                .initiateScan()
        }

        vm.aggregatedRiskResult.observe2(this) {
            binding.labelAggregatedRiskResult.text = it
        }

        vm.exposureWindowCountString.observe2(this) {
            binding.labelExposureWindowCount.text = it
        }

        vm.exposureWindows.observe2(this) {
            binding.labelExposureWindows.text = it
        }
    }

    companion object {
        val TAG: String = TestRiskLevelCalculationFragment::class.simpleName!!
        val MENU_ITEM = TestMenuItem(
            title = "ENF v2 Calculation",
            description = "Window Mode related overview.",
            targetId = R.id.test_risklevel_calculation_fragment
        )
    }
}
