package de.rki.coronawarnapp.ui.submission.fragment

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
import de.rki.coronawarnapp.databinding.FragmentSubmissionSymptomCalendarBinding
import de.rki.coronawarnapp.submission.Symptoms
import de.rki.coronawarnapp.ui.doNavigate
import de.rki.coronawarnapp.ui.submission.SubmissionSymptomCalendarFragmentDirections
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionNavigationEvents
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionSymptomCalendarViewModel
import de.rki.coronawarnapp.ui.viewmodel.SubmissionViewModel
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.formatter.formatCalendarBackgroundButtonStyleByState
import de.rki.coronawarnapp.util.formatter.formatCalendarButtonStyleByState
import de.rki.coronawarnapp.util.formatter.isEnableSymptomCalendarButtonByState
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

class SubmissionSymptomCalendarFragment : Fragment(), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: SubmissionSymptomCalendarViewModel by cwaViewModels { viewModelFactory }
    private var _binding: FragmentSubmissionSymptomCalendarBinding? = null
    private val binding: FragmentSubmissionSymptomCalendarBinding get() = _binding!!
    private val submissionViewModel: SubmissionViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSubmissionSymptomCalendarBinding.inflate(inflater)
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

        binding.symptomCalendarContainer.setDateSelectedListener(submissionViewModel::onDateSelected)

        viewModel.routeToScreen.observe(viewLifecycleOwner, Observer {
            when (it) {
                is SubmissionNavigationEvents.NavigateToResultPositiveOtherWarning -> navigateToSymptomFinish()
                is SubmissionNavigationEvents.NavigateToSymptomIntroduction -> navigateToPreviousScreen()
            }
        })

        submissionViewModel.symptomStart.observe(viewLifecycleOwner, Observer {
            updateButtons(it)
            if (it !is Symptoms.StartOf.Date) {
                binding.symptomCalendarContainer.unsetSelection()
            }
        })

        submissionViewModel.initSymptomStart()
    }

    private fun updateButtons(symptomStart: Symptoms.StartOf?) {
        binding.symptomCalendarChoiceSelection.calendarButtonSevenDays
            .findViewById<Button>(R.id.calendar_button_seven_days)
            .setTextColor(formatCalendarButtonStyleByState(symptomStart, Symptoms.StartOf.LastSevenDays))
        binding.symptomCalendarChoiceSelection.targetLayout
            .findViewById<Button>(R.id.calendar_button_seven_days).backgroundTintList =
            ColorStateList.valueOf(
                formatCalendarBackgroundButtonStyleByState(
                    symptomStart, Symptoms.StartOf.LastSevenDays
                )
            )

        binding.symptomCalendarChoiceSelection.targetLayout
            .findViewById<Button>(R.id.calendar_button_one_two_weeks)
            .setTextColor(formatCalendarButtonStyleByState(symptomStart, Symptoms.StartOf.OneToTwoWeeksAgo))
        binding.symptomCalendarChoiceSelection.targetLayout
            .findViewById<Button>(R.id.calendar_button_one_two_weeks).backgroundTintList =
            ColorStateList.valueOf(
                formatCalendarBackgroundButtonStyleByState(
                    symptomStart, Symptoms.StartOf.OneToTwoWeeksAgo
                )
            )

        binding.symptomCalendarChoiceSelection.targetLayout
            .findViewById<Button>(R.id.calendar_button_more_than_two_weeks)
            .setTextColor(formatCalendarButtonStyleByState(symptomStart, Symptoms.StartOf.MoreThanTwoWeeks))
        binding.symptomCalendarChoiceSelection.targetLayout
            .findViewById<Button>(R.id.calendar_button_more_than_two_weeks).backgroundTintList =
            ColorStateList.valueOf(
                formatCalendarBackgroundButtonStyleByState(
                    symptomStart, Symptoms.StartOf.MoreThanTwoWeeks
                )
            )

        binding.symptomCalendarChoiceSelection.targetLayout
            .findViewById<Button>(R.id.target_button_verify)
            .setTextColor(formatCalendarButtonStyleByState(symptomStart, Symptoms.StartOf.NoInformation))
        binding.symptomCalendarChoiceSelection.targetLayout
            .findViewById<Button>(R.id.target_button_verify).backgroundTintList =
            ColorStateList.valueOf(
                formatCalendarBackgroundButtonStyleByState(
                    symptomStart, Symptoms.StartOf.NoInformation
                )
            )

        binding
            .symptomButtonNext.findViewById<Button>(R.id.symptom_button_next).isEnabled =
            isEnableSymptomCalendarButtonByState(
                symptomStart
            )
    }

    private fun navigateToSymptomFinish() {
        findNavController().doNavigate(SubmissionSymptomCalendarFragmentDirections
            .actionSubmissionSymptomCalendarFragmentToSubmissionResultPositiveOtherWarningFragment())
    }

    private fun navigateToPreviousScreen() {
        findNavController().doNavigate(SubmissionSymptomCalendarFragmentDirections
            .actionSubmissionCalendarFragmentToSubmissionSymptomIntroductionFragment())
    }

    private fun setButtonOnClickListener() {
        binding
            .submissionSymptomCalendarHeader.headerButtonBack.buttonIcon
            .setOnClickListener { viewModel.onCalendarPreviousClicked() }

        binding
            .symptomButtonNext
            .setOnClickListener { viewModel.onCalendarNextClicked() }

        binding.symptomCalendarChoiceSelection
            .calendarButtonSevenDays
            .setOnClickListener { submissionViewModel.onLastSevenDaysStart() }

        binding.symptomCalendarChoiceSelection
            .calendarButtonOneTwoWeeks
            .setOnClickListener { submissionViewModel.onOneToTwoWeeksAgoStart() }

        binding.symptomCalendarChoiceSelection
            .calendarButtonMoreThanTwoWeeks
            .setOnClickListener { submissionViewModel.onMoreThanTwoWeeksStart() }

        binding.symptomCalendarChoiceSelection
            .targetButtonVerify
            .setOnClickListener { submissionViewModel.onNoInformationStart() }
    }
}
