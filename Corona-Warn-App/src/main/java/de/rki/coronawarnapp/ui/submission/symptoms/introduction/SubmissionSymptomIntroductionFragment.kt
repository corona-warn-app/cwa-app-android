package de.rki.coronawarnapp.ui.submission.symptoms.introduction

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSubmissionSymptomIntroBinding
import de.rki.coronawarnapp.submission.Symptoms
import de.rki.coronawarnapp.ui.submission.SubmissionBlockingDialog
import de.rki.coronawarnapp.ui.submission.SubmissionCancelDialog
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionNavigationEvents
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.formatter.formatBackgroundButtonStyleByState
import de.rki.coronawarnapp.util.formatter.formatButtonStyleByState
import de.rki.coronawarnapp.util.formatter.isEnableSymptomIntroButtonByState
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

class SubmissionSymptomIntroductionFragment : Fragment(R.layout.fragment_submission_symptom_intro),
    AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: SubmissionSymptomIntroductionViewModel by cwaViewModels { viewModelFactory }

    private val binding: FragmentSubmissionSymptomIntroBinding by viewBindingLazy()
    private lateinit var uploadDialog: SubmissionBlockingDialog

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        uploadDialog = SubmissionBlockingDialog(requireContext())

        viewModel.routeToScreen.observe2(this) {
            when (it) {
                is SubmissionNavigationEvents.NavigateToSymptomCalendar -> doNavigate(
                    SubmissionSymptomIntroductionFragmentDirections
                        .actionSubmissionSymptomIntroductionFragmentToSubmissionSymptomCalendarFragment()
                )
                is SubmissionNavigationEvents.NavigateToResultPositiveOtherWarning -> doNavigate(
                    SubmissionSymptomIntroductionFragmentDirections
                        .actionSubmissionSymptomIntroductionFragmentToSubmissionResultPositiveOtherWarningFragment()
                )
                is SubmissionNavigationEvents.NavigateToTestResult -> doNavigate(
                    SubmissionSymptomIntroductionFragmentDirections
                        .actionSubmissionSymptomIntroductionFragmentToSubmissionResultFragment()
                )
                is SubmissionNavigationEvents.NavigateToMainActivity -> doNavigate(
                    SubmissionSymptomIntroductionFragmentDirections
                        .actionSubmissionSymptomIntroductionFragmentToMainFragment()
                )
            }
        }

        viewModel.showCancelDialog.observe2(this) {
            SubmissionCancelDialog(requireContext()).show {
                viewModel.onCancelConfirmed()
            }
        }

        viewModel.showUploadDialog.observe2(this) {
            uploadDialog.setState(show = it)
        }

        viewModel.symptomIndication.observe2(this) {
            updateButtons(it)
        }

        val backCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                viewModel.onPreviousClicked()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backCallback)

        binding.apply {
            submissionSymptomHeader.headerButtonBack.buttonIcon
                .setOnClickListener { viewModel.onPreviousClicked() }

            symptomButtonNext
                .setOnClickListener { viewModel.onNextClicked() }

            symptomChoiceSelection.targetButtonApply
                .setOnClickListener { viewModel.onPositiveSymptomIndication() }

            symptomChoiceSelection.targetButtonReject
                .setOnClickListener { viewModel.onNegativeSymptomIndication() }

            symptomChoiceSelection.targetButtonVerify
                .setOnClickListener { viewModel.onNoInformationSymptomIndication() }
        }
    }

    private fun updateButtons(symptomIndication: Symptoms.Indication?) {
        binding.submissionSymptomContainer.findViewById<Button>(R.id.target_button_apply)
            .setTextColor(formatButtonStyleByState(symptomIndication, Symptoms.Indication.POSITIVE))
        binding.submissionSymptomContainer.findViewById<Button>(R.id.target_button_apply).backgroundTintList =
            ColorStateList.valueOf(
                formatBackgroundButtonStyleByState(
                    symptomIndication,
                    Symptoms.Indication.POSITIVE
                )
            )
        binding.submissionSymptomContainer.findViewById<Button>(R.id.target_button_reject)
            .setTextColor(formatButtonStyleByState(symptomIndication, Symptoms.Indication.NEGATIVE))
        binding.submissionSymptomContainer.findViewById<Button>(R.id.target_button_reject).backgroundTintList =
            ColorStateList.valueOf(
                formatBackgroundButtonStyleByState(
                    symptomIndication,
                    Symptoms.Indication.NEGATIVE
                )
            )
        binding.submissionSymptomContainer.findViewById<Button>(R.id.target_button_verify)
            .setTextColor(
                formatButtonStyleByState(
                    symptomIndication,
                    Symptoms.Indication.NO_INFORMATION
                )
            )
        binding.submissionSymptomContainer.findViewById<Button>(R.id.target_button_verify).backgroundTintList =
            ColorStateList.valueOf(
                formatBackgroundButtonStyleByState(
                    symptomIndication,
                    Symptoms.Indication.NO_INFORMATION
                )
            )
        binding
            .symptomButtonNext.findViewById<Button>(R.id.symptom_button_next).isEnabled =
            isEnableSymptomIntroButtonByState(
                symptomIndication
            )
    }
}
