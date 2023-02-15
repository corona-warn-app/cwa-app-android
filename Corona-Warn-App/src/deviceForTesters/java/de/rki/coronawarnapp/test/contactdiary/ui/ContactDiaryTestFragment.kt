package de.rki.coronawarnapp.test.contactdiary.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.core.widget.TextViewCompat
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentTestContactDiaryBinding
import de.rki.coronawarnapp.test.menu.ui.TestMenuItem
import de.rki.coronawarnapp.ui.durationpicker.DurationPicker
import de.rki.coronawarnapp.ui.durationpicker.format
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import java.time.Duration
import javax.inject.Inject
import kotlin.math.roundToInt

@SuppressLint("SetTextI18n")
class ContactDiaryTestFragment :
    Fragment(R.layout.fragment_test_contact_diary),
    AutoInject,
    DurationPicker.OnChangeListener {
    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val vm: ContactDiaryTestFragmentViewModel by cwaViewModels { viewModelFactory }

    private val binding: FragmentTestContactDiaryBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vm.locationVisits.observe(viewLifecycleOwner) {
            binding.locationVisitsFancy.text = vm.getFancyLocationVisitString(it)
            binding.locationVisitsStatus.text = vm.getLocationVisitStatusString(it)
        }

        vm.personEncounters.observe(viewLifecycleOwner) {
            binding.personEncountersFancy.text = vm.getFancyPersonEncounterString(it)
            binding.personEncountersStatus.text = vm.getPersonEncounterStatusString(it)
        }

        binding.apply {
            wipeAllButton.setOnClickListener { vm.clearAll() }
            outdatedLocationVisitsButton.setOnClickListener { vm.createLocationVisit(true) }
            normalLocationVisitsButton.setOnClickListener { vm.createLocationVisit(false) }
            outdatedPersonEncountersButton.setOnClickListener { vm.createPersonEncounters(true) }
            normalPersonEncountersButton.setOnClickListener { vm.createPersonEncounters(false) }
            locationVisitsCleanButton.setOnClickListener { vm.clearLocationVisits() }
            personEncountersCleanButton.setOnClickListener { vm.clearPersonEncounters() }
            durationValue.setOnClickListener {
                val durationPicker = DurationPicker.Builder()
                    .duration(binding.durationValue.text.toString())
                    .title(getString(R.string.duration_dialog_title))
                    .build()
                durationPicker.setDurationChangeListener(this@ContactDiaryTestFragment)
                durationPicker.show(parentFragmentManager, "ContactDiaryDurationPickerFragment")
            }

            durationValue2.setOnClickListener {
                val durationPicker = DurationPicker.Builder()
                    .duration(binding.durationValue2.text.toString())
                    .title("Presence tracing title")
                    .minutes(step = minutesSlider.value.toInt())
                    .hours(
                        min = hoursLimitsRangeSlider.values.first().roundToInt(),
                        max = hoursLimitsRangeSlider.values.last().roundToInt(),
                        step = hoursSlider.value.toInt()
                    )
                    .build()
                durationPicker.show(parentFragmentManager, "PresenceTracing")
                durationPicker.setDurationChangeListener {
                    durationValue2.text = it.format()
                }
            }

            hoursLimitsRangeSlider.setValues(0f, 24f)
        }
    }

    override fun onChange(duration: Duration) {
        with(binding.durationValue) {
            text = duration.format()
            if (duration.toMillis() == 0L) {
                setBackgroundResource(R.drawable.contact_diary_duration_background_default)
                TextViewCompat.setTextAppearance(this, R.style.bodyNeutral)
            } else {
                setBackgroundResource(R.drawable.contact_diary_duration_background_selected)
                TextViewCompat.setTextAppearance(this, R.style.body1)
            }
        }
    }

    companion object {
        val TAG: String = ContactDiaryTestFragment::class.simpleName!!
        val MENU_ITEM = TestMenuItem(
            title = "Contact Diary Test Options",
            description = "Contact Diary related test options.",
            targetId = R.id.test_contact_diary_fragment
        )
    }
}
