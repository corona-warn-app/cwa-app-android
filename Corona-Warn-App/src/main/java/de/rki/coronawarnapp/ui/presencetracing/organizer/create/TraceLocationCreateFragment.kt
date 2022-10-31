package de.rki.coronawarnapp.ui.presencetracing.organizer.create

import android.os.Bundle
import android.text.InputFilter
import android.text.format.DateFormat.is24HourFormat
import android.view.View
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.util.getLocale
import de.rki.coronawarnapp.contactdiary.util.hideKeyboard
import de.rki.coronawarnapp.databinding.TraceLocationCreateFragmentBinding
import de.rki.coronawarnapp.ui.dialog.displayDialog
import de.rki.coronawarnapp.ui.durationpicker.DurationPicker
import de.rki.coronawarnapp.ui.durationpicker.format
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.addSubtitleId
import de.rki.coronawarnapp.util.ui.addTitleId
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class TraceLocationCreateFragment : Fragment(R.layout.trace_location_create_fragment), AutoInject {

    private val binding: TraceLocationCreateFragmentBinding by viewBinding()
    private val navArgs by navArgs<TraceLocationCreateFragmentArgs>()

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: TraceLocationCreateViewModel by cwaViewModelsAssisted(
        keyProducer = { navArgs.category.type.toString() },
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, _ ->
            factory as TraceLocationCreateViewModel.Factory
            factory.create(navArgs.category)
        }
    )

    @Suppress("NestedBlockDepth")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.apply {
            setNavigationOnClickListener {
                it.hideKeyboard()
                popBackStack()
            }
            addTitleId(R.id.trace_location_create_fragment_title_id)
            addSubtitleId(R.id.trace_location_create_fragment_subtitle_id)
        }

        viewModel.result.observe(viewLifecycleOwner) { result ->
            when (result) {
                is TraceLocationCreateViewModel.Result.Error -> {
                    showErrorDialog(result.exception)
                }
                is TraceLocationCreateViewModel.Result.Success -> {
                    findNavController().navigate(
                        TraceLocationCreateFragmentDirections
                            .actionTraceLocationCreateFragmentToTraceLocationOrganizerListFragment()
                    )
                }
            }
        }

        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            binding.apply {
                toolbar.setSubtitle(state.title)
                valueStart.text = state.getBegin(requireContext().getLocale())
                valueEnd.text = state.getEnd(requireContext().getLocale())
                buttonSubmit.isLoading = state.isRequestInProgress
                buttonSubmit.isActive = state.isSendEnable
                layoutBegin.isVisible = state.isDateVisible
                layoutEnd.isVisible = state.isDateVisible
                valueLengthOfStay.text = state.getCheckInLength(resources)
            }
        }

        binding.descriptionInputEdit.setMaxLength(MAX_LENGTH_DESCRIPTION)
        binding.descriptionInputEdit.doOnTextChanged { text, _, _, _ ->
            viewModel.description = text.toString()
        }

        binding.placeInputEdit.setMaxLength(MAX_LENGTH_LOCATION)
        binding.placeInputEdit.doOnTextChanged { text, _, _, _ ->
            viewModel.address = text.toString()
        }

        binding.layoutBegin.setOnClickListener {
            it.hideKeyboard()
            showDatePicker(viewModel.begin) { value ->
                viewModel.begin = value
            }
        }

        binding.layoutEnd.setOnClickListener {
            it.hideKeyboard()
            showDatePicker(viewModel.end, viewModel.begin) { value ->
                viewModel.end = value
            }
        }

        binding.layoutLengthOfStay.setOnClickListener {
            it.hideKeyboard()
            showDurationPicker()
        }

        binding.buttonSubmit.setOnClickListener {
            it.hideKeyboard()
            viewModel.send()
        }

        if (savedInstanceState == null) {
            navArgs.originalItem?.let {
                binding.apply {
                    descriptionInputEdit.setText(it.description)
                    placeInputEdit.setText(it.address)
                }
                viewModel.apply {
                    begin = it.startDate?.atZone(ZoneId.systemDefault())
                    end = it.endDate?.atZone(ZoneId.systemDefault())
                    checkInLength = Duration.ofMinutes(it.defaultCheckInLengthInMinutes?.toLong() ?: 0L)
                }
            }
        }
    }

    private fun showErrorDialog(exception: Exception) = displayDialog {
        title(R.string.tracelocation_generic_error_title)
        message(R.string.tracelocation_generic_qr_code_error_body)
        positiveButton(R.string.errors_generic_button_positive)
        negativeButton(R.string.errors_generic_button_negative) { showExceptionDetails(exception) }
    }

    private fun showExceptionDetails(exception: Exception) = displayDialog {
        title(R.string.errors_generic_headline)
        message(exception.toString())
        positiveButton(R.string.errors_generic_button_positive)
    }

    private fun showDatePicker(
        defaultValue: ZonedDateTime?,
        minConstraint: ZonedDateTime? = null,
        callback: (ZonedDateTime) -> Unit
    ) {
        MaterialDatePicker
            .Builder
            .datePicker()
            .setSelection(defaultValue?.toInstant()?.toEpochMilli())
            .apply {
                if (minConstraint != null) {
                    setCalendarConstraints(
                        CalendarConstraints.Builder()
                            .setValidator(
                                DateValidatorPointForward
                                    .from(
                                        minConstraint.minusDays(1).truncatedTo(ChronoUnit.MILLIS).toInstant()
                                            .toEpochMilli()
                                    )
                            )
                            .build()
                    )
                }
            }
            .build()
            .apply {
                addOnPositiveButtonClickListener {
                    showTimePicker(
                        Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate(),
                        defaultValue?.hour,
                        defaultValue?.minute,
                        callback
                    )
                }
            }
            .show(childFragmentManager, DATE_PICKER_TAG)
    }

    private fun showTimePicker(date: LocalDate, hours: Int?, minutes: Int?, callback: (ZonedDateTime) -> Unit) {
        MaterialTimePicker
            .Builder()
            .setTimeFormat(if (is24HourFormat(requireContext())) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H)
            .apply {
                if (hours != null && minutes != null) {
                    setHour(hours)
                    setMinute(minutes)
                }
            }
            .build()
            .apply {
                addOnPositiveButtonClickListener {
                    callback(date.atTime(this.hour, this.minute).atZone(ZoneId.systemDefault()))
                }
            }
            .show(childFragmentManager, TIME_PICKER_TAG)
    }

    private fun showDurationPicker() {
        DurationPicker.Builder()
            .duration(viewModel.checkInLength.format())
            .title(getString(R.string.tracelocation_organizer_add_event_length_of_stay))
            .build()
            .apply {
                setDurationChangeListener {
                    viewModel.checkInLength = it
                }
                setDurationValueChangeListener { duration ->
                    if (duration == Duration.ZERO) setDuration(arrayOf("00", "15"))
                }
            }
            .show(parentFragmentManager, DURATION_PICKER_TAG)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(
            outState.apply {
                putLong(LENGTH_OF_STAY, viewModel.checkInLength.toMinutes())
                putSerializable(BEGIN, viewModel.begin)
                putSerializable(END, viewModel.end)
            }
        )
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.getLong(LENGTH_OF_STAY)?.let {
            viewModel.checkInLength = Duration.ofMinutes(it)
        }
        viewModel.begin = savedInstanceState?.getSerializable(BEGIN) as ZonedDateTime?
        viewModel.end = savedInstanceState?.getSerializable(END) as ZonedDateTime?
    }

    private fun TextInputEditText.setMaxLength(max: Int) {
        filters += InputFilter.LengthFilter(max)
    }

    companion object {
        private const val DURATION_PICKER_TAG = "duration_picker"
        private const val DATE_PICKER_TAG = "date_picker"
        private const val TIME_PICKER_TAG = "time_picker"
        private const val LENGTH_OF_STAY = "length_of_stay"
        private const val BEGIN = "begin"
        private const val END = "end"
    }
}
