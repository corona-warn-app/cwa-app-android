package de.rki.coronawarnapp.covidcertificate.validation.ui.validationstart

import TextViewUrlSet
import android.os.Bundle
import android.text.format.DateFormat
import android.view.View
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.util.getLocale
import de.rki.coronawarnapp.databinding.ValidationStartFragmentBinding
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.toResolvingString
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.LocalTime
import setTextWithUrls
import javax.inject.Inject

class ValidationStartFragment : Fragment(R.layout.validation_start_fragment), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel by cwaViewModels<ValidationStartViewModel> { viewModelFactory }
    private val binding by viewBinding<ValidationStartFragmentBinding>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {
        viewModel.countryList.observe(viewLifecycleOwner) { countries ->
            val countriesReadable = countries.map { it.getCountryDisplayName(requireContext().getLocale()) }
            val adapter = ArrayAdapter(requireContext(), R.layout.validation_start_land_list_item, countriesReadable)
            countryPicker.apply {
                setAdapter(adapter)
                setText(adapter.getItem(0), false)
            }
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            state?.let { datePicker.setText(it.formattedDateTime()) }
        }

        dateInfoIcon.setOnClickListener { viewModel.onInfoClick() }
        privacyInformation.setOnClickListener { viewModel.onPrivacyClick() }
        checkButton.setOnClickListener { viewModel.onCheckClick() }
        datePicker.setOnClickListener {
            showDatePicker(viewModel.currentDateTime) { value -> viewModel.dateChanged(value) }
        }

        countryPicker.setOnItemClickListener { parent, _, position, _ ->
            viewModel.countryChanged(parent.adapter.getItem(position).toString())
        }

        faq.setTextWithUrls(
            R.string.validation_start_faq.toResolvingString(),
            TextViewUrlSet(
                labelResource = R.string.validation_start_faq_label,
                urlResource = R.string.validation_start_faq_link
            ),
            TextViewUrlSet(
                labelResource = R.string.validation_start_reopen_europe_label,
                urlResource = R.string.validation_start_reopen_europe_link
            )
        )

        viewModel.routeToScreen.observe(viewLifecycleOwner) {
            when (it) {
                ValidationStartNavigationEvents.NavigateToValidationInfoFragment -> doNavigate(
                    ValidationStartFragmentDirections.actionValidationStartFragmentToValidationTimeInfoFragment()
                )
                ValidationStartNavigationEvents.NavigateToPrivacyFragment -> doNavigate(
                    ValidationStartFragmentDirections.actionValidationStartFragmentToPrivacyFragment()
                )

                ValidationStartNavigationEvents.NavigateToNewFunctionFragment -> {
                    // TODO: navigation to next screen (new functions)
                }
            }
        }
    }

    private fun showDatePicker(
        defaultValue: DateTime,
        minConstraint: DateTime = DateTime.now().minusDays(1), // Allow selection from today on
        callback: (DateTime) -> Unit
    ) {
        MaterialDatePicker
            .Builder
            .datePicker()
            .setSelection(defaultValue.millis)
            .apply {
                setCalendarConstraints(
                    CalendarConstraints.Builder()
                        .setValidator(DateValidatorPointForward.from(minConstraint.withSecondOfMinute(0).millis))
                        .build()
                )
            }
            .build()
            .apply {
                addOnPositiveButtonClickListener {
                    showTimePicker(LocalDate(it), defaultValue.hourOfDay, defaultValue.minuteOfHour, callback)
                }
            }
            .show(childFragmentManager, DATE_PICKER_TAG)
    }

    private fun showTimePicker(date: LocalDate, hours: Int?, minutes: Int?, callback: (DateTime) -> Unit) {
        MaterialTimePicker
            .Builder()
            .setTimeFormat(
                if (DateFormat.is24HourFormat(requireContext())) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H
            )
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
