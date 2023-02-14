package de.rki.coronawarnapp.srs.ui.symptoms.calendar

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import dagger.hilt.android.AndroidEntryPoint
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSubmissionSymptomCalendarBinding
import de.rki.coronawarnapp.srs.ui.dialogs.showCloseDialog
import de.rki.coronawarnapp.srs.ui.dialogs.showSubmissionWarningDialog
import de.rki.coronawarnapp.srs.ui.dialogs.showTruncatedSubmissionDialog
import de.rki.coronawarnapp.srs.ui.vm.TeksSharedViewModel
import de.rki.coronawarnapp.submission.Symptoms
import de.rki.coronawarnapp.ui.dialog.displayDialog
import de.rki.coronawarnapp.util.ExternalActionHelper.openUrl
import de.rki.coronawarnapp.util.formatter.formatSymptomBackgroundButtonStyleByState
import de.rki.coronawarnapp.util.formatter.formatSymptomButtonTextStyleByState
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.assistedViewModel
import javax.inject.Inject

@AndroidEntryPoint
class SrsSymptomsCalendarFragment : Fragment(R.layout.fragment_submission_symptom_calendar) {

    @Inject lateinit var factory: SrsSymptomsCalendarViewModel.Factory
    val navArgs by navArgs<SrsSymptomsCalendarFragmentArgs>()
    private val teksSharedViewModel by navGraphViewModels<TeksSharedViewModel>(R.id.srs_nav_graph)
    private val viewModel: SrsSymptomsCalendarViewModel by assistedViewModel {
        factory.create(
            submissionType = navArgs.submissionType,
            selectedCheckIns = navArgs.selectedCheckIns,
            symptomsIndication = navArgs.symptomIndication,
            teksSharedViewModel = teksSharedViewModel
        )
    }

    private val binding: FragmentSubmissionSymptomCalendarBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val backCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = viewModel.onCancelConfirmed()
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backCallback)

        binding.symptomCalendarContainer.setDateSelectedListener {
            viewModel.onDateSelected(it)
        }

        viewModel.symptomStart.observe(viewLifecycleOwner) {
            when (it) {
                is Symptoms.StartOf.Date -> binding.symptomCalendarContainer.setSelectedDate(it.date)
                else -> binding.symptomCalendarContainer.setSelectedDate(null)
            }

            updateButtons(it)
        }

        binding.toolbar.setNavigationOnClickListener { viewModel.onCancelConfirmed() }

        viewModel.showLoadingIndicator.observe(viewLifecycleOwner) { binding.symptomButtonNext.isLoading = it }

        viewModel.events.observe(viewLifecycleOwner) {
            when (it) {
                SrsSymptomsCalendarNavigation.ShowCloseDialog -> showCloseDialog { viewModel.goHome() }
                SrsSymptomsCalendarNavigation.ShowSubmissionWarning -> showSubmissionWarningDialog {
                    viewModel.startSubmission()
                }

                SrsSymptomsCalendarNavigation.GoToHome -> findNavController().navigate(
                    SrsSymptomsCalendarFragmentDirections.actionSrsSymptomsCalendarFragmentToMainFragment()
                )

                is SrsSymptomsCalendarNavigation.GoToThankYouScreen -> findNavController().navigate(
                    SrsSymptomsCalendarFragmentDirections.actionSrsSymptomsCalendarFragmentToSrsSubmissionDoneFragment()
                )

                is SrsSymptomsCalendarNavigation.TruncatedSubmission -> {
                    showTruncatedSubmissionDialog(it.numberOfDays) {
                        viewModel.onTruncatedDialogClick()
                    }
                }

                is SrsSymptomsCalendarNavigation.Error -> displayDialog {
                    setError(it.cause)
                    positiveButton(android.R.string.ok)
                    negativeButton(R.string.nm_faq_label) { openUrl(R.string.srs_faq_url) }
                }
            }
        }
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
}
