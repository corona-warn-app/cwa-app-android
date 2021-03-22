package de.rki.coronawarnapp.ui.eventregistration.organizer.create

import android.os.Bundle
import android.view.View
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.util.getLocale
import de.rki.coronawarnapp.contactdiary.util.hideKeyboard
import de.rki.coronawarnapp.databinding.TraceLocationCreateFragmentBinding
import de.rki.coronawarnapp.ui.durationpicker.DurationPicker
import de.rki.coronawarnapp.ui.durationpicker.toContactDiaryFormat
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import org.joda.time.DateTimeZone
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime
import org.joda.time.LocalTime
import javax.inject.Inject

class TraceLocationCreateFragment : Fragment(R.layout.trace_location_create_fragment), AutoInject {

    private val binding: TraceLocationCreateFragmentBinding by viewBindingLazy()
    private val navArgs by navArgs<TraceLocationCreateFragmentArgs>()

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: TraceLocationCreateViewModel by cwaViewModelsAssisted(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, _ ->
            factory as TraceLocationCreateViewModel.Factory
            factory.create(navArgs.category)
        }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener {
            it.hideKeyboard()
            popBackStack()
        }

        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            binding.toolbar.setSubtitle(state.title)
            binding.valueStart.text = state.getStartDate(requireContext().getLocale())
            binding.valueEnd.text = state.getEndDate(requireContext().getLocale())
            binding.layoutBegin.visibility = if (state.isDateVisible) View.VISIBLE else View.GONE
            binding.layoutEnd.visibility = if (state.isDateVisible) View.VISIBLE else View.GONE
            binding.valueLengthOfStay.text = state.getLength(resources)
            binding.buttonSubmit.isEnabled = state.isSendEnable
        }

        binding.descriptionInputEdit.doOnTextChanged { text, _, _, _ ->
            viewModel.description = text?.toString()
        }

        binding.placeInputEdit.doOnTextChanged { text, _, _, _ ->
            viewModel.address = text?.toString()
        }

        binding.layoutBegin.setOnClickListener {
            it.hideKeyboard()
            showDatePicker(viewModel.start) { value ->
                viewModel.start = value
            }
        }

        binding.layoutEnd.setOnClickListener {
            it.hideKeyboard()
            showDatePicker(viewModel.end, viewModel.start) { value ->
                viewModel.end = value
            }
        }

        binding.layoutLengthOfStay.setOnClickListener {
            it.hideKeyboard()
            showDurationPicker()
        }

        binding.buttonSubmit.setOnClickListener {
            viewModel.send()
        }
    }

    private fun showDatePicker(
        defaultValue: LocalDateTime?,
        minConstraint: LocalDateTime? = null,
        callback: (LocalDateTime) -> Unit
    ) {
        MaterialDatePicker
            .Builder
            .datePicker()
            .setSelection(defaultValue?.toDateTime(DateTimeZone.UTC)?.millis)
            .apply {
                if (minConstraint != null) {
                    setCalendarConstraints(
                        CalendarConstraints.Builder()
                            .setValidator(
                                DateValidatorPointForward
                                    .from(minConstraint.withMillisOfDay(0).toDateTime(DateTimeZone.UTC).millis)
                            )
                            .build()
                    )
                }
            }
            .build()
            .apply {
                addOnPositiveButtonClickListener {
                    showTimePicker(LocalDate(it), defaultValue?.hourOfDay, defaultValue?.minuteOfHour, callback)
                }
            }
            .show(childFragmentManager, DATE_PICKER_TAG)
    }

    private fun showTimePicker(date: LocalDate, hours: Int?, minutes: Int?, callback: (LocalDateTime) -> Unit) {
        MaterialTimePicker
            .Builder()
            .apply {
                if (hours != null && minutes != null) {
                    setHour(hours)
                    setMinute(minutes)
                }
            }
            .build()
            .apply {
                addOnPositiveButtonClickListener {
                    callback(date.toLocalDateTime(LocalTime(this.hour, this.minute)))
                }
            }
            .show(childFragmentManager, TIME_PICKER_TAG)
    }

    private fun showDurationPicker() {
        DurationPicker.Builder()
            .duration(viewModel.checkInLength?.toContactDiaryFormat() ?: "")
            .title(getString(R.string.tracelocation_organizer_add_event_length_of_stay))
            .build()
            .apply {
                setDurationChangeListener {
                    viewModel.checkInLength = it
                }
            }
            .show(parentFragmentManager, DURATION_PICKER_TAG)
    }

    companion object {
        private const val DURATION_PICKER_TAG = "duration_picker"
        private const val DATE_PICKER_TAG = "date_picker"
        private const val TIME_PICKER_TAG = "time_picker"
    }
}
