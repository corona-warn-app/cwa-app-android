package de.rki.coronawarnapp.covidcertificate.validation.ui.validationstart

import TextViewUrlSet
import android.os.Bundle
import android.text.format.DateFormat
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.bugreporting.ui.toErrorDialogBuilder
import de.rki.coronawarnapp.covidcertificate.validation.core.DccValidation
import de.rki.coronawarnapp.covidcertificate.validation.core.country.DccCountry
import de.rki.coronawarnapp.databinding.ValidationStartFragmentBinding
import de.rki.coronawarnapp.util.ContextExtensions.getColorCompat
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.toResolvingString
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.LocalTime
import setTextWithUrls
import javax.inject.Inject

class ValidationStartFragment : Fragment(R.layout.validation_start_fragment), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val args by navArgs<ValidationStartFragmentArgs>()
    private val viewModel by cwaViewModelsAssisted<ValidationStartViewModel>(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, _ ->
            factory as ValidationStartViewModel.Factory
            factory.create(args.containerId)
        }
    )
    private val binding by viewBinding<ValidationStartFragmentBinding>()
    private val dccCountryAdapter by lazy { DccCountryAdapter(requireContext()) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) =
        with(binding) {
            toolbar.setNavigationOnClickListener { popBackStack() }
            dateInfoIcon.setOnClickListener { viewModel.onInfoClick() }
            privacyInformation.setOnClickListener { viewModel.onPrivacyClick() }
            checkButton.setOnClickListener { viewModel.onCheckClick() }
            datePicker.setOnClickListener {
                countryPicker.clearFocus()
                showDatePicker()
            }

            countryPicker.setOnItemClickListener { _, _, position, _ ->
                viewModel.countryChanged(dccCountryAdapter.getItem(position))
            }

            countryPicker.setAdapter(dccCountryAdapter)

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

            viewModel.state.observe(viewLifecycleOwner) { datePicker.setText(it.formattedDateTime()) }
            viewModel.countryList.observe(viewLifecycleOwner) { onCountiesAvailable(it) }
            viewModel.events.observe(viewLifecycleOwner) { onNavEvent(it) }
        }

    override fun onResume() {
        super.onResume()
        viewModel.refreshTimeCheck()
    }

    private fun ValidationStartFragmentBinding.onNavEvent(event: StartValidationNavEvent?) {
        when (event) {
            NavigateToValidationInfoFragment -> doNavigate(
                ValidationStartFragmentDirections.actionValidationStartFragmentToValidationTimeInfoFragment()
            )
            NavigateToPrivacyFragment -> doNavigate(
                ValidationStartFragmentDirections.actionValidationStartFragmentToPrivacyFragment()
            )
            is NavigateToValidationResultFragment -> navigateToResultScreen(event.validationResult)

            is ShowTimeMessage -> showTimeMessage(event)
            is ShowErrorDialog -> event.error.toErrorDialogBuilder(requireContext()).show()
        }
    }

    private fun ValidationStartFragmentBinding.showTimeMessage(event: ShowTimeMessage) {
        if (event.invalidTime) {
            dateInfo.setText(R.string.validation_start_time_error)
            dateInfo.setTextColor(requireContext().getColorCompat(R.color.colorTextSemanticRed))
            dateLayout.isErrorEnabled = true
            dateLayout.error = " " // Just to highlight the field with red colour
        } else {
            dateInfo.setText(R.string.validation_start_date_info)
            dateInfo.setTextColor(
                requireContext().getColorCompat(R.color.colorOnPrimary)
            )
            dateLayout.isErrorEnabled = false
        }
    }

    private fun navigateToResultScreen(validationResult: DccValidation) {
        when (validationResult.state) {
            DccValidation.State.PASSED ->
                ValidationStartFragmentDirections.actionValidationStartFragmentToDccValidationPassedFragment(
                    validationResult
                )

            DccValidation.State.OPEN ->
                ValidationStartFragmentDirections.actionValidationStartFragmentToDccValidationOpenFragment(
                    validationResult
                )

            DccValidation.State.TECHNICAL_FAILURE,
            DccValidation.State.FAILURE ->
                ValidationStartFragmentDirections.actionValidationStartFragmentToDccValidationFailedFragment(
                    validationResult
                )
        }.also { doNavigate(it) }
    }

    private fun ValidationStartFragmentBinding.onCountiesAvailable(countries: List<DccCountry>) {
        dccCountryAdapter.update(countries)
        countryPicker.setText(countries[0].displayName(), false)
    }

    private fun showDatePicker() {
        val minConstraint: DateTime = DateTime.now().minusDays(1) // Allow selection from today on only
        val constraints = CalendarConstraints.Builder()
            .setValidator(DateValidatorPointForward.from(minConstraint.withSecondOfMinute(0).millis))
            .build()

        val dateTime = viewModel.currentDateTime
        MaterialDatePicker
            .Builder
            .datePicker()
            .setSelection(dateTime.millis)
            .setCalendarConstraints(constraints)
            .build()
            .apply {
                addOnPositiveButtonClickListener {
                    showTimePicker(LocalDate(it), dateTime.hourOfDay, dateTime.minuteOfHour)
                }
            }
            .show(childFragmentManager, DATE_PICKER_TAG)
    }

    private fun showTimePicker(date: LocalDate, hours: Int, minutes: Int) {
        val timeFormat = when {
            DateFormat.is24HourFormat(requireContext()) -> TimeFormat.CLOCK_24H
            else -> TimeFormat.CLOCK_12H
        }
        MaterialTimePicker
            .Builder()
            .setTimeFormat(timeFormat)
            .setHour(hours)
            .setMinute(minutes)
            .build()
            .apply {
                addOnPositiveButtonClickListener {
                    viewModel.dateChanged(date.toDateTime(LocalTime(hour, minute)))
                }
            }
            .show(childFragmentManager, TIME_PICKER_TAG)
    }

    companion object {
        private const val DATE_PICKER_TAG = "date_picker"
        private const val TIME_PICKER_TAG = "time_picker"
    }
}
