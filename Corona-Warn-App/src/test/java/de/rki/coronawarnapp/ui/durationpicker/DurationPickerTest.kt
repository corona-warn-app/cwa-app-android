package de.rki.coronawarnapp.ui.durationpicker

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DurationPickerTest {

    private lateinit var durationPickerBuilder: DurationPicker.Builder

    @BeforeEach
    fun setup() {
        durationPickerBuilder = DurationPicker.Builder()
    }

    @Test
    fun `check hours array`() {
        durationPickerBuilder.hoursArray.count() shouldBe 24
        for (i in 0..9) {
            durationPickerBuilder.hoursArray[i] shouldBe "0$i"
        }
        for (i in 10..23) {
            durationPickerBuilder.hoursArray[i] shouldBe "$i"
        }
    }

    @Test
    fun `check minutes array`() {
        durationPickerBuilder.minutesArray.count() shouldBe 4
        durationPickerBuilder.minutesArray[0] shouldBe "00"
        for (i in 1..3) {
            durationPickerBuilder.minutesArray[i] shouldBe "${i * 15}"
        }
    }
}
