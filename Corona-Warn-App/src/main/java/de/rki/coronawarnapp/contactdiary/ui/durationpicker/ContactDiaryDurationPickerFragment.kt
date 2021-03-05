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

    val binding: Lazy<ContactDiaryDurationPickerDialogFragmentBinding> = lazy {
        ContactDiaryDurationPickerDialogFragmentBinding.inflate(
            layoutInflater
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return binding.value.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding.value.hours) {
            minValue = 0
            maxValue = hoursArray.size - 1
            displayedValues = hoursArray
        }

        with(binding.value.minutes) {
            minValue = 0
            maxValue = minutesArray.size - 1
            displayedValues = minutesArray
        }

        with(binding.value) {
            var duration = requireArguments().getString(DURATION_ARGUMENT_KEY)!!.split(":").toTypedArray()
            if (duration.size < 2) duration = arrayOf("00", "00")

            hours.value = hoursArray.indexOf(duration[0])
            minutes.value = minutesArray.indexOf(duration[1])

            cancelButton.setOnClickListener { dismiss() }
            okButton.setOnClickListener {
                (targetFragment as? OnChangeListener)?.onChange(getDuration(hours.value, minutes.value))
                dismiss()
            }
        }
    }

    companion object {
        const val DURATION_ARGUMENT_KEY = "duration"

        val minutesArray = arrayOf("00", "15", "30", "45")
        val hoursArray = Array(24) { "%02d".format(it) }

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
