package de.rki.coronawarnapp.test.risklevel.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentTestRiskLevelCalculationBinding
import de.rki.coronawarnapp.storage.TestSettings
import de.rki.coronawarnapp.test.menu.ui.TestMenuItem
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import timber.log.Timber
import java.io.File
import javax.inject.Inject

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

    private val binding: FragmentTestRiskLevelCalculationBinding by viewBindingLazy()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonRetrieveDiagnosisKeys.setOnClickListener { vm.retrieveDiagnosisKeys() }
        binding.buttonCalculateRiskLevel.setOnClickListener { vm.calculateRiskLevel() }
        binding.buttonClearDiagnosisKeyCache.setOnClickListener { vm.clearKeyCache() }
        binding.buttonResetRiskLevel.setOnClickListener { vm.resetRiskLevel() }
        binding.buttonExposureWindowsShare.setOnClickListener { vm.shareExposureWindows() }

        vm.dataResetEvent.observe2(this) { Snackbar.make(requireView(), it, Snackbar.LENGTH_SHORT).show() }

        vm.additionalRiskCalcInfo.observe2(this) {
            binding.labelRiskAdditionalInfo.text = it
        }
        vm.aggregatedRiskResult.observe2(this) {
            binding.labelAggregatedRiskResult.text = it
        }
        vm.backendParameters.observe2(this) {
            binding.labelBackendParameters.text = it
        }
        vm.exposureWindowCount.observe2(this) { exposureWindowCount ->
            binding.labelExposureWindowCount.text = "Retrieved $exposureWindowCount Exposure Windows"
            binding.buttonExposureWindowsShare.visibility = when (exposureWindowCount > 0) {
                true -> View.VISIBLE
                false -> View.GONE
            }
        }
        vm.shareFileEvent.observe2(this) {
            shareExposureWindowsFile(it)
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

    private fun shareExposureWindowsFile(file: File) {
        Timber.d("Opening Share-Intent for Exposure Windows")
        val shareFileUri =
            FileProvider.getUriForFile(requireContext(), requireContext().packageName + ".fileProvider", file)
        val shareIntent = ShareCompat.IntentBuilder
            .from(requireActivity())
            .setStream(shareFileUri)
            .setType("text/plain")
            .intent
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        if (shareIntent.resolveActivity(requireActivity().packageManager) != null) {
            startActivity(shareIntent)
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
