package de.rki.coronawarnapp.ui.presencetracing.organizer.warn.duration

import android.os.Bundle
import android.text.format.DateFormat
import android.view.View
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.google.android.material.transition.MaterialSharedAxis
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.util.hideKeyboard
import de.rki.coronawarnapp.databinding.TraceLocationOrganizerWarnDurationFragmentBinding
import de.rki.coronawarnapp.ui.durationpicker.DurationPicker
import de.rki.coronawarnapp.ui.durationpicker.toContactDiaryFormat
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import org.joda.time.LocalDate
import org.joda.time.LocalTime
import javax.inject.Inject

class TraceLocationWarnDurationFragment :
    Fragment(R.layout.trace_location_organizer_warn_duration_fragment),
    AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val binding: TraceLocationOrganizerWarnDurationFragmentBinding by viewBinding()
    private val navArgs by navArgs<TraceLocationWarnDurationFragmentArgs>()
    private val viewModel: TraceLocationWarnDurationViewModel by cwaViewModelsAssisted(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, _ ->
            factory as TraceLocationWarnDurationViewModel.Factory
            factory.create(navArgs.traceLocationId)
        }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.state.observe2(this) { uiState ->
            with(binding) {
                if (uiState.description != null) {
                    description.text = uiState.description
                }

                if (uiState.startDateTime != null && uiState.endDateTime != null) {

                    val startTime = uiState.startDateTime!!.toDateTime()
                    val endTime = uiState.endDateTime!!.toDateTime()

                    eventDate.isGone = false

                    val startDay = startTime.toLocalDate().toString("dd.MM.yyyy")
                    val startHour = startTime.toLocalTime().toString("HH:mm")
                    val endDay = endTime.toLocalDate().toString("dd.MM.yyyy")
                    val endHour = endTime.toLocalTime().toString("HH:mm")
                    eventDate.text = if (startTime.toLocalDate() == endTime.toLocalDate()) {
                        requireContext().getString(
                            R.string.trace_location_organizer_detail_item_duration,
                            startDay,
                            startHour,
                            endHour
                        )
                    } else {
                        requireContext().getString(
                            R.string.trace_location_organizer_detail_item_duration_multiple_days,
                            startDay,
                            startHour,
                            endDay,
                            endHour
                        )
                    }
                } else {
                    eventDate.isGone = true
                }

                startInputEdit.setText(uiState.formattedDateTime())
                durationInputEdit.setText(uiState.duration.toContactDiaryFormat())
            }
        }

        with(binding) {
            startInputEdit.setOnClickListener {
                it.hideKeyboard()
                showDatePicker()
            }
            durationInputEdit.setOnClickListener {
                it.hideKeyboard()
                showDurationPicker()
            }
        }
    }

    private fun showDatePicker() {
        val constraintsBuilder = CalendarConstraints.Builder()
            .setValidator(DateValidatorPointBackward.now())

        val dateTime = viewModel.selectedDate.toDateTime(viewModel.selectedTime)
        MaterialDatePicker
            .Builder
            .datePicker()
            .setSelection(dateTime.millis)
            .setCalendarConstraints(constraintsBuilder.build())
            .build()
            .apply {
                addOnPositiveButtonClickListener {
                    showTimePicker(LocalDate(it), viewModel.selectedTime)
                }
            }
            .show(childFragmentManager, DATE_PICKER_TAG)
    }

    private fun showTimePicker(date: LocalDate, time: LocalTime) {
        val timeFormat = when {
            DateFormat.is24HourFormat(requireContext()) -> TimeFormat.CLOCK_24H
            else -> TimeFormat.CLOCK_12H
        }
        MaterialTimePicker
            .Builder()
            .setTimeFormat(timeFormat)
            .setHour(time.hourOfDay)
            .setMinute(time.minuteOfHour)
            .build()
            .apply {
                addOnPositiveButtonClickListener {
                    viewModel.dateChanged(date, LocalTime(hour, minute))
                }
            }
            .show(childFragmentManager, TIME_PICKER_TAG)
    }

    private fun showDurationPicker() {
        DurationPicker.Builder()
            .duration(viewModel.selectedDuration.toContactDiaryFormat())
            .title(getString(R.string.tracelocation_organizer_add_event_length_of_stay)) //TODO <<<<<
            .build()
            .apply {
                setDurationChangeListener {
                    viewModel.durationChanged(it)
                }
            }
            .show(childFragmentManager, DURATION_PICKER_TAG)
    }

    companion object {
        private const val DURATION_PICKER_TAG = "duration_picker"
        private const val DATE_PICKER_TAG = "date_picker"
        private const val TIME_PICKER_TAG = "time_picker"
    }
}
