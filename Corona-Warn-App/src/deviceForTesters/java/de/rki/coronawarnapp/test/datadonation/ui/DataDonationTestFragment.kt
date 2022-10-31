package de.rki.coronawarnapp.test.datadonation.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.core.app.ShareCompat
import androidx.core.view.ViewCompat
import androidx.core.view.children
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentTestDatadonationBinding
import de.rki.coronawarnapp.datadonation.safetynet.SafetyNetException
import de.rki.coronawarnapp.datadonation.survey.SurveyException
import de.rki.coronawarnapp.test.menu.ui.TestMenuItem
import de.rki.coronawarnapp.ui.dialog.displayDialog
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.tryHumanReadableError
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import org.json.JSONObject
import javax.inject.Inject

@SuppressLint("SetTextI18n")
class DataDonationTestFragment : Fragment(R.layout.fragment_test_datadonation), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val vm: DataDonationTestFragmentViewModel by cwaViewModels { viewModelFactory }

    private val binding: FragmentTestDatadonationBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vm.currentReport.observe2(this) {
            binding.safetynetBody.text = it?.body?.toString()?.let { json ->
                JSONObject(json).toString(4)
            }
        }

        vm.currentAnalyticsData.observe2(this) {
            binding.analyticsBody.text = it.toString()
        }

        binding.apply {
            safetynetCreateReport.setOnClickListener { vm.createSafetyNetReport() }
            safetynetCopyJws.setOnClickListener { vm.copyJWS() }
            analyticsCollect.setOnClickListener { vm.collectAnalyticsData() }
            analyticsCopy.setOnClickListener { vm.copyAnalytics() }
            analyticsSubmit.setOnClickListener { vm.submitAnalytics() }
        }

        vm.copyJWSEvent.observe2(this) { jws ->
            val intent = ShareCompat.IntentBuilder(requireActivity())
                .setType("text/plain")
                .setSubject("JWS")
                .setText(jws)
                .createChooserIntent()
            startActivity(intent)
        }

        vm.copyAnalyticsEvent.observe2(this) { analytics ->
            val intent = ShareCompat.IntentBuilder(requireActivity())
                .setType("text/plain")
                .setSubject("Analytics")
                .setText(analytics)
                .createChooserIntent()
            startActivity(intent)
        }

        vm.infoEvents.observe2(this) {
            Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
        }

        vm.currentValidation.observe2(this) { items ->
            if (items?.first == null) {
                binding.safetynetRequirementsBody.text = "No validation yet."
                return@observe2
            }
            binding.safetynetRequirementsBody.apply {
                text = items.first.toString()
                append("\n\n")
                if (items.second != null) {
                    append(items.second.toString())
                } else {
                    append("Requirements passed!")
                }
            }
        }

        binding.apply {
            safetynetRequirementsCasually.setOnClickListener { vm.validateSafetyNetCasually() }
            safetynetRequirementsStrict.setOnClickListener { vm.validateSafetyNetStrict() }
        }

        vm.lastAnalyticsData.observe2(this) {
            binding.analyticsLastSubmitBody.text =
                it?.toString() ?: "No analytics were successfully submitted until now"
        }

        vm.checkLastAnalytics()

        vm.otp.observe2(this) {
            binding.oneTimePasswordBody.text = it.toString()
        }

        vm.surveyConfig.observe2(this) {
            binding.surveyConfigBody.text = it
        }

        vm.showErrorDialog.observe2(this) {
            val humanReadableError = it.tryHumanReadableError(requireContext())
            displayDialog {
                title(R.string.datadonation_details_survey_consent_error_dialog_title)
                message(humanReadableError.description)
                positiveButton(R.string.datadonation_details_survey_consent_error_dialog_pos_button)
                setCancelable(false)
            }
        }

        vm.currentSafetyNetExceptionType.observe2(this) { type ->
            binding.apply {
                if (safetynetExceptionSimulationRadioGroup.childCount != SafetyNetException.Type.values().size) {
                    SafetyNetException.Type.values()
                        .forEach { safetynetExceptionSimulationRadioGroup.addRadioButton(it.name) }
                }
                safetynetExceptionSimulationRadioGroup.children.checkByName(type.name)
            }
        }

        binding.apply {
            safetynetExceptionSimulationRadioGroup.setOnCheckedChangeListener { group, checkedId ->
                val rb = group.findViewById(checkedId) as RadioButton
                if (!rb.isPressed) return@setOnCheckedChangeListener
                vm.selectSafetyNetExceptionType(SafetyNetException.Type.valueOf(rb.text as String))
            }

            safetynetExceptionSimulationButton.setOnClickListener { vm.showSafetyNetErrorDialog() }
        }

        vm.currentSurveyExceptionType.observe2(this) { type ->
            binding.apply {
                if (surveyExceptionSimulationRadioGroup.childCount != SurveyException.Type.values().size) {
                    SurveyException.Type.values()
                        .forEach { surveyExceptionSimulationRadioGroup.addRadioButton(it.name) }
                }
                surveyExceptionSimulationRadioGroup.children.checkByName(type.name)
            }
        }

        binding.apply {
            surveyExceptionSimulationRadioGroup.setOnCheckedChangeListener { group, checkedId ->
                val rb = group.findViewById(checkedId) as RadioButton
                if (!rb.isPressed) return@setOnCheckedChangeListener
                vm.selectSurveyExceptionType(SurveyException.Type.valueOf(rb.text as String))
            }

            surveyExceptionSimulationButton.setOnClickListener { vm.showSurveyErrorDialog() }
        }

        vm.isSafetyNetTimeCheckSkipped.observe2(this) {
            binding.disableSafetynetToggle.isChecked = it
        }

        binding.disableSafetynetToggle.setOnClickListener {
            vm.toggleSkipSafetyNetTimeCheck()
        }
    }

    private fun RadioGroup.addRadioButton(text: String) {
        val rb = RadioButton(context).apply {
            this.text = text
            id = ViewCompat.generateViewId()
        }
        addView(rb)
    }

    private fun Sequence<View>.checkByName(name: String) {
        forEach {
            it as RadioButton
            it.isChecked = it.text == name
        }
    }

    companion object {
        val MENU_ITEM = TestMenuItem(
            title = "Data Donation",
            description = "SafetyNet, Analytics, Surveys et al.",
            targetId = R.id.test_datadonation_fragment
        )
    }
}
