package de.rki.coronawarnapp.test.risklevel.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.app.ShareCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentTestRiskLevelCalculationBinding
import de.rki.coronawarnapp.test.menu.ui.TestMenuItem
import de.rki.coronawarnapp.ui.viewmodel.SettingsViewModel
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
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

        vm.additionalRiskCalcInfo.observe2(this) {
            binding.labelRiskAdditionalInfo.text = it
        }

        vm.aggregatedRiskResult.observe2(this) {
            binding.labelAggregatedRiskResult.text = it
        }

        vm.backendParameters.observe2(this) {
            binding.labelBackendParameters.text = it
        }

        vm.exposureWindowCountString.observe2(this) {
            binding.labelExposureWindowCount.text = it
        }

        vm.exposureWindows.observe2(this) {
            binding.labelExposureWindows.text = it
        }

        binding.buttonExposureWindowsShare.setOnClickListener { shareExposureWindows() }
    }

    private fun shareExposureWindows() {
        activity?.let { activity ->
            val shareIntent = ShareCompat.IntentBuilder
                .from(activity)
                .setType("text/plain")
                .setText(binding.labelExposureWindows.text)
                .createChooserIntent()

            if (shareIntent.resolveActivity(activity.packageManager) != null) {
                startActivity(shareIntent)
            }
        }
    }

    companion object {
        private val TAG = TestRiskLevelCalculationFragment::class.java.simpleName
        val MENU_ITEM = TestMenuItem(
            title = "ENF v2 Calculation",
            description = "Window Mode related overview.",
            targetId = R.id.test_risklevel_calculation_fragment
        )
    }
}
