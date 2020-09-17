package de.rki.coronawarnapp.ui.submission

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSubmissionSymptomIntroBinding
import de.rki.coronawarnapp.submission.Symptoms
import de.rki.coronawarnapp.ui.doNavigate
import de.rki.coronawarnapp.ui.viewmodel.SubmissionViewModel
import de.rki.coronawarnapp.util.formatter.formatBackgroundButtonStyleByState
import de.rki.coronawarnapp.util.formatter.formatButtonStyleByState
import de.rki.coronawarnapp.util.formatter.isEnableSymptomIntroButtonByState

class SubmissionSymptomIntroductionFragment : Fragment() {

    private var _binding: FragmentSubmissionSymptomIntroBinding? = null
    private val binding: FragmentSubmissionSymptomIntroBinding get() = _binding!!
    private val submissionViewModel: SubmissionViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSubmissionSymptomIntroBinding.inflate(inflater)
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
        setButtonOnClickListener()

        submissionViewModel.symptomIntroductionEvent.observe(viewLifecycleOwner, Observer {
            when (it) {
                is SymptomIntroductionEvent.NavigateToSymptomCalendar -> navigateToNext()
                is SymptomIntroductionEvent.NavigateToPreviousScreen -> navigateToPreviousScreen()
            }
        })

        submissionViewModel.symptomIndication.observe(viewLifecycleOwner, Observer {
            updateButtons(it)
        })

        submissionViewModel.initSymptoms()
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

    private fun navigateToNext() {

        if (submissionViewModel.symptomIndication.value!! == Symptoms.Indication.POSITIVE) {
            findNavController().doNavigate(
                SubmissionSymptomIntroductionFragmentDirections
                    .actionSubmissionSymptomIntroductionFragmentToSubmissionSymptomCalendarFragment()
            )
        } else {
            findNavController().doNavigate(
                SubmissionSymptomIntroductionFragmentDirections
                    .actionSubmissionSymptomIntroductionFragmentToSubmissionResultPositiveOtherWarningFragment()
            )
        }
    }

    private fun navigateToPreviousScreen() {
        findNavController().doNavigate(
            SubmissionSymptomIntroductionFragmentDirections
                .actionSubmissionSymptomIntroductionFragmentToSubmissionResultFragment()
        )
    }

    private fun setButtonOnClickListener() {
        binding
            .submissionSymptomHeader.headerButtonBack.buttonIcon
            .setOnClickListener { submissionViewModel.onPreviousClicked() }

        binding
            .symptomButtonNext
            .setOnClickListener { submissionViewModel.onNextClicked() }

        binding
            .symptomChoiceSelection.targetButtonApply
            .setOnClickListener { submissionViewModel.onPositiveSymptomIndication() }

        binding
            .symptomChoiceSelection.targetButtonReject
            .setOnClickListener { submissionViewModel.onNegativeSymptomIndication() }

        binding
            .symptomChoiceSelection.targetButtonVerify
            .setOnClickListener { submissionViewModel.onNoInformationSymptomIndication() }
    }
}
