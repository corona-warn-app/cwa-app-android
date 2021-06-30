package de.rki.coronawarnapp.covidcertificate.validation.ui

import android.os.Bundle
import android.text.format.DateFormat
import android.view.View
import android.view.View.OnFocusChangeListener
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.fragment.app.Fragment
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.util.getLocale
import de.rki.coronawarnapp.contactdiary.util.hideKeyboard
import de.rki.coronawarnapp.databinding.ValidationStartFragmentBinding
import de.rki.coronawarnapp.util.TextViewUrlSet
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.setDoubleUrl
import de.rki.coronawarnapp.util.setUrl
import de.rki.coronawarnapp.util.setUrls
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.LocalTime
import timber.log.Timber
import javax.inject.Inject

class ValidationStartFragment : Fragment(R.layout.validation_start_fragment), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: ValidationStartViewModel by cwaViewModels { viewModelFactory }
    private val binding by viewBinding<ValidationStartFragmentBinding>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.landList.observe2(this){
            val landAdapter = ArrayAdapter(requireContext(), R.layout.validation_start_land_list_item, it)
            (binding.countryPicker as? AutoCompleteTextView)?.setAdapter(landAdapter)
            (binding.countryPicker as? AutoCompleteTextView)?.setText(landAdapter.getItem(0).toString(), false);
        }

        viewModel.state.observe2(this) { state ->
            binding.apply {
                if (state != null) {
                    datePicker.setText(state.getDate(requireContext().getLocale()))
                }
            }
        }

        binding.datePicker.setOnClickListener {
            it.hideKeyboard()
            binding.datePicker.clearFocus()
            showDatePicker(viewModel.state.value?.date) { value ->
                viewModel.dateChanged(value)
            }
        }

//        binding.countryPicker.setOnDismissListener {
//            binding.countryPicker.clearFocus()
//        }

        binding.countryPicker.setOnItemClickListener { parent, _, position, _ ->
//            binding.countryPicker.clearFocus()
            viewModel.countryChanged(parent.adapter.getItem(position) as String)
        }

        with(binding.faq) {
//            setDoubleUrl(
//                context.getString(R.string.validation_start_faq),
//                context.getString(R.string.validation_start_faq_label),
//                context.getString(R.string.validation_start_faq_link),
//                context.getString(R.string.validation_start_reopen_europe_link),
//                context.getString(R.string.validation_start_reopen_europe_link),
//            )

            setUrls(
                context.getString(R.string.validation_start_faq),
                listOf(
                    TextViewUrlSet(
                        labelResource = R.string.validation_start_faq_label,
                        urlResource = R.string.validation_start_faq_link
                    ),
                    TextViewUrlSet(
                        labelResource = R.string.validation_start_reopen_europe_label,
                        urlResource = R.string.validation_start_reopen_europe_link
                    ),
                    // just for testing
                    TextViewUrlSet(),
                    TextViewUrlSet("", ""),
                    TextViewUrlSet(labelResource = R.string.debugging_debuglog_intro_explanation_section_two_link_label),
                    TextViewUrlSet(
                        labelResource = R.string.debugging_debuglog_intro_explanation_section_two_link_label,
                        urlResource = R.string.debugging_debuglog_intro_explanation_section_two_faq_link
                    ),
                    TextViewUrlSet(
                        labelResource = R.string.debugging_debuglog_intro_explanation_section_two_link_label,
                        urlResource = null
                    )
                )
            )
        }
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
            .setTimeFormat(if (DateFormat.is24HourFormat(requireContext())) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H)
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

    companion object {
        private const val DATE_PICKER_TAG = "date_picker"
        private const val TIME_PICKER_TAG = "time_picker"
    }
}
