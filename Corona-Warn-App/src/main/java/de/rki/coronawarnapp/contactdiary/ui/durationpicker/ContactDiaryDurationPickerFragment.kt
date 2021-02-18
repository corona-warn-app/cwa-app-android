package de.rki.coronawarnapp.contactdiary.ui.durationpicker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import de.rki.coronawarnapp.databinding.ContactDiaryDurationPickerDialogFragmentBinding
import org.joda.time.Duration
import org.joda.time.format.PeriodFormatter
import org.joda.time.format.PeriodFormatterBuilder

class ContactDiaryDurationPickerFragment : DialogFragment() {

    interface OnChangeListener {
        fun onChange(duration: Duration)
    }

    private var _binding: ContactDiaryDurationPickerDialogFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = ContactDiaryDurationPickerDialogFragmentBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding.hours) {
            minValue = 0
            maxValue = hoursArray.size - 1
            displayedValues = hoursArray
        }

        with(binding.minutes) {
            minValue = 0
            maxValue = minutesArray.size - 1
            displayedValues = minutesArray
        }

        with(binding) {
            val duration = requireArguments().getString(DURATION_ARGUMENT_KEY)!!.split(":").toTypedArray()
            hours.value = hoursArray.indexOf(duration[0])
            minutes.value = minutesArray.indexOf(duration[1])

            cancelButton.setOnClickListener { dismiss() }
            okButton.setOnClickListener {
                (targetFragment as? OnChangeListener)?.onChange(getDuration(hours.value, minutes.value))
                dismiss()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val DURATION_ARGUMENT_KEY = "duration"

        val minutesArray = arrayOf("00", "15", "30", "45")
        val hoursArray = Array(24) {
            if (it < 10) {
                "0$it"
            } else {
                it.toString()
            }
        }

        fun getDuration(hours: Int, minutes: Int): Duration {
            val durationString = hoursArray[hours] + ":" + minutesArray[minutes]
            val formatter: PeriodFormatter = PeriodFormatterBuilder()
                .appendHours()
                .appendLiteral(":")
                .appendMinutes()
                .toFormatter()
            return formatter.parsePeriod(durationString).toStandardDuration()
        }
    }
}
