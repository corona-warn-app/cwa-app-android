package de.rki.coronawarnapp.ui.presencetracing.attendee.edit

import android.os.Bundle
import android.text.format.DateFormat
import android.view.View
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.google.android.material.transition.MaterialContainerTransform
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentEditCheckInBinding
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import javax.inject.Inject
import kotlin.math.abs

class EditCheckInFragment : Fragment(R.layout.fragment_edit_check_in), AutoInject {
    private val navArgs by navArgs<EditCheckInFragmentArgs>()

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: EditCheckInViewModel by cwaViewModelsAssisted(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, _ ->
            factory as EditCheckInViewModel.Factory
            factory.create(if (navArgs.editCheckInId == 0L) null else navArgs.editCheckInId)
        }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedElementEnterTransition = MaterialContainerTransform()
        sharedElementReturnTransition = MaterialContainerTransform()
    }

    private val binding: FragmentEditCheckInBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            appBarLayout.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
                title.alpha = (
                    1.0f - abs(verticalOffset / (appBarLayout.totalScrollRange.toFloat() * 0.6f))
                    )
            }

            toolbar.setNavigationOnClickListener { viewModel.onClose() }

            editCheckinEditCardCheckinDate.setOnClickListener {
                viewModel.onStartDateClicked()
            }

            editCheckinEditCardCheckinTime.setOnClickListener {
                viewModel.onStartTimeClicked()
            }

            editCheckinEditCardCheckoutDate.setOnClickListener {
                viewModel.onEndDateClicked()
            }

            editCheckinEditCardCheckoutTime.setOnClickListener {
                viewModel.onEndTimeClicked()
            }

            editCheckinConfirmButton.setOnClickListener {
                viewModel.onSaveClicked()
            }
            root.transitionName = navArgs.editCheckInId.toString()
        }

        viewModel.events.observe(viewLifecycleOwner) { navEvent ->
            when (navEvent) {
                EditCheckInNavigation.BackNavigation -> popBackStack()
                EditCheckInNavigation.ConfirmNavigation -> popBackStack()
            }
        }

        viewModel.uiState.observe(viewLifecycleOwner) { uiState ->
            with(binding) {
                editCheckinInfoCardHeader.text = getString(uiState.typeRes)
                editCheckinInfoCardTitle.text = uiState.description
                editCheckinInfoCardAddress.text = uiState.address

                editCheckinEditCardCheckinDate.text = uiState.checkInStartDate
                editCheckinEditCardCheckinTime.text = uiState.checkInStartTime

                editCheckinEditCardCheckoutDate.text = uiState.checkInEndDate
                editCheckinEditCardCheckoutTime.text = uiState.checkInEndTime

                editCheckinDurationEditHintCard.isGone = !uiState.diaryWarningVisible

                editCheckinConfirmButton.isEnabled = uiState.saveButtonEnabled

                editCheckinWrongInputWarning.isGone = !uiState.wrongInputErrorShown
            }
        }

        viewModel.openStartPickerEvent.observe(viewLifecycleOwner) { event ->
            when (event) {
                is EditCheckInViewModel.DateTimePickerEvent.DatePickerEvent ->
                    showDatePicker(event.localDate) {
                        viewModel.onStartTimeChanged(
                            EditCheckInViewModel.DateTimePickerEvent.DatePickerEvent(it)
                        )
                    }

                is EditCheckInViewModel.DateTimePickerEvent.TimePickerEvent ->
                    showTimePicker(event.localTime) {
                        viewModel.onStartTimeChanged(
                            EditCheckInViewModel.DateTimePickerEvent.TimePickerEvent(it)
                        )
                    }
            }
        }

        viewModel.openEndPickerEvent.observe(viewLifecycleOwner) { event ->
            when (event) {
                is EditCheckInViewModel.DateTimePickerEvent.DatePickerEvent ->
                    showDatePicker(event.localDate) {
                        viewModel.onEndTimeChanged(
                            EditCheckInViewModel.DateTimePickerEvent.DatePickerEvent(it)
                        )
                    }

                is EditCheckInViewModel.DateTimePickerEvent.TimePickerEvent ->
                    showTimePicker(event.localTime) {
                        viewModel.onEndTimeChanged(
                            EditCheckInViewModel.DateTimePickerEvent.TimePickerEvent(it)
                        )
                    }
            }
        }
    }

    private fun showDatePicker(
        defaultValue: LocalDate?,
        callback: (LocalDate) -> Unit
    ) {
        MaterialDatePicker
            .Builder
            .datePicker()
            .setSelection(defaultValue?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli())
            .build()
            .apply {
                addOnPositiveButtonClickListener {
                    callback(Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate())
                }
            }
            .show(childFragmentManager, DATE_PICKER_TAG)
    }

    private fun showTimePicker(
        defaultValue: LocalTime?,
        callback: (LocalTime) -> Unit
    ) {
        MaterialTimePicker
            .Builder()
            .setTimeFormat(
                if (DateFormat.is24HourFormat(requireContext())) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H
            )
            .apply {
                if (defaultValue != null) {
                    setHour(defaultValue.hour)
                    setMinute(defaultValue.minute)
                }
            }
            .build()
            .apply {
                addOnPositiveButtonClickListener {
                    callback(LocalTime.of(this.hour, this.minute))
                }
            }
            .show(childFragmentManager, TIME_PICKER_TAG)
    }

    companion object {
        private const val DATE_PICKER_TAG = "date_picker"
        private const val TIME_PICKER_TAG = "time_picker"
    }
}
