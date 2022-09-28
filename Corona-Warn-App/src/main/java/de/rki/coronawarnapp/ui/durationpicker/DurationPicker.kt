package de.rki.coronawarnapp.ui.durationpicker

import android.os.Bundle
import android.view.View
import androidx.annotation.IntRange
import androidx.fragment.app.DialogFragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.DurationPickerBinding
import de.rki.coronawarnapp.util.ui.viewBinding
import java.time.Duration
import kotlin.math.max

class DurationPicker : DialogFragment(R.layout.duration_picker) {

    fun interface OnChangeListener {
        fun onChange(duration: Duration)
    }

    fun interface OnDurationValueChangeListener {
        fun onDurationChange(duration: Duration)
    }

    private var onChangeListener = OnChangeListener {}
    private var onDurationValueChangeListener = OnDurationValueChangeListener { }

    private val hoursArray by lazy { requireArguments().getStringArray(HOURS_KEY).orEmpty() }
    private val title by lazy { requireArguments().getString(TITLE_KEY).orEmpty() }
    private val minutesArray by lazy { requireArguments().getStringArray(MINUTES_KEY).orEmpty() }

    private val binding: DurationPickerBinding by viewBinding()

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

        with(binding.title) {
            text = title
        }

        with(binding) {
            setDuration(
                requireArguments().getString(DURATION_KEY)!!.split(":").toTypedArray()
            )
            minutes.setOnValueChangedListener { _, _, _ ->
                onDurationValueChangeListener.onDurationChange(getDuration())
            }

            hours.setOnValueChangedListener { _, _, _ ->
                onDurationValueChangeListener.onDurationChange(getDuration())
            }

            cancelButton.setOnClickListener { dismiss() }
            okButton.setOnClickListener {
                onChangeListener.onChange(getDuration())
                dismiss()
            }
        }
    }

    /**
     * Picker values after user confirmation
     */
    fun setDurationChangeListener(onChangeListener: OnChangeListener) {
        this.onChangeListener = onChangeListener
    }

    /**
     * Instant picker value before user confirmation
     */
    fun setDurationValueChangeListener(onDurationValueChangeListener: OnDurationValueChangeListener) {
        this.onDurationValueChangeListener = onDurationValueChangeListener
    }

    fun setDuration(newDuration: Array<String>) {
        binding.setDuration(newDuration)
    }

    private fun DurationPickerBinding.setDuration(newDuration: Array<String>) {
        val duration = if (newDuration.size < 2) arrayOf("00", "00") else newDuration
        hours.value = max(0, hoursArray.indexOf(duration[0]))
        minutes.value = max(0, minutesArray.indexOf(duration[1]))
    }

    private fun getDuration(): Duration {
        return Duration.parse("PT${hoursArray[binding.hours.value]}H${minutesArray[binding.minutes.value]}M")
    }

    companion object {
        private const val DURATION_KEY = "duration"
        private const val TITLE_KEY = "title"
        private const val HOURS_KEY = "hours"
        private const val MINUTES_KEY = "minutes"

        private fun newInstance(builder: Builder) = DurationPicker()
            .apply {
                arguments = Bundle().apply {
                    putString(DURATION_KEY, builder.duration)
                    putString(TITLE_KEY, builder.title)
                    putStringArray(HOURS_KEY, builder.hoursArray)
                    putStringArray(MINUTES_KEY, builder.minutesArray)
                }
            }
    }

    class Builder {
        var title: String = ""
            private set
        var duration: String = ""
            private set

        var minutesArray = arrayOf("00", "15", "30", "45")
            private set
        var hoursArray = Array(24) { "%02d".format(it) }
            private set

        /**
         * Sets picker dialog title
         */
        fun title(title: String) = apply { this.title = title }

        /**
         * Sets picker default duration as "hh:mm" string
         */
        fun duration(duration: String) = apply { this.duration = duration }

        /**
         * Sets minutes range set
         * @param min [Int] inclusive start
         * @param max [Int] exclusive end
         * @param step [Int] a value in range of `0` and `59`
         */
        fun minutes(
            min: Int = 0,
            max: Int = 60,
            @IntRange(from = 1, to = 59) step: Int = 1
        ) = apply {
            minutesArray = valuesArray(min, max, step)
        }

        /**
         * Sets hours minutes set
         * @param min [Int] inclusive start
         * @param max [Int] exclusive end
         * @param step [Int] a value in range of `0` and `23`
         */
        fun hours(
            min: Int = 0,
            max: Int = 24,
            @IntRange(from = 1, to = 23) step: Int = 1
        ) = apply {
            hoursArray = valuesArray(min, max, step)
        }

        fun build() = newInstance(this)

        private fun valuesArray(min: Int, max: Int, step: Int): Array<String> {
            val values = mutableListOf<String>()
            for (item in min until max step step) {
                values.add("%02d".format(item))
            }
            if (values.size == 0) {
                return arrayOf("00")
            }
            return values.toTypedArray()
        }
    }
}
