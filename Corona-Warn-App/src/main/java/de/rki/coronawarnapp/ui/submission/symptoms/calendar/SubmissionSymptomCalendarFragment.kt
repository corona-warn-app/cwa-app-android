package de.rki.coronawarnapp.ui.submission.symptoms.calendar

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSubmissionSymptomCalendarBinding
import de.rki.coronawarnapp.submission.Symptoms
import de.rki.coronawarnapp.ui.submission.SubmissionBlockingDialog
import de.rki.coronawarnapp.ui.submission.SubmissionCancelDialog
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionNavigationEvents.NavigateToMainActivity
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionNavigationEvents.NavigateToResultPositiveOtherWarning
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.formatter.formatCalendarBackgroundButtonStyleByState
import de.rki.coronawarnapp.util.formatter.formatCalendarButtonStyleByState
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import javax.inject.Inject

class SubmissionSymptomCalendarFragment : Fragment(R.layout.fragment_submission_symptom_calendar),
    AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: SubmissionSymptomCalendarViewModel by cwaViewModelsAssisted(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, _ ->
            factory as SubmissionSymptomCalendarViewModel.Factory
            factory.create()
        }
    )

    private val binding: FragmentSubmissionSymptomCalendarBinding by viewBindingLazy()
    private lateinit var uploadDialog: SubmissionBlockingDialog

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        uploadDialog = SubmissionBlockingDialog(requireContext())

        binding.symptomCalendarContainer.setDateSelectedListener {
            viewModel.onDateSelected(it)
        }

        viewModel.showCancelDialog.observe2(this) {
            SubmissionCancelDialog(requireContext()).show {
                viewModel.onCancelConfirmed()
            }
        }
        viewModel.showUploadDialog.observe2(this) {
            uploadDialog.setState(show = it)
        }

        viewModel.routeToScreen.observe2(this) {
            when (it) {
                is NavigateToResultPositiveOtherWarning -> doNavigate(
                    SubmissionSymptomCalendarFragmentDirections
                        .actionSubmissionSymptomCalendarFragmentToSubmissionResultPositiveOtherWarningFragment()
                )
                is NavigateToMainActivity -> doNavigate(
                    SubmissionSymptomCalendarFragmentDirections.actionSubmissionSymptomCalendarFragmentToMainFragment()
                )
            }
        }

        viewModel.symptomStart.observe2(this) {
            when (it) {
                is Symptoms.StartOf.Date -> binding.symptomCalendarContainer.setSelectedDate(it.date)
                else -> binding.symptomCalendarContainer.setSelectedDate(null)
            }

            updateButtons(it)
        }

        val backCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                viewModel.onCalendarPreviousClicked()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backCallback)

        binding.submissionSymptomCalendarHeader.headerButtonBack.buttonIcon.setOnClickListener {
            viewModel.onCalendarPreviousClicked()
        }
    }

    private fun updateButtons(symptomStart: Symptoms.StartOf?) {
        binding.apply {
            calendarButtonSevenDays.apply {
                setTextColor(
                    formatCalendarButtonStyleByState(symptomStart, Symptoms.StartOf.LastSevenDays)
                )
                backgroundTintList = ColorStateList.valueOf(
                    formatCalendarBackgroundButtonStyleByState(symptomStart, Symptoms.StartOf.LastSevenDays)
                )
                setOnClickListener { viewModel.onLastSevenDaysStart() }
            }

            calendarButtonOneTwoWeeks.apply {
                setTextColor(
                    formatCalendarButtonStyleByState(symptomStart, Symptoms.StartOf.OneToTwoWeeksAgo)
                )
                backgroundTintList = ColorStateList.valueOf(
                    formatCalendarBackgroundButtonStyleByState(symptomStart, Symptoms.StartOf.OneToTwoWeeksAgo)
                )
                setOnClickListener { viewModel.onOneToTwoWeeksAgoStart() }
            }

            calendarButtonMoreThanTwoWeeks.apply {
                setTextColor(
                    formatCalendarButtonStyleByState(symptomStart, Symptoms.StartOf.MoreThanTwoWeeks)
                )
                backgroundTintList = ColorStateList.valueOf(
                    formatCalendarBackgroundButtonStyleByState(symptomStart, Symptoms.StartOf.MoreThanTwoWeeks)
                )
                setOnClickListener { viewModel.onMoreThanTwoWeeksStart() }
            }
            targetButtonVerify.apply {
                setTextColor(formatCalendarButtonStyleByState(symptomStart, Symptoms.StartOf.NoInformation))
                backgroundTintList = ColorStateList.valueOf(
                    formatCalendarBackgroundButtonStyleByState(symptomStart, Symptoms.StartOf.NoInformation)
                )
                setOnClickListener { viewModel.onNoInformationStart() }
            }

            symptomButtonNext.apply {
                isEnabled = symptomStart != null
                setOnClickListener { viewModel.onDone() }
            }
        }
    }
}
