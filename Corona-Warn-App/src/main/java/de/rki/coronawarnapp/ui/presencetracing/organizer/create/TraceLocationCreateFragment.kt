package de.rki.coronawarnapp.ui.presencetracing.organizer.create

import android.os.Bundle
import android.text.format.DateFormat.is24HourFormat
import android.view.View
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.util.getLocale
import de.rki.coronawarnapp.contactdiary.util.hideKeyboard
import de.rki.coronawarnapp.databinding.TraceLocationCreateFragmentBinding
import de.rki.coronawarnapp.ui.durationpicker.DurationPicker
import de.rki.coronawarnapp.ui.durationpicker.toContactDiaryFormat
import de.rki.coronawarnapp.util.DialogHelper
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import org.joda.time.DateTime
import org.joda.time.Duration
import org.joda.time.LocalDate
import org.joda.time.LocalTime
import javax.inject.Inject

class TraceLocationCreateFragment : Fragment(R.layout.trace_location_create_fragment), AutoInject {

    private val binding: TraceLocationCreateFragmentBinding by viewBindingLazy()
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

        binding.toolbar.setNavigationOnClickListener {
            it.hideKeyboard()
            popBackStack()
        }

        viewModel.result.observe(viewLifecycleOwner) { result ->
            when (result) {
                is TraceLocationCreateViewModel.Result.Error -> {
                    DialogHelper.showDialog(getErrorDialogInstance(result.exception))
                }
                is TraceLocationCreateViewModel.Result.Success -> {
                    doNavigate(
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
                progressBar.isVisible = state.isRequestInProgress
                layoutBegin.isVisible = state.isDateVisible
                layoutEnd.isVisible = state.isDateVisible
                valueLengthOfStay.text = state.getCheckInLength(resources)
                buttonSubmit.isEnabled = state.isSendEnable
            }
        }

        binding.descriptionInputEdit.doOnTextChanged { text, _, _, _ ->
            viewModel.description = text.toString()
        }

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
                    begin = it.startDate?.toDateTime()
                    end = it.endDate?.toDateTime()
                    checkInLength = Duration.standardMinutes(it.defaultCheckInLengthInMinutes?.toLong() ?: 0L)
                }
            }
        }
    }

    private fun getErrorDialogInstance(exception: Exception): DialogHelper.DialogInstance {
        return DialogHelper.DialogInstance(
            requireActivity(),
            R.string.tracelocation_generic_error_title,
            R.string.tracelocation_generic_qr_code_error_body,
            R.string.errors_generic_button_positive,
            R.string.errors_generic_button_negative,
            negativeButtonFunction = { showExceptionDetails(exception) }
        )
    }

    private fun showExceptionDetails(exception: Exception) {
        DialogHelper.showDialog(
            DialogHelper.DialogInstance(
                requireActivity(),
                R.string.errors_generic_headline,
                exception.toString(),
                R.string.errors_generic_button_positive
            )
        )
    }

    private fun showDatePicker(
        defaultValue: DateTime?,
        minConstraint: DateTime? = null,
        callback: (DateTime) -> Unit
    ) {
        MaterialDatePicker
            .Builder
            .datePicker()
            .setSelection(defaultValue?.toDateTime()?.millis)
            .apply {
                if (minConstraint != null) {
                    setCalendarConstraints(
                        CalendarConstraints.Builder()
                            .setValidator(
                                DateValidatorPointForward
                                    .from(minConstraint.withMillisOfDay(0).toDateTime().millis)
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

    private fun showTimePicker(date: LocalDate, hours: Int?, minutes: Int?, callback: (DateTime) -> Unit) {
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
                    callback(date.toDateTime(LocalTime(this.hour, this.minute)))
                }
            }
            .show(childFragmentManager, TIME_PICKER_TAG)
    }

    private fun showDurationPicker() {
        DurationPicker.Builder()
            .duration(viewModel.checkInLength.toContactDiaryFormat())
            .title(getString(R.string.tracelocation_organizer_add_event_length_of_stay))
            .build()
            .apply {
                setDurationChangeListener {
                    viewModel.checkInLength = it
                }
            }
            .show(parentFragmentManager, DURATION_PICKER_TAG)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(
            outState.apply {
                putLong(LENGTH_OF_STAY, viewModel.checkInLength.standardMinutes)
                putSerializable(BEGIN, viewModel.begin)
                putSerializable(END, viewModel.end)
            }
        )
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.getLong(LENGTH_OF_STAY)?.let {
            viewModel.checkInLength = Duration.standardMinutes(it)
        }
        viewModel.begin = savedInstanceState?.getSerializable(BEGIN) as DateTime?
        viewModel.end = savedInstanceState?.getSerializable(END) as DateTime?
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
