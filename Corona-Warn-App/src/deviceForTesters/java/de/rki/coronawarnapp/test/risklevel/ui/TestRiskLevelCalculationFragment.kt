package de.rki.coronawarnapp.test.risklevel.ui

import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentTestRiskLevelCalculationBinding
import de.rki.coronawarnapp.storage.TestSettings
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

        vm.exposureWindowCountString.observe2(this) {
            binding.labelExposureWindowCount.text = it
        }

        vm.exposureWindows.observe2(this) {
            binding.labelExposureWindows.text = it
        }

        vm.backendParameters.observe2(this) {
            binding.labelBackendParameters.text = it
        }

        vm.fakeWindowsState.observe2(this) { currentType ->
            binding.apply {
                if (fakeWindowsToggleGroup.childCount != TestSettings.FakeExposureWindowTypes.values().size) {
                    fakeWindowsToggleGroup.removeAllViews()
                    TestSettings.FakeExposureWindowTypes.values().forEach { type ->
                        RadioButton(requireContext()).apply {
                            id = ViewCompat.generateViewId()
                            text = type.name
                            layoutParams = RadioGroup.LayoutParams(
                                RadioGroup.LayoutParams.MATCH_PARENT,
                                RadioGroup.LayoutParams.WRAP_CONTENT
                            )
                            fakeWindowsToggleGroup.addView(this)
                        }
                    }
                }

                fakeWindowsToggleGroup.children.forEach {
                    it as RadioButton
                    it.isChecked = it.text == currentType.name
                }
            }
        }
        binding.fakeWindowsToggleGroup.apply {
            setOnCheckedChangeListener { group, checkedId ->
                val chip = group.findViewById<RadioButton>(checkedId)
                if (!chip.isPressed) return@setOnCheckedChangeListener
                vm.selectFakeExposureWindowMode(TestSettings.FakeExposureWindowTypes.valueOf(chip.text.toString()))
            }
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
