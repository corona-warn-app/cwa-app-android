package de.rki.coronawarnapp.ui.submission.symptoms.calendar

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSubmissionSymptomCalendarBinding
import de.rki.coronawarnapp.submission.Symptoms
import de.rki.coronawarnapp.ui.submission.submissionCancelDialog
import de.rki.coronawarnapp.util.formatter.formatSymptomBackgroundButtonStyleByState
import de.rki.coronawarnapp.util.formatter.formatSymptomButtonTextStyleByState
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.assistedViewModel
import javax.inject.Inject

@AndroidEntryPoint
class SubmissionSymptomCalendarFragment : Fragment(R.layout.fragment_submission_symptom_calendar) {

    @Inject lateinit var factory: SubmissionSymptomCalendarViewModel.Factory
    val navArgs by navArgs<SubmissionSymptomCalendarFragmentArgs>()
    private val viewModel: SubmissionSymptomCalendarViewModel by assistedViewModel {
        factory.create(
            navArgs.symptomIndication,
            navArgs.testType,
            navArgs.comesFromDispatcherFragment
        )
    }

    private val binding: FragmentSubmissionSymptomCalendarBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.symptomCalendarContainer.setDateSelectedListener {
            viewModel.onDateSelected(it)
        }

        viewModel.showCancelDialog.observe(viewLifecycleOwner) {
            submissionCancelDialog { viewModel.onCancelConfirmed() }
        }

        viewModel.navigateBack.observe(viewLifecycleOwner) {
            popBackStack()
        }

        val backCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = submissionCancelDialog { viewModel.onCancelConfirmed() }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backCallback)

        viewModel.routeToScreen.observe(viewLifecycleOwner) {
            findNavController().navigate(it)
        }

        viewModel.symptomStart.observe(viewLifecycleOwner) {
            when (it) {
                is Symptoms.StartOf.Date -> binding.symptomCalendarContainer.setSelectedDate(it.date)
                else -> binding.symptomCalendarContainer.setSelectedDate(null)
            }

            updateButtons(it)
        }

        binding.toolbar.setNavigationOnClickListener { viewModel.onCalendarPreviousClicked() }
    }

    private fun updateButtons(symptomStart: Symptoms.StartOf?) {
        binding.apply {
            calendarButtonSevenDays.apply {
                handleColors(symptomStart, Symptoms.StartOf.LastSevenDays)
                setOnClickListener { viewModel.onLastSevenDaysStart() }
            }

            calendarButtonOneTwoWeeks.apply {
                handleColors(symptomStart, Symptoms.StartOf.OneToTwoWeeksAgo)
                setOnClickListener { viewModel.onOneToTwoWeeksAgoStart() }
            }

            calendarButtonMoreThanTwoWeeks.apply {
                handleColors(symptomStart, Symptoms.StartOf.MoreThanTwoWeeks)
                setOnClickListener { viewModel.onMoreThanTwoWeeksStart() }
            }
            targetButtonVerify.apply {
                handleColors(symptomStart, Symptoms.StartOf.NoInformation)
                setOnClickListener { viewModel.onNoInformationStart() }
            }

            symptomButtonNext.apply {
                isActive = symptomStart != null
                setOnClickListener { viewModel.onDone() }
            }
        }
    }

    private fun Button.handleColors(symptomStart: Symptoms.StartOf?, state: Symptoms.StartOf) {
        setTextColor(formatSymptomButtonTextStyleByState(context, symptomStart, state))
        backgroundTintList = ColorStateList.valueOf(
            formatSymptomBackgroundButtonStyleByState(context, symptomStart, state)
        )
    }

    override fun onResume() {
        super.onResume()
        viewModel.onNewUserActivity()
    }
}
