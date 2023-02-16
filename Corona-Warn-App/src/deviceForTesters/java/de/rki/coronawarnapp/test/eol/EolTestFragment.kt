package de.rki.coronawarnapp.test.eol

import android.os.Bundle
import android.text.format.DateFormat
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentTestEolBinding
import de.rki.coronawarnapp.test.menu.ui.TestMenuItem
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject

class EolTestFragment : Fragment(R.layout.fragment_test_eol), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory

    private val viewModel: EolTestViewModel by cwaViewModels { viewModelFactory }
    private val binding: FragmentTestEolBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.dateTime.observe(viewLifecycleOwner) {
            binding.dateTime.text = it.toString()
        }

        binding.openPicket.setOnClickListener { showDatePicker() }
    }

    private fun showDatePicker() {
        MaterialDatePicker
            .Builder
            .datePicker()
            .setSelection(Instant.now().toEpochMilli())
            .build()
            .apply {
                addOnPositiveButtonClickListener { date ->
                    showTimePicker { time ->
                        viewModel.updateEolDateTime(
                            Instant.ofEpochMilli(date)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                                .atTime(time.hour, time.minute)
                                .atZone(ZoneId.of("CET"))
                        )
                    }
                }
            }
            .show(childFragmentManager, "eol.test.date.picker")
    }

    private fun showTimePicker(
        defaultValue: LocalTime = LocalTime.now(),
        callback: (LocalTime) -> Unit
    ) {
        MaterialTimePicker
            .Builder()
            .setTimeFormat(
                if (DateFormat.is24HourFormat(requireContext())) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H
            )
            .apply {
                setHour(defaultValue.hour)
                setMinute(defaultValue.minute)
            }
            .build()
            .apply {
                addOnPositiveButtonClickListener {
                    callback(LocalTime.of(hour, minute))
                }
            }
            .show(childFragmentManager, "eol.test.time.picker")
    }

    companion object {
        val MENU_ITEM = TestMenuItem(
            title = "EOl Date Time",
            description = "End of Life Dev Settings",
            targetId = R.id.eolTestFragment
        )
    }
}
