package de.rki.coronawarnapp.ui.submission.symptoms.calendar

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSubmissionSymptomCalendarBinding
import de.rki.coronawarnapp.submission.Symptoms
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionNavigationEvents
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.formatter.formatCalendarBackgroundButtonStyleByState
import de.rki.coronawarnapp.util.formatter.formatCalendarButtonStyleByState
import de.rki.coronawarnapp.util.formatter.isEnableSymptomCalendarButtonByState
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import javax.inject.Inject

class SubmissionSymptomCalendarFragment : Fragment(R.layout.fragment_submission_symptom_calendar),
    AutoInject {

    private val navArgs by navArgs<SubmissionSymptomCalendarFragmentArgs>()

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: SubmissionSymptomCalendarViewModel by cwaViewModelsAssisted(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, _ ->
            factory as SubmissionSymptomCalendarViewModel.Factory
            factory.create(navArgs.symptomIndication)
        }
    )

    private val binding: FragmentSubmissionSymptomCalendarBinding by viewBindingLazy()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.symptomCalendarContainer.setDateSelectedListener {
            viewModel.onDateSelected(it)
        }

        viewModel.routeToScreen.observe2(this) {
            when (it) {
                is SubmissionNavigationEvents.NavigateToResultPositiveOtherWarning -> doNavigate(
                    SubmissionSymptomCalendarFragmentDirections
                        .actionSubmissionSymptomCalendarFragmentToSubmissionResultPositiveOtherWarningFragment(
                            it.symptoms
                        )
                )
                is SubmissionNavigationEvents.NavigateToSymptomIntroduction -> doNavigate(
                    SubmissionSymptomCalendarFragmentDirections
                        .actionSubmissionCalendarFragmentToSubmissionSymptomIntroductionFragment()
                )
            }
        }

        viewModel.symptomStart.observe2(this) {
            updateButtons(it)
            if (it !is Symptoms.StartOf.Date) {
                binding.symptomCalendarContainer.unsetSelection()
            }
        }

        binding.apply {
            submissionSymptomCalendarHeader.headerButtonBack.buttonIcon
                .setOnClickListener { viewModel.onCalendarPreviousClicked() }

            symptomButtonNext
                .setOnClickListener { viewModel.onCalendarNextClicked() }

            symptomCalendarChoiceSelection
                .calendarButtonSevenDays
                .setOnClickListener { viewModel.onLastSevenDaysStart() }

            symptomCalendarChoiceSelection
                .calendarButtonOneTwoWeeks
                .setOnClickListener { viewModel.onOneToTwoWeeksAgoStart() }

            symptomCalendarChoiceSelection
                .calendarButtonMoreThanTwoWeeks
                .setOnClickListener { viewModel.onMoreThanTwoWeeksStart() }

            symptomCalendarChoiceSelection
                .targetButtonVerify
                .setOnClickListener { viewModel.onNoInformationStart() }
        }
    }

    private fun updateButtons(symptomStart: Symptoms.StartOf?) {
        binding.symptomCalendarChoiceSelection.calendarButtonSevenDays
            .findViewById<Button>(R.id.calendar_button_seven_days)
            .setTextColor(
                formatCalendarButtonStyleByState(
                    symptomStart,
                    Symptoms.StartOf.LastSevenDays
                )
            )
        binding.symptomCalendarChoiceSelection.targetLayout
            .findViewById<Button>(R.id.calendar_button_seven_days).backgroundTintList =
            ColorStateList.valueOf(
                formatCalendarBackgroundButtonStyleByState(
                    symptomStart, Symptoms.StartOf.LastSevenDays
                )
            )

        binding.symptomCalendarChoiceSelection.targetLayout
            .findViewById<Button>(R.id.calendar_button_one_two_weeks)
            .setTextColor(
                formatCalendarButtonStyleByState(
                    symptomStart,
                    Symptoms.StartOf.OneToTwoWeeksAgo
                )
            )
        binding.symptomCalendarChoiceSelection.targetLayout
            .findViewById<Button>(R.id.calendar_button_one_two_weeks).backgroundTintList =
            ColorStateList.valueOf(
                formatCalendarBackgroundButtonStyleByState(
                    symptomStart, Symptoms.StartOf.OneToTwoWeeksAgo
                )
            )

        binding.symptomCalendarChoiceSelection.targetLayout
            .findViewById<Button>(R.id.calendar_button_more_than_two_weeks)
            .setTextColor(
                formatCalendarButtonStyleByState(
                    symptomStart,
                    Symptoms.StartOf.MoreThanTwoWeeks
                )
            )
        binding.symptomCalendarChoiceSelection.targetLayout
            .findViewById<Button>(R.id.calendar_button_more_than_two_weeks).backgroundTintList =
            ColorStateList.valueOf(
                formatCalendarBackgroundButtonStyleByState(
                    symptomStart, Symptoms.StartOf.MoreThanTwoWeeks
                )
            )

        binding.symptomCalendarChoiceSelection.targetLayout
            .findViewById<Button>(R.id.target_button_verify)
            .setTextColor(
                formatCalendarButtonStyleByState(
                    symptomStart,
                    Symptoms.StartOf.NoInformation
                )
            )
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
}
