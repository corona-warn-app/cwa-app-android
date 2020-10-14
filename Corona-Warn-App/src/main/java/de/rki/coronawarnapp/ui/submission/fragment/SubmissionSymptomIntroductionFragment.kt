package de.rki.coronawarnapp.ui.submission.fragment

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSubmissionSymptomIntroBinding
import de.rki.coronawarnapp.submission.Symptoms
import de.rki.coronawarnapp.ui.doNavigate
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionNavigationEvents
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionSymptomIntroductionViewModel
import de.rki.coronawarnapp.ui.viewmodel.SubmissionViewModel
import de.rki.coronawarnapp.util.DialogHelper
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.formatter.formatBackgroundButtonStyleByState
import de.rki.coronawarnapp.util.formatter.formatButtonStyleByState
import de.rki.coronawarnapp.util.formatter.isEnableSymptomIntroButtonByState
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

class SubmissionSymptomIntroductionFragment : Fragment(), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: SubmissionSymptomIntroductionViewModel by cwaViewModels { viewModelFactory }
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

        viewModel.routeToScreen.observe2(this) {
            when (it) {
                is SubmissionNavigationEvents.NavigateToSymptomCalendar -> navigateToNext()
                is SubmissionNavigationEvents.NavigateToResultPositiveOtherWarning -> handleSubmissionCancellation()
            }
        }

        submissionViewModel.symptomIndication.observe(viewLifecycleOwner, {
            updateButtons(it)
        })

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backCallback)

        submissionViewModel.initSymptoms()
    }

    private val backCallback: OnBackPressedCallback =
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                viewModel.onPreviousClicked()
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

    /**
     * Opens a Dialog that warns user
     * when they're about to cancel the submission flow
     * @see DialogHelper
     * @see navigateToPreviousScreen
     */
    private fun handleSubmissionCancellation() {
        DialogHelper.showDialog(
            DialogHelper.DialogInstance(
                requireActivity(),
                R.string.submission_error_dialog_confirm_cancellation_title,
                R.string.submission_error_dialog_confirm_cancellation_body,
                R.string.submission_error_dialog_confirm_cancellation_button_positive,
                R.string.submission_error_dialog_confirm_cancellation_button_negative,
                true,
                ::navigateToPreviousScreen
            )
        )
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
            .setOnClickListener { viewModel.onPreviousClicked() }

        binding
            .symptomButtonNext
            .setOnClickListener { viewModel.onNextClicked() }

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
